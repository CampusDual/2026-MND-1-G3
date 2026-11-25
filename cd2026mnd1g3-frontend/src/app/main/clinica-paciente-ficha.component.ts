import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { EMPTY, Observable, catchError, expand, forkJoin, map, of, reduce, switchMap } from 'rxjs';
import { AvisoClinico, AvisoPage, CategoriaAvisoClinico } from '../core/aviso-clinico.models';
import { AvisoClinicoService } from '../core/aviso-clinico.service';
import { ClinicaPacienteService } from '../core/clinica-paciente.service';
import { ConsultaClinica, PacienteClinica, RecetaClinica } from '../core/clinica-paciente.models';
import { httpErrorMessage } from '../core/http-error-message';
import { LoteStock, LivestockService, Medicamento, MovimientoInventario } from '../core/livestock.service';
import { RecetaClinicaService } from '../core/receta-clinica.service';

interface PendingVaccineNotice {
  patientId: number;
  consultationDate: string;
  targetDate: string;
  vaccineName: string;
}

interface ConsultationFormValue {
  fecha: string;
  hora: string;
  motivo: string;
  notas: string;
  proximaVacuna: string;
  vacuna: string;
  medicamentoId: number | null;
  loteStockId: number | null;
  cantidad: number | null;
  indicaciones: string;
}

type VaccineNoticeResult = 'created' | 'already-pending';

