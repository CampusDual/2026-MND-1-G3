import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, debounceTime, distinctUntilChanged, of, startWith, switchMap, tap } from 'rxjs';
import { ClinicaPacienteService } from '../core/clinica-paciente.service';
import { httpErrorMessage } from '../core/http-error-message';
import { PacienteClinica, PacientePage } from '../core/clinica-paciente.models';

/** Directorio paginado de pacientes de la clínica. */
@Component({
  selector: 'app-clinica-pacientes',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './clinica-pacientes.component.html',
  styleUrl: './clinica-pacientes.component.scss'
})
export class ClinicaPacientesComponent {
  private readonly patientsService = inject(ClinicaPacienteService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly search = new FormControl('', { nonNullable: true });
  readonly patients = signal<PacienteClinica[]>([]);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal('');
  private readonly pageSize = 20;

  constructor() {
    this.search.valueChanges.pipe(
      startWith(this.search.value),
      debounceTime(300),
      distinctUntilChanged(),
      tap(() => {
        this.page.set(0);
        this.loading.set(true);
        this.error.set('');
      }),
      switchMap(search => this.patientsService.findPacientes(search, 0, this.pageSize).pipe(
        catchError(error => {
          this.error.set(httpErrorMessage(error, 'No se pudieron cargar los pacientes.'));
          return of(null);
        })
      )),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(result => this.applyPage(result));
  }

  setPage(page: number): void {
    if (page < 0 || page >= this.totalPages()) return;
    this.page.set(page);
    this.loading.set(true);
    this.error.set('');
    this.patientsService.findPacientes(this.search.value, page, this.pageSize).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: result => this.applyPage(result),
      error: error => {
        this.loading.set(false);
        this.error.set(httpErrorMessage(error, 'No se pudo cambiar de página.'));
      }
    });
  }

  openPatient(id: number): void {
    void this.router.navigate(['/clinica/pacientes', id]);
  }

  private applyPage(result: PacientePage | null): void {
    this.loading.set(false);
    if (!result) {
      this.patients.set([]);
      return;
    }
    this.patients.set(result.content);
    this.totalPages.set(result.totalPages);
    this.totalElements.set(result.totalElements);
  }
}
