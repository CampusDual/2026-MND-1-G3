import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AvisoClinicoService } from '../core/aviso-clinico.service';
import { httpErrorMessage } from '../core/http-error-message';
import {
  AvisoClinico,
  EstadoAvisoClinico,
  MotivoDescarteAviso
} from '../core/aviso-clinico.models';

interface AvisoOwnerGroup {
  propietarioId: number;
  nombrePropietario: string;
  telefonoPropietario?: string;
  emailPropietario?: string;
  avisos: AvisoClinico[];
}

/** Tablero operativo manual de avisos clínicos por propietario y mascota. */
@Component({
  selector: 'app-clinica-avisos',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './clinica-avisos.component.html',
  styleUrl: './clinica-avisos.component.scss'
})
export class ClinicaAvisosComponent {
  private readonly service = inject(AvisoClinicoService);
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = signal(false);
  readonly loadError = signal('');
  readonly actionError = signal('');
  readonly avisos = signal<AvisoClinico[]>([]);
  readonly page = signal(0);
  readonly size = signal(20);
  readonly totalElements = signal(0);
  readonly totalPages = signal(0);
  readonly grouped = signal(true);
  readonly expandedClinicalDetails = signal<Set<number>>(new Set<number>());
  readonly discardAvisoId = signal<number | null>(null);
  readonly discardReason = signal<MotivoDescarteAviso>('OTRO');
  readonly discardNote = signal('');
  readonly postponerAvisoId = signal<number | null>(null);
  readonly postponerFecha = signal('');

  readonly groupedAvisos = computed<AvisoOwnerGroup[]>(() => {
    if (!this.grouped()) {
      return [];
    }
    const map = new Map<number, AvisoOwnerGroup>();
    for (const aviso of this.avisos()) {
      const existing = map.get(aviso.propietarioId);
      if (existing) {
        existing.avisos.push(aviso);
      } else {
        map.set(aviso.propietarioId, {
          propietarioId: aviso.propietarioId,
          nombrePropietario: aviso.nombrePropietario,
          telefonoPropietario: aviso.telefonoPropietario,
          emailPropietario: aviso.emailPropietario,
          avisos: [aviso]
        });
      }
    }
    return [...map.values()];
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.loadError.set('');
    this.service.find(this.page(), this.size()).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: page => {
        this.avisos.set(page.content);
        this.totalElements.set(page.totalElements);
        this.totalPages.set(page.totalPages);
        this.loading.set(false);
      },
      error: error => {
        this.loading.set(false);
        this.loadError.set(httpErrorMessage(error, 'No se pudieron cargar los avisos clínicos.'));
      }
    });
  }

  retry(): void {
    this.load();
  }

  setPage(page: number): void {
    if (page < 0 || (this.totalPages() > 0 && page >= this.totalPages())) return;
    this.page.set(page);
    this.load();
  }

  toggleClinicalDetails(avisoId: number): void {
    this.expandedClinicalDetails.update(current => {
      const updated = new Set(current);
      if (updated.has(avisoId)) updated.delete(avisoId);
      else updated.add(avisoId);
      return updated;
    });
  }

  clinicalDetailsExpanded(avisoId: number): boolean {
    return this.expandedClinicalDetails().has(avisoId);
  }

  marcarIntentado(aviso: AvisoClinico): void {
    this.service.marcarIntentado(aviso.id).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: updated => this.replace(updated),
      error: error => this.actionError.set(httpErrorMessage(error, 'No se pudo registrar el intento.'))
    });
  }

  contactar(aviso: AvisoClinico): void {
    this.setState(aviso, 'CONTACTADO');
  }

  cita(aviso: AvisoClinico): void {
    this.setState(aviso, 'CITA_AGENDADA');
  }

  posponer(aviso: AvisoClinico, days?: number): void {
    const date = new Date(aviso.fechaObjetivo);
    if (days) {
      date.setDate(date.getDate() + days);
      const formatted = date.toISOString().slice(0, 10);
      this.service.posponer(aviso.id, formatted).pipe(
        takeUntilDestroyed(this.destroyRef)
      ).subscribe({
        next: updated => this.replace(updated),
        error: error => this.actionError.set(httpErrorMessage(error, 'No se pudo posponer el aviso.'))
      });
      return;
    }
    this.postponerAvisoId.set(aviso.id);
  }

  confirmarPosponer(): void {
    const id = this.postponerAvisoId();
    if (!id || !this.postponerFecha()) return;
    this.service.posponer(id, this.postponerFecha()).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: updated => {
        this.replace(updated);
        this.postponerAvisoId.set(null);
        this.postponerFecha.set('');
      },
      error: error => this.actionError.set(httpErrorMessage(error, 'No se pudo posponer el aviso.'))
    });
  }

  abrirDescarte(aviso: AvisoClinico): void {
    this.discardAvisoId.set(aviso.id);
  }

  confirmarDescarte(): void {
    const id = this.discardAvisoId();
    if (!id) return;
    if (this.discardReason() === 'FALLECIMIENTO_BAJA'
      && !window.confirm('Confirma de nuevo: ¿dar de baja por fallecimiento y cancelar avisos futuros?')) {
      return;
    }
    this.service.cambiarEstado(
      id,
      'DESCARTADA',
      this.discardReason(),
      this.discardNote().trim() || undefined,
      this.discardReason() === 'FALLECIMIENTO_BAJA'
    ).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.discardAvisoId.set(null);
        this.load();
      },
      error: error => this.actionError.set(httpErrorMessage(error, 'No se pudo descartar el aviso.'))
    });
  }

  async copyText(aviso: AvisoClinico): Promise<void> {
    const text = `Hola ${aviso.nombrePropietario}, le recordamos el aviso de ${aviso.categoria.toLowerCase()} para ${aviso.nombrePaciente}: ${aviso.titulo}. Fecha: ${aviso.fechaObjetivo}.`;
    await navigator.clipboard.writeText(text);
  }

  call(aviso: AvisoClinico): void {
    if (aviso.telefonoPropietario) {
      window.location.href = `tel:${aviso.telefonoPropietario}`;
    }
  }

  email(aviso: AvisoClinico): void {
    if (aviso.emailPropietario) {
      window.location.href = `mailto:${aviso.emailPropietario}?subject=${encodeURIComponent(aviso.titulo)}&body=${encodeURIComponent(aviso.titulo)}`;
    }
  }

  private setState(aviso: AvisoClinico, estado: EstadoAvisoClinico): void {
    this.service.cambiarEstado(aviso.id, estado).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: updated => this.replace(updated),
      error: error => this.actionError.set(httpErrorMessage(error, 'No se pudo actualizar el estado.'))
    });
  }

  private replace(updated: AvisoClinico): void {
    this.avisos.update(current =>
      current.map(aviso => aviso.id === updated.id ? updated : aviso));
  }
}
