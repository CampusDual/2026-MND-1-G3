import { ChangeDetectionStrategy, Component, OnInit, inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { WebSocketService } from '../../core/websocket/websocket.service';
import {
  AlertaSistema,
  Animal,
  Farm,
  LoteStock,
  LivestockService,
  Medicamento,
  MovimientoInventario
} from '../../core/livestock.service';
import { SkeletonLoaderComponent } from '../../shared/ui/skeleton-loader.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state.component';
import { ErrorBannerComponent } from '../../shared/ui/error-banner.component';
import { ConfirmDialogComponent } from '../../shared/ui/confirm-dialog.component';
import { ToastNotificationComponent } from '../../shared/ui/toast-notification.component';
import { PatientDirectoryComponent } from './patient-directory.component';

interface ConsultaReceta {
  fecha: string;
  animalId: number;
  loteStockId: number;
  cantidad: number;
  motivo: string;
}

/** Vista operativa de consultas, recetas y trazabilidad clínica. */
@Component({
  selector: 'app-clinica-dashboard',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink, SkeletonLoaderComponent, EmptyStateComponent, ErrorBannerComponent, ConfirmDialogComponent, ToastNotificationComponent, PatientDirectoryComponent],
  templateUrl: './clinica-dashboard.component.html',
  styleUrl: './clinica-dashboard.component.scss'
})
export class ClinicaDashboardComponent implements OnInit, OnDestroy {
  private readonly service = inject(LivestockService);
  private readonly webSocketService = inject(WebSocketService);
  private alertSubscription?: Subscription;

  activeTab: 'pacientes' | 'consultas' | 'inventario' = 'pacientes';
  farms: Farm[] = [];
  animals: Animal[] = [];
  medicamentos: Medicamento[] = [];
  lotes: LoteStock[] = [];
  movimientos: MovimientoInventario[] = [];
  alerts: AlertaSistema[] = [];
  selectedMedicationId: number | null = null;
  entryLotId: number | null = null;
  entryQuantity = 0;
  error = '';
  loading = false;
  isSubmitting = false;
  toastMessage = '';
  confirmMovement: 'consultation' | 'entry' | 'anulation' | null = null;
  readonly consultationForm = inject(FormBuilder).nonNullable.group({
    fecha: [new Date().toISOString().slice(0, 10), Validators.required],
    animalId: [0, [Validators.required, Validators.min(1)]],
    loteStockId: [0, [Validators.required, Validators.min(1)]],
    cantidad: [0, [Validators.required, Validators.min(0.01)]],
    motivo: ['', [Validators.required, Validators.minLength(3)]]
  });
  readonly entryForm = inject(FormBuilder).nonNullable.group({
    loteStockId: [0, [Validators.required, Validators.min(1)]],
    cantidad: [0, [Validators.required, Validators.min(0.01)]]
  });
  consultation: ConsultaReceta = this.emptyConsultation();
  consultas: ConsultaReceta[] = [];

  ngOnInit(): void {
    this.webSocketService.connect();
    this.alertSubscription = this.webSocketService.alerts.subscribe(alerts => this.alerts = [...alerts]);
    this.load();
  }

  ngOnDestroy(): void {
    this.alertSubscription?.unsubscribe();
  }

  get unreadAlerts(): number {
    return this.alerts.filter(alert => !alert.leida).length;
  }

  get lotesDisponibles(): LoteStock[] {
    return this.lotes.filter(lote =>
      (this.selectedMedicationId === null || lote.medicamentoId === this.selectedMedicationId)
      && this.stockOfLote(lote.id) > 0);
  }

  stockOfLote(loteId: number): number {
    return this.movimientos
      .filter(movimiento => movimiento.loteStockId === loteId)
      .reduce((total, movimiento) => total + movimiento.cantidad, 0);
  }

  stockOfMedication(medicamentoId: number): number {
    return this.lotes
      .filter(lote => lote.medicamentoId === medicamentoId)
      .reduce((total, lote) => total + this.stockOfLote(lote.id), 0);
  }

  loteNumber(loteId: number): string {
    return this.lotes.find(lote => lote.id === loteId)?.numeroLote ?? String(loteId);
  }

