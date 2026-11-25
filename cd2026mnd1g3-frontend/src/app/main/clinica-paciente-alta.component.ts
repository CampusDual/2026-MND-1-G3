import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { catchError, debounceTime, distinctUntilChanged, EMPTY, of, switchMap, tap } from 'rxjs';
import { ClinicaPacienteService } from '../core/clinica-paciente.service';
import { httpErrorMessage } from '../core/http-error-message';
import {
  AltaPacienteRequest,
  EspecieMascota,
  PacienteClinica,
  PropietarioClinica
} from '../core/clinica-paciente.models';

/** Alta progresiva de propietarios y mascotas clínicas con guardado transaccional. */
@Component({
  selector: 'app-clinica-paciente-alta',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './clinica-paciente-alta.component.html',
  styleUrl: './clinica-paciente-alta.component.scss'
})
export class ClinicaPacienteAltaComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly service = inject(ClinicaPacienteService);
  private readonly destroyRef = inject(DestroyRef);

  readonly species: EspecieMascota[] = ['PERRO', 'GATO', 'PAJARO', 'OTRO'];
  readonly dirty = signal(false);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly success = signal('');
  readonly newPatientId = signal<number | null>(null);
  readonly attemptedSave = signal(false);
  readonly existingOwnerMode = signal(true);
  readonly duplicateOwner = signal<PropietarioClinica | null>(null);
  readonly ownerMatches = signal<PropietarioClinica[]>([]);
  readonly ownerPets = signal<PacienteClinica[]>([]);
  readonly ownerPetNames = computed(() =>
    this.ownerPets().map(pet => pet.nombre).join(', ')
  );
  readonly selectedOwner = signal<PropietarioClinica | null>(null);
  readonly breeds = signal<string[]>([
    'Mestizo / Sin raza definida',
    'Sin especificar'
  ]);
  readonly duplicatePetWarning = signal('');
  readonly ownerSearchLoading = signal(false);
  readonly ownerDropdownOpen = signal(false);

  readonly searchForm = this.fb.control('', { validators: [Validators.maxLength(250)] });

  readonly ownerForm = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(120)]],
    apellidos: ['', [Validators.required, Validators.maxLength(120)]],
    dniNif: ['', Validators.maxLength(20)],
    telefono: ['', [Validators.required, Validators.pattern(/^\+[1-9][0-9]{7,14}$/)]],
    email: ['', [Validators.email, Validators.maxLength(160)]]
  });

  readonly patientForm = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    especie: this.fb.control<EspecieMascota>('PERRO', { validators: [Validators.required] }),
    raza: this.fb.control<string>('Mestizo / Sin raza definida'),
    sinMicrochip: this.fb.control(false),
    microchip: ['', Validators.maxLength(30)],
    ageMode: this.fb.control<'exact' | 'approx'>('exact'),
    fechaNacimiento: '',
    edadAproxAnos: this.fb.control<number | null>(null),
    edadAproxMeses: this.fb.control<number | null>(null),
    sexo: this.fb.control<'MACHO' | 'HEMBRA' | 'DESCONOCIDO'>('DESCONOCIDO'),
    esterilizado: this.fb.control<'SI' | 'NO' | 'DESCONOCIDO'>('DESCONOCIDO'),
    pesoInicial: this.fb.control<number | null>(null),
    alergias: false,
    manejoNervioso: false,
    patologiaCronica: false,
    notaBanderas: ['', Validators.maxLength(500)]
  });

  constructor() {
    this.loadBreeds('PERRO');
    this.searchForm.valueChanges.pipe(
      debounceTime(250),
      distinctUntilChanged(),
      switchMap(value => {
        const term = value.trim();
        if (term.length < 2) {
          this.ownerMatches.set([]);
          this.ownerSearchLoading.set(false);
          this.ownerDropdownOpen.set(false);
          return of([]);
        }
        this.ownerSearchLoading.set(true);
        this.ownerDropdownOpen.set(true);
        return this.service.buscarPropietarios(term).pipe(
          catchError(error => {
            this.ownerSearchLoading.set(false);
            this.error.set(httpErrorMessage(error, 'No se pudo buscar el propietario.'));
            return of([]);
          })
        );
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: matches => {
        this.ownerMatches.set(matches);
        this.ownerSearchLoading.set(false);
        this.ownerDropdownOpen.set(true);
      },
      error: error => {
        this.ownerSearchLoading.set(false);
        this.ownerDropdownOpen.set(true);
        this.error.set(httpErrorMessage(error, 'No se pudo buscar el propietario.'));
      }
    });

    this.ownerForm.valueChanges.pipe(
      tap(() => this.dirty.set(true)),
      debounceTime(300),
      distinctUntilChanged((previous, current) =>
        previous.telefono === current.telefono && previous.dniNif === current.dniNif),
      switchMap(() => {
        this.duplicateOwner.set(null);
        if (this.existingOwnerMode()) {
          return of(null);
        }
        const owner = this.ownerForm.getRawValue();
        if (!owner.telefono && !owner.dniNif) {
          return of(null);
        }
        return this.service.buscarDuplicado(owner.telefono, owner.dniNif).pipe(
          catchError(error => {
            this.error.set(httpErrorMessage(error, 'No se pudo comprobar si ya existe el propietario.'));
            return EMPTY;
          })
        );
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(match => this.duplicateOwner.set(match));

    this.patientForm.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.dirty.set(true);
        this.checkDuplicatePet();
      });

    this.patientForm.controls.especie.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(species => this.loadBreeds(species));
  }

  setOwnerMode(existing: boolean): void {
    this.existingOwnerMode.set(existing);
    this.duplicateOwner.set(null);
    this.selectedOwner.set(null);
    this.ownerPets.set([]);
  }

  selectOwner(owner: PropietarioClinica): void {
    this.selectedOwner.set(owner);
    this.ownerDropdownOpen.set(false);
    this.ownerMatches.set([]);
    this.service.mascotasPropietario(owner.id).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: pets => this.ownerPets.set(pets),
      error: error => this.error.set(httpErrorMessage(error, 'No se pudieron cargar las mascotas del propietario.'))
    });
  }

  changeOwner(): void {
    this.selectedOwner.set(null);
    this.ownerPets.set([]);
    this.searchForm.setValue('', { emitEvent: false });
    this.ownerMatches.set([]);
    this.ownerDropdownOpen.set(true);
  }

  createNewOwner(): void {
    this.setOwnerMode(false);
    this.ownerDropdownOpen.set(false);
  }

  get weightUnit(): 'kg' | 'g' {
    const species = this.patientForm.controls.especie.value;
    return species === 'PERRO' || species === 'GATO' ? 'kg' : 'g';
  }

  canSave(): boolean {
    if (this.existingOwnerMode() && !this.selectedOwner()) return false;
    if (!this.existingOwnerMode() && this.ownerForm.invalid) return false;
    return this.patientForm.valid;
  }

  save(addAnother = false): void {
    this.attemptedSave.set(true);
    if (!this.canSave()) {
      this.ownerForm.markAllAsTouched();
      this.patientForm.markAllAsTouched();
      this.error.set('Completa los bloques obligatorios antes de guardar.');
      return;
    }

    const patient = this.patientForm.getRawValue();
    const owner = this.ownerForm.getRawValue();
    const approx = patient.ageMode === 'approx';
    const request: AltaPacienteRequest = {
      clienteExistenteId: this.existingOwnerMode()
        ? this.selectedOwner()?.id : undefined,
      nuevoCliente: this.existingOwnerMode() ? undefined : {
        nombre: owner.nombre,
        apellidos: owner.apellidos,
        dniNif: owner.dniNif || undefined,
        telefono: owner.telefono,
        email: owner.email || undefined
      },
      nombre: patient.nombre,
      especie: patient.especie,
      raza: patient.raza,
      sinMicrochip: patient.sinMicrochip,
      microchip: patient.sinMicrochip ? undefined : patient.microchip || undefined,
      fechaNacimiento: approx ? undefined : patient.fechaNacimiento || undefined,
      fechaNacimientoEstimada: approx,
      edadAproxAnos: approx ? patient.edadAproxAnos ?? undefined : undefined,
      edadAproxMeses: approx ? patient.edadAproxMeses ?? undefined : undefined,
      sexo: patient.sexo,
      esterilizado: patient.esterilizado,
      pesoInicial: patient.pesoInicial ?? undefined,
      alergias: patient.alergias,
      manejoNervioso: patient.manejoNervioso,
      patologiaCronica: patient.patologiaCronica,
      notaBanderas: patient.notaBanderas || undefined
    };

    this.loading.set(true);
    this.error.set('');
    this.service.alta(request).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: response => {
        this.loading.set(false);
        this.newPatientId.set(response.paciente.id);
        this.success.set(`${response.paciente.nombre} se ha registrado correctamente.`);
        this.finishSave(addAnother ? response.propietario : undefined);
      },
      error: error => {
        this.loading.set(false);
        this.error.set(httpErrorMessage(error, 'No se pudo guardar la mascota. Comprueba duplicados y datos obligatorios.'));
      }
    });
  }

  closeError(): void {
    this.error.set('');
  }

  fieldError(control: AbstractControl): string {
    if (!control.touched || !control.errors) return '';
    if (control.errors['required']) return 'Este campo es obligatorio.';
    if (control.errors['pattern']) return 'Usa el formato internacional, por ejemplo +34600123456.';
    if (control.errors['email']) return 'Introduce un email válido.';
    if (control.errors['maxlength']) return 'Se ha superado la longitud máxima.';
    return 'Revisa el valor introducido.';
  }

  private checkDuplicatePet(): void {
    const owner = this.selectedOwner();
    const patient = this.patientForm.getRawValue();
    if (!owner || !patient.nombre) {
      this.duplicatePetWarning.set('');
      return;
    }
    const match = this.ownerPets().some(pet =>
      pet.nombre.toLowerCase() === patient.nombre.toLowerCase()
      && pet.especie === patient.especie);
    this.duplicatePetWarning.set(match
      ? 'Ya existe una mascota con el mismo nombre y especie para este propietario.'
      : '');
  }

  private loadBreeds(especies: EspecieMascota): void {
    this.service.razas(especies).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: breeds => {
        this.breeds.set(breeds);
        this.patientForm.controls.raza.setValue(breeds[0] ?? 'Sin especificar', { emitEvent: false });
      },
      error: error => this.error.set(httpErrorMessage(error, 'No se pudieron cargar las razas.'))
    });
  }

  private finishSave(owner?: PropietarioClinica): void {
    this.resetPatientForm();
    this.ownerForm.reset(undefined, { emitEvent: false });
    this.searchForm.reset('', { emitEvent: false });
    this.selectedOwner.set(owner ?? null);
    if (!owner) this.ownerPets.set([]);
    this.ownerMatches.set([]);
    this.duplicateOwner.set(null);
    this.duplicatePetWarning.set('');
    this.ownerDropdownOpen.set(false);
    this.existingOwnerMode.set(true);
    if (owner) this.success.set('Mascota guardada. El propietario permanece seleccionado.');
    this.dirty.set(false);
  }

  private resetPatientForm(): void {
    this.patientForm.reset({
      nombre: '',
      especie: 'PERRO',
      raza: 'Mestizo / Sin raza definida',
      sinMicrochip: false,
      microchip: '',
      ageMode: 'exact',
      fechaNacimiento: '',
      edadAproxAnos: null,
      edadAproxMeses: null,
      sexo: 'DESCONOCIDO',
      esterilizado: 'DESCONOCIDO',
      pesoInicial: null,
      alergias: false,
      manejoNervioso: false,
      patologiaCronica: false,
      notaBanderas: ''
    }, { emitEvent: false });
    this.loadBreeds('PERRO');
  }
}