/** Ficha central del paciente clínico y sus consultas, recetas y avisos. */
@Component({
  selector: 'app-clinica-paciente-ficha',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './clinica-paciente-ficha.component.html',
  styleUrl: './clinica-paciente-ficha.component.scss'
})
export class ClinicaPacienteFichaComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly patientsService = inject(ClinicaPacienteService);
  private readonly prescriptionsService = inject(RecetaClinicaService);
  private readonly noticesService = inject(AvisoClinicoService);
  private readonly inventoryService = inject(LivestockService);
  private readonly destroyRef = inject(DestroyRef);

  readonly patient = signal<PacienteClinica | null>(null);
  readonly consultations = signal<ConsultaClinica[]>([]);
  readonly prescriptions = signal<RecetaClinica[]>([]);
  readonly notices = signal<AvisoClinico[]>([]);
  readonly medications = signal<Medicamento[]>([]);
  readonly lots = signal<LoteStock[]>([]);
  readonly movements = signal<MovimientoInventario[]>([]);
  readonly loading = signal(true);
  readonly savingConsultation = signal(false);
  readonly savingNotice = signal(false);
  readonly schedulingVaccineNotice = signal(false);
  readonly showConsultationForm = signal(false);
  readonly showNoticeForm = signal(false);
  readonly pendingVaccineNotice = signal<PendingVaccineNotice | null>(null);
  readonly vaccineNoticeError = signal('');
  readonly error = signal('');
  readonly success = signal('');
  readonly medicationLots = computed(() => this.lots().filter(lot =>
    this.consultationForm.controls.medicamentoId.value === null
      || lot.medicamentoId === this.consultationForm.controls.medicamentoId.value));

  readonly consultationForm = new FormGroup({
    fecha: new FormControl(this.localDate(), { nonNullable: true, validators: [Validators.required] }),
    hora: new FormControl(this.localTime(), { nonNullable: true, validators: [Validators.required] }),
    motivo: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(500)] }),
    notas: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(2000)] }),
    proximaVacuna: new FormControl('', { nonNullable: true }),
    vacuna: new FormControl('Revacunación', { nonNullable: true, validators: [Validators.maxLength(120)] }),
    medicamentoId: new FormControl<number | null>(null),
    loteStockId: new FormControl<number | null>(null),
    cantidad: new FormControl<number | null>(null, [Validators.min(0.01)]),
    indicaciones: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(1000)] })
  });

  readonly noticeForm = new FormGroup({
    categoria: new FormControl<CategoriaAvisoClinico>('REVISION', { nonNullable: true, validators: [Validators.required] }),
    titulo: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(150)] }),
    fechaObjetivo: new FormControl(this.localDate(), { nonNullable: true, validators: [Validators.required] }),
    detalleClinico: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(2000)] })
  });

  constructor() {
    this.consultationForm.controls.medicamentoId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.consultationForm.controls.loteStockId.setValue(null));
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isInteger(id) || id <= 0) {
      this.loading.set(false);
      this.error.set('El identificador del paciente no es válido.');
      return;
    }
    this.load(id);
  }

  openConsultationForm(): void {
    this.showConsultationForm.set(true);
    this.success.set('');
    this.error.set('');
  }

  openNoticeForm(): void {
    this.showNoticeForm.set(true);
    this.success.set('');
    this.error.set('');
  }

  closeNoticeForm(): void {
    this.showNoticeForm.set(false);
  }

  closeConsultationForm(): void {
    this.showConsultationForm.set(false);
    this.consultationForm.reset({
      fecha: this.localDate(), hora: this.localTime(), motivo: '', notas: '',
      proximaVacuna: '', vacuna: 'Revacunación',
      medicamentoId: null, loteStockId: null, cantidad: null, indicaciones: ''
    });
  }

  stockOfLot(lotId: number): number {
    return this.movements()
      .filter(movement => movement.loteStockId === lotId)
      .reduce((stock, movement) => stock + movement.cantidad, 0);
  }

  medicationName(id: number | undefined): string {
    return this.medications().find(item => item.id === id)?.nombreComercial ?? 'Medicamento';
  }

  lotName(id: number | undefined): string {
    return this.lots().find(item => item.id === id)?.numeroLote ?? 'Sin lote';
  }

  fieldError(control: AbstractControl): string {
    if (!control.touched || !control.errors) return '';
    if (control.errors['required']) return 'Este campo es obligatorio.';
    if (control.errors['min']) return 'La cantidad debe ser mayor que cero.';
    if (control.errors['dateInPast']) return 'La fecha debe ser hoy o posterior.';
    if (control.errors['dateBeforeConsultation']) return 'La próxima vacuna debe ser posterior a la fecha de consulta.';
    if (control.errors['maxlength']) return 'Se ha superado la longitud máxima.';
    return 'Revisa el valor introducido.';
  }

  submitConsultation(): void {
    this.error.set('');
    this.success.set('');
    this.consultationForm.markAllAsTouched();
    const form = this.consultationForm.getRawValue();
    const vaccineDateControl = this.consultationForm.controls.proximaVacuna;
    const vaccineDateErrors = { ...(vaccineDateControl.errors ?? {}) };
    if (form.proximaVacuna && form.proximaVacuna <= form.fecha) {
      vaccineDateErrors['dateBeforeConsultation'] = true;
    } else {
      delete vaccineDateErrors['dateBeforeConsultation'];
    }
    vaccineDateControl.setErrors(Object.keys(vaccineDateErrors).length ? vaccineDateErrors : null);
    const hasMedicationDetails = form.medicamentoId !== null
      || form.loteStockId !== null
      || form.cantidad !== null;
    const hasCompleteMedication = form.medicamentoId !== null
      && form.loteStockId !== null
      && form.cantidad !== null
      && form.cantidad > 0;
    if (this.consultationForm.invalid
      || (hasMedicationDetails && !hasCompleteMedication)) {
      if (hasMedicationDetails && !hasCompleteMedication) {
        this.error.set('Para emitir una receta, selecciona medicamento y lote e indica una cantidad válida.');
      } else {
        this.error.set('Revisa los campos obligatorios de la consulta.');
      }
      return;
    }
    if (form.loteStockId !== null && form.cantidad !== null
      && form.cantidad > this.stockOfLot(form.loteStockId)) {
      this.error.set('La cantidad supera el stock disponible del lote.');
      return;
    }

    const patient = this.patient();
    if (!patient) return;
    this.savingConsultation.set(true);
    this.patientsService.crearConsulta({
      pacienteId: patient.id,
      fechaConsulta: `${form.fecha}T${form.hora}:00`,
      motivo: form.motivo.trim(),
      notas: form.notas.trim() || undefined,
      proximaVacuna: form.proximaVacuna || undefined
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: consultation => {
        const pendingNotice = form.proximaVacuna
          ? {
            patientId: patient.id,
            consultationDate: form.fecha,
            targetDate: form.proximaVacuna,
            vaccineName: form.vacuna.trim() || 'Revacunación'
          }
          : null;
        this.createPrescription(consultation.id, patient.id, form, hasMedicationDetails).pipe(
          switchMap(prescriptionError => {
            if (!pendingNotice) return of({ prescriptionError, vaccineResult: null });
            return this.createVaccineNotice(pendingNotice).pipe(
              map(vaccineResult => ({ prescriptionError, vaccineResult }))
            );
          }),
          takeUntilDestroyed(this.destroyRef)
        ).subscribe({
          next: result => {
            this.resetConsultationForm();
            this.showConsultationForm.set(false);
            this.savingConsultation.set(false);
            this.load(patient.id);
            if (result.vaccineResult === 'created') {
              this.success.set(`Aviso de vacunación programado para el ${pendingNotice?.targetDate}.`);
            } else if (result.vaccineResult === 'already-pending') {
              this.success.set(`Ya existía un aviso de vacunación pendiente para el ${pendingNotice?.targetDate}.`);
            } else {
              this.success.set('Consulta registrada correctamente.');
            }
            if (result.prescriptionError) this.error.set(result.prescriptionError);
          },
          error: error => {
            this.resetConsultationForm();
            this.showConsultationForm.set(false);
            this.savingConsultation.set(false);
            this.pendingVaccineNotice.set(pendingNotice);
            this.vaccineNoticeError.set(httpErrorMessage(error, 'La consulta se guardó, pero no se pudo programar el aviso.'));
            this.load(patient.id);
          }
        });
      },
      error: error => {
        this.savingConsultation.set(false);
        this.error.set(httpErrorMessage(error, 'No se pudo guardar la consulta.'));
      }
    });
  }

  submitNotice(): void {
    this.error.set('');
    this.success.set('');
    this.noticeForm.markAllAsTouched();
    const form = this.noticeForm.getRawValue();
    const dateControl = this.noticeForm.controls.fechaObjetivo;
    const dateErrors = { ...(dateControl.errors ?? {}) };
    if (form.fechaObjetivo && form.fechaObjetivo < this.localDate()) {
      dateErrors['dateInPast'] = true;
    } else {
      delete dateErrors['dateInPast'];
    }
    dateControl.setErrors(Object.keys(dateErrors).length ? dateErrors : null);
    if (this.noticeForm.invalid) {
      this.error.set('Revisa los campos obligatorios del aviso.');
      return;
    }
    const patient = this.patient();
    if (!patient) return;
    this.savingNotice.set(true);
    this.noticesService.crear({
      pacienteId: patient.id,
      categoria: form.categoria,
      titulo: form.titulo.trim(),
      fechaObjetivo: form.fechaObjetivo,
      detalleClinico: form.detalleClinico.trim() || undefined
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.savingNotice.set(false);
        this.showNoticeForm.set(false);
        this.noticeForm.reset({
          categoria: 'REVISION', titulo: '', fechaObjetivo: this.localDate(), detalleClinico: ''
        });
        this.success.set('Aviso creado correctamente.');
        this.load(patient.id);
      },
      error: error => {
        this.savingNotice.set(false);
        this.error.set(httpErrorMessage(error, 'No se pudo crear el aviso.'));
      }
    });
  }

  retryVaccineNotice(): void {
    const pending = this.pendingVaccineNotice();
    if (!pending || this.schedulingVaccineNotice()) return;
    this.schedulingVaccineNotice.set(true);
    this.vaccineNoticeError.set('');
    this.createVaccineNotice(pending).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: result => {
        this.schedulingVaccineNotice.set(false);
        this.pendingVaccineNotice.set(null);
        this.load(pending.patientId);
        this.success.set(result === 'created'
          ? `Aviso de vacunación programado para el ${pending.targetDate}.`
          : `Ya existía un aviso de vacunación pendiente para el ${pending.targetDate}.`);
      },
      error: error => {
        this.schedulingVaccineNotice.set(false);
        this.vaccineNoticeError.set(httpErrorMessage(error, 'La consulta se guardó, pero no se pudo programar el aviso.'));
      }
    });
  }

  private createPrescription(
    consultationId: number,
    patientId: number,
    form: ConsultationFormValue,
    hasMedicationDetails: boolean
  ): Observable<string | null> {
    if (!hasMedicationDetails || form.medicamentoId === null
      || form.loteStockId === null || form.cantidad === null) {
      return of(null);
    }
    const lotId = form.loteStockId;
    const quantity = form.cantidad;
    return this.prescriptionsService.crear({
      consultaId: consultationId,
      pacienteId: patientId,
      medicamentoId: form.medicamentoId,
      loteStockId: form.loteStockId,
      indicaciones: form.indicaciones.trim() || form.notas.trim() || form.motivo.trim(),
      fechaEmision: form.fecha
    }).pipe(
      switchMap(() => this.inventoryService.registrarMovimiento({
        loteStockId: lotId,
        cantidad: quantity,
        tipo: 'SALIDA_CLINICA'
      })),
      map(() => null),
      catchError(error => of(
        `La consulta se guardó, pero no se pudo emitir la receta o descontar el stock: ${httpErrorMessage(error, 'error de receta o inventario')}`
      ))
    );
  }

  private createVaccineNotice(pending: PendingVaccineNotice): Observable<VaccineNoticeResult> {
    return this.findAllNotices().pipe(
      switchMap(notices => {
        const alreadyPending = notices.some(notice =>
          notice.pacienteId === pending.patientId
          && notice.categoria === 'VACUNACION'
          && notice.fechaObjetivo === pending.targetDate
          && notice.estado === 'PENDIENTE'
          && notice.activo);
        if (alreadyPending) return of('already-pending' as const);
        return this.noticesService.crear({
          pacienteId: pending.patientId,
          categoria: 'VACUNACION',
          titulo: `Vacuna: ${pending.vaccineName}`,
          fechaObjetivo: pending.targetDate,
          detalleClinico: `Programado automáticamente desde la consulta del ${pending.consultationDate}`
        }).pipe(map(() => 'created' as const));
      })
    );
  }

  private resetConsultationForm(): void {
    this.consultationForm.reset({
      fecha: this.localDate(), hora: this.localTime(), motivo: '', notas: '',
      proximaVacuna: '', vacuna: 'Revacunación',
      medicamentoId: null, loteStockId: null, cantidad: null, indicaciones: ''
    });
  }

  private load(patientId: number): void {
    this.loading.set(true);
    this.error.set('');
    forkJoin({
      patient: this.patientsService.findPaciente(patientId),
      consultations: this.patientsService.findConsultas(),
      prescriptions: this.prescriptionsService.find(),
      notices: this.findAllNotices(),
      medications: this.inventoryService.medicamentos(),
      lots: this.inventoryService.lotes(),
      movements: this.inventoryService.movimientos()
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: data => {
        this.patient.set(data.patient);
        this.consultations.set(data.consultations.filter(item => item.pacienteId === patientId));
        this.prescriptions.set(data.prescriptions.filter(item => item.pacienteId === patientId));
        this.notices.set(data.notices.filter(item => item.pacienteId === patientId));
        this.medications.set(data.medications);
        this.lots.set(data.lots);
        this.movements.set(data.movements);
        this.loading.set(false);
      },
      error: error => {
        this.loading.set(false);
        this.error.set(httpErrorMessage(error, 'No se pudo cargar la ficha completa del paciente.'));
      }
    });
  }

  private findAllNotices(): Observable<AvisoClinico[]> {
    return this.noticesService.find(0, 100).pipe(
      expand((page: AvisoPage) =>
        page.page + 1 < page.totalPages
          ? this.noticesService.find(page.page + 1, 100)
          : EMPTY
      ),
      reduce((all: AvisoClinico[], page: AvisoPage) => [...all, ...page.content], [])
    );
  }

  private localDate(): string {
    const date = new Date();
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }

  private localTime(): string {
    const date = new Date();
    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
  }
}
