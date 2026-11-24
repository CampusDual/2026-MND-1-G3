import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterOutlet } from '@angular/router';
import { GanaderiaHudStateService } from '../core/ganaderia-hud-state.service';
import { GanaderiaOfflineService } from '../core/ganaderia-offline.service';
import { GanaderiaScannerService } from '../core/ganaderia-scanner.service';
import { GanaderiaHierarchyService } from '../core/ganaderia-hierarchy.service';

/** Shell HUD único para las vistas de explotaciones ganaderas. */
@Component({
  selector: 'app-ganaderia-hud',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule, RouterLink, RouterOutlet],
  templateUrl: './ganaderia-hud.component.html',
  styleUrl: './ganaderia-hud.component.scss'
})
export class GanaderiaHudComponent {
  readonly state = inject(GanaderiaHudStateService);
  readonly offline = inject(GanaderiaOfflineService);
  readonly scanner = inject(GanaderiaScannerService);
  private readonly hierarchy = inject(GanaderiaHierarchyService);
  private readonly destroyRef = inject(DestroyRef);

  readonly online = signal(navigator.onLine);
  readonly scanInput = signal('');
  readonly bulkSubtype = signal('');
  readonly bulkLocation = signal('');
  readonly quickDetail = signal('');
  readonly toast = signal('');
  readonly bulkState = signal<'SANO' | 'EN_TRATAMIENTO' | 'ENFERMO'>('SANO');
  readonly bulkProtocolTitle = signal('');
  readonly bulkProtocolType = signal<'VACUNACION' | 'TRATAMIENTO_MEDICO' | 'PARTO' | 'REVISION_PERIODICA'>('VACUNACION');

  constructor() {
    window.addEventListener('offline', () => this.online.set(false));
    window.addEventListener('online', () => {
      this.online.set(true);
      this.offline.syncPending();
    });
  }

  toggleMode(): void {
    this.state.mode.update(mode => mode === 'clinica' ? 'campo' : 'clinica');
  }

  scanEnter(): void {
    const crotal = this.scanInput().trim().toUpperCase();
    if (!crotal) return;
    const farmId = this.state.activeFarmId();
    if (!farmId) {
      this.toast.set('Selecciona una explotación antes de escanear.');
      return;
    }
    this.hierarchy.findByCrotal(farmId, crotal)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: animal => {
        this.state.selectedIds.update(ids =>
          ids.includes(animal.id) ? ids : [...ids, animal.id]);
        this.toast.set(`Crotal ${crotal} encontrado y seleccionado.`);
        this.quickDetail.set(`Crotal ${crotal} seleccionado`);
        this.scanInput.set('');
      },
      error: () => this.toast.set('No se pudo consultar el lector RFID.')
    });
  }

  addOfflineOperation(): void {
    const farmId = this.state.activeFarmId();
    if (!farmId) {
      this.toast.set('Selecciona una explotación antes de registrar una incidencia.');
      return;
    }
    this.offline.enqueue({
      farmId,
      tipoOperacion: 'INCIDENCIA',
      detalle: this.quickDetail() || 'Incidencia registrada desde campo',
      animalId: undefined
    });
    this.toast.set(navigator.onLine
      ? 'Incidencia registrada y sincronizada.'
      : 'Incidencia guardada pendiente de sincronizar.');
    if (navigator.onLine) this.offline.syncPending();
  }

  applyBulkSubtype(): void {
    this.applyBulk({ subtipo: this.bulkSubtype() });
  }

  applyBulkLocation(): void {
    this.applyBulk({ ubicacion: this.bulkLocation() });
  }

  applyBulkState(): void {
    const farmId = this.state.activeFarmId();
    const ids = this.state.selectedIds();
    if (!farmId || ids.length === 0) return;
    if (!window.confirm(`¿Cambiar el estado sanitario de ${ids.length} animales?`)) return;
    this.hierarchy.aplicarEstadoMasivo(farmId, {
      animalIds: ids,
      estadoSanitario: this.bulkState()
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: updated => this.toast.set(`Estado aplicado a ${updated} animales.`),
      error: () => this.toast.set('No se pudo cambiar el estado sanitario.')
    });
  }

  applyBulkProtocol(): void {
    const farmId = this.state.activeFarmId();
    const ids = this.state.selectedIds();
    const title = this.bulkProtocolTitle().trim();
    if (!farmId || ids.length === 0 || !title) return;
    if (!window.confirm(`¿Aplicar ${title} a ${ids.length} animales?`)) return;
    this.hierarchy.aplicarProtocoloMasivo(farmId, {
      animalIds: ids,
      eventType: this.bulkProtocolType(),
      title,
      applicationDate: new Date().toISOString().slice(0, 10),
      withdrawalDays: 0
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: created => this.toast.set(`Protocolo aplicado a ${created} animales.`),
      error: () => this.toast.set('No se pudo aplicar el protocolo.')
    });
  }

  private applyBulk(changes: { subtipo?: string; ubicacion?: string }): void {
    const farmId = this.state.activeFarmId();
    const ids = this.state.selectedIds();
    if (!farmId || ids.length === 0) return;
    this.hierarchy.clasificarAnimales(farmId, {
      animalIds: ids,
      subtipo: changes.subtipo || undefined,
      ubicacion: changes.ubicacion || undefined
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => this.toast.set(`Acción aplicada a ${ids.length} animales.`),
      error: () => this.toast.set('No se pudo aplicar la acción masiva.')
    });
  }
}