  animalName(animalId: number | undefined): string {
    if (!animalId) return 'Sin animal';
    const animal = this.animals.find(item => item.id === animalId);
    return animal ? `${animal.name || animal.earTag} · ${animal.earTag}` : String(animalId);
  }

  farmName(explotacionId: number | undefined): string {
    if (!explotacionId) return 'Sin explotación';
    return this.farms.find(farm => farm.id === explotacionId)?.name ?? String(explotacionId);
  }

  onMedicationChange(): void {
    this.consultation.loteStockId = 0;
    this.consultationForm.patchValue({ loteStockId: 0 });
  }

  registerConsultation(): void {
    if (this.consultationForm.invalid) {
      this.consultationForm.markAllAsTouched();
      this.error = 'Completa animal, lote, cantidad y motivo.';
      return;
    }
    const raw = this.consultationForm.getRawValue();
    const stock = this.stockOfLote(raw.loteStockId);
    if (raw.cantidad <= 0 || raw.cantidad > stock) {
      this.error = 'La cantidad utilizada debe ser mayor que cero y no superar el stock disponible.';
      return;
    }
    this.confirmMovement = 'consultation';
  }

  executeConsultation(): void {
    const raw = this.consultationForm.getRawValue();
    const animal = this.animals.find(item => item.id === raw.animalId);
    this.isSubmitting = true;
    this.confirmMovement = null;
    this.service.registrarMovimiento({
      loteStockId: raw.loteStockId,
      cantidad: -raw.cantidad,
      tipo: 'SALIDA_CLINICA',
      animalId: raw.animalId,
      explotacionId: animal?.farmId
    }).subscribe({
      next: () => {
        this.consultas = [...this.consultas, raw];
        this.consultation = this.emptyConsultation();
        this.consultationForm.reset(this.emptyConsultationForm());
        this.toastMessage = 'Consulta y salida clínica registradas.';
        this.load();
      },
      error: () => this.error = 'No se pudo registrar la salida clínica.',
      complete: () => this.isSubmitting = false
    });
  }

  registerPurchaseEntry(): void {
    if (this.entryForm.invalid) {
      this.entryForm.markAllAsTouched();
      this.error = 'Selecciona un lote y una cantidad válida.';
      return;
    }
    this.confirmMovement = 'entry';
  }

  executePurchaseEntry(): void {
    const raw = this.entryForm.getRawValue();
    this.isSubmitting = true;
    this.confirmMovement = null;
    this.service.registrarMovimiento({
      loteStockId: raw.loteStockId,
      cantidad: raw.cantidad,
      tipo: 'ENTRADA_COMPRA'
    }).subscribe({
      next: () => {
        this.entryForm.reset({ loteStockId: 0, cantidad: 0 });
        this.toastMessage = 'Entrada de lote registrada.';
        this.load();
      },
      error: () => this.error = 'No se pudo registrar la entrada de compra.',
      complete: () => this.isSubmitting = false
    });
  }

  markAlertRead(alert: AlertaSistema): void {
    this.service.markAlertRead(alert.id).subscribe({
      next: updated => this.alerts = this.alerts.map(item => item.id === updated.id ? updated : item),
      error: () => this.error = 'No se pudo marcar la alerta como leída.'
    });
  }

  load(): void {
    this.loading = true;
    this.service.farms().subscribe(data => this.farms = data);
    this.service.animals().subscribe(data => this.animals = data);
    this.service.medicamentos().subscribe(data => this.medicamentos = data);
    this.service.lotes().subscribe(data => this.lotes = data);
    this.service.alerts().subscribe({
      next: data => {
        this.webSocketService.setInitialAlerts(data);
        this.loading = false;
      },
      error: () => { this.error = 'No se pudieron cargar los datos de clínica.'; this.loading = false; }
    });
    this.service.movimientos().subscribe({
      next: data => this.movimientos = data,
      error: () => this.error = 'No se pudo cargar el libro mayor.'
    });
  }

  private emptyConsultation(): ConsultaReceta {
    return {
      fecha: new Date().toISOString().slice(0, 10),
      animalId: 0,
      loteStockId: 0,
      cantidad: 0,
      motivo: ''
    };
  }
  private emptyConsultationForm(): ConsultaReceta {
    return this.emptyConsultation();
  }
}
