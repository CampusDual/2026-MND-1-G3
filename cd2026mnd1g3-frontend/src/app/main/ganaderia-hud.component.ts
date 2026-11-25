import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterOutlet } from '@angular/router';
import { GanaderiaHudStateService } from '../core/ganaderia-hud-state.service';
import { GanaderiaOfflineService } from '../core/ganaderia-offline.service';
import { GanaderiaScannerService } from '../core/ganaderia-scanner.service';
import { GanaderiaHierarchyService } from '../core/ganaderia-hierarchy.service';
import { AlertService } from '../core/alert.service';
import { AlertaSistema, Farm, LivestockService } from '../core/livestock.service';

interface AlertFarmGroup {
  farmId: number;
  farmName: string;
  alerts: AlertaSistema[];
}

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
  private readonly alertService = inject(AlertService);
  private readonly livestock = inject(LivestockService);
  readonly connectionState = this.alertService.connected.asObservable();

  readonly online = signal(navigator.onLine);
  readonly scanInput = signal('');
  readonly quickDetail = signal('');
  readonly toast = signal('');
  readonly lastScannedAnimalId = signal<number | null>(null);
  readonly lastScannedFarmId = signal<number | null>(null);
  readonly alertsPanelOpen = signal(false);
  readonly alertList = signal<AlertaSistema[]>([]);
  readonly farmNames = signal<Record<number, string>>({});
  readonly unreadAlerts = computed(() => this.alertList().filter(alert => !alert.leida).length);
  readonly alertGroups = computed<AlertFarmGroup[]>(() => {
    const groups = new Map<number, AlertFarmGroup>();
    for (const alert of this.alertList()) {
      if (alert.explotacionId == null) continue;
      const farmId = alert.explotacionId;
      const group = groups.get(farmId) ?? {
        farmId,
        farmName: this.farmNames()[farmId] ?? `Explotación ${farmId}`,
        alerts: []
      };
      group.alerts.push(alert);
      groups.set(farmId, group);
    }
    return [...groups.values()];
  });

  constructor() {
    this.alertService.connect();
    this.alertService.alerts.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(alerts => this.alertList.set(alerts));
    this.livestock.alerts().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: alerts => this.alertService.setInitialAlerts(alerts),
      error: () => this.toast.set('No se pudieron cargar las alertas.')
    });
    this.livestock.farms().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: farms => this.farmNames.set(this.toFarmNames(farms)),
      error: () => this.toast.set('No se pudieron cargar los nombres de las explotaciones.')
    });
    this.offline.synchronizedOperationIds.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(ids => {
      if (this.pendingSyncOperationId && ids.includes(this.pendingSyncOperationId)) {
        this.toast.set('Incidencia sincronizada.');
        this.pendingSyncOperationId = null;
      }
    });
    this.offline.syncErrors.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      if (this.pendingSyncOperationId) {
        this.toast.set('No se pudo sincronizar la incidencia; permanece en la cola.');
      }
    });
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
        this.lastScannedAnimalId.set(animal.id);
        this.lastScannedFarmId.set(farmId);
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
    let operationId: string;
    try {
      operationId = this.offline.enqueue({
        farmId,
        tipoOperacion: 'INCIDENCIA',
        detalle: this.quickDetail() || 'Incidencia registrada desde campo',
        animalId: this.lastScannedFarmId() === farmId
          ? this.lastScannedAnimalId() ?? undefined
          : undefined
      });
    } catch {
      this.toast.set('No se pudo guardar la incidencia en la cola local.');
      return;
    }
    if (!navigator.onLine) {
      this.toast.set('Incidencia guardada pendiente de sincronizar.');
      return;
    }
    this.pendingSyncOperationId = operationId;
    this.toast.set('Enviando incidencia y esperando confirmación...');
    this.offline.syncPending();
  }

  toggleAlertsPanel(): void {
    this.alertsPanelOpen.update(open => !open);
  }

  runDailyReview(): void {
    this.livestock.runDailyReview().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => this.toast.set('Revisión diaria ejecutada correctamente.'),
      error: () => this.toast.set('No se pudo ejecutar la revisión diaria.')
    });
  }

  private pendingSyncOperationId: string | null = null;

  private toFarmNames(farms: Farm[]): Record<number, string> {
    const names: Record<number, string> = {};
    for (const farm of farms) {
      if (farm.id !== undefined) names[farm.id] = farm.name;
    }
    return names;
  }
}
