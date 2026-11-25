import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { GanaderiaHierarchyService } from '../core/ganaderia-hierarchy.service';
import {
  GanaderiaAnimal,
  GanaderiaAnimalPage,
  GanaderiaGroup,
  GanaderiaGroupType,
  GanaderiaEstadoSanitario
} from '../core/ganaderia-hierarchy.models';
import { OpcionesClasificacion } from '../core/ganaderia-hierarchy.models';
import { GanaderiaHudStateService } from '../core/ganaderia-hud-state.service';
import {
  AnimalHealthStatus,
  AlertaSistema,
  LivestockService,
  LoteStock,
  Medicamento,
  MovimientoInventario,
  ProtocolEvent,
  ProtocolEventType
} from '../core/livestock.service';
import { AlertService } from '../core/alert.service';

type FarmTab = 'animales' | 'protocolos' | 'alertas';

const HEALTH_STATUSES: AnimalHealthStatus[] = [
  'SANO', 'EN_TRATAMIENTO', 'EN_RETIRADA_MEDICAMENTO', 'ENFERMO', 'CUARENTENA'
];

/** Espacio aislado de explotacion y listado paginado del grupo seleccionado. */
@Component({
  selector: 'app-ganaderia-explotacion',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink],
  templateUrl: './ganaderia-explotacion.component.html',
  styleUrl: './ganaderia-explotacion.component.scss'
})
export class GanaderiaExplotacionComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(GanaderiaHierarchyService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly hud = inject(GanaderiaHudStateService);
  private readonly livestock = inject(LivestockService);
  private readonly alertService = inject(AlertService);
  private readonly searchChanges = new Subject<string>();

  readonly farmId = signal(Number(this.route.snapshot.paramMap.get('farmId')));
  readonly farmName = signal('Explotación');
  readonly farmHeads = signal(0);
  readonly tab = signal<FarmTab>('animales');
  readonly groupType = signal<GanaderiaGroupType>('SUBTIPO');
  readonly groups = signal<GanaderiaGroup[]>([]);
  readonly selectedGroup = signal<GanaderiaGroup | null>(null);
  readonly animals = signal<GanaderiaAnimal[]>([]);
  readonly page = signal(0);
  readonly size = signal(20);
  readonly totalElements = signal(0);
  readonly totalPages = signal(0);
  readonly search = signal('');
  readonly selectedIds = this.hud.selectedIds;
  readonly selectedAnimal = signal<GanaderiaAnimal | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly addDrawerOpen = signal(false);
  readonly toast = signal('');
  readonly opciones = signal<OpcionesClasificacion>({
    subtipos: ['Sin clasificar'],
    ubicaciones: ['Sin ubicación'],
    subtiposRecomendados: ['ORDEÑO', 'RECRIA', 'TERNEROS', 'SECAS', 'CEBO', 'MADRES', 'TRANSICION'],
    ubicacionesRecomendadas: ['NAVE_1', 'NAVE_2', 'CORRAL_A', 'PARCELA_NORTE', 'ENFERMERIA']
  });
  readonly subtypeAction = signal('');
  readonly locationAction = signal('');
  readonly bulkHealthStatus = signal<GanaderiaEstadoSanitario>('SANO');
  readonly bulkProtocolType = signal<'VACUNACION' | 'REVISION_PERIODICA'>('VACUNACION');
  readonly bulkProtocolTitle = signal('');
  readonly protocolTitle = signal('');
  readonly protocolType = signal<ProtocolEventType>('TRATAMIENTO_MEDICO');
  readonly protocolDate = signal(new Date().toISOString().slice(0, 10));
  readonly protocolMedicationId = signal<number | null>(null);
  readonly protocolLotId = signal<number | null>(null);
  readonly protocolQuantity = signal<number | null>(null);
  readonly protocolWithdrawalDays = signal<number | null>(null);
  readonly protocolNextCycleDays = signal<number | null>(0);
  readonly protocolAnimalId = signal<number | null>(null);
  readonly protocolAnimalSearch = signal('');
  readonly protocolAnimalMatch = signal<GanaderiaAnimal | null>(null);
  readonly protocols = signal<ProtocolEvent[]>([]);
  readonly medications = signal<Medicamento[]>([]);
  readonly lots = signal<LoteStock[]>([]);
  readonly movements = signal<MovimientoInventario[]>([]);
  readonly alerts = signal<AlertaSistema[]>([]);
  readonly selectedHealthStatus = signal<AnimalHealthStatus>('SANO');
  readonly statusNotes = signal('');
  readonly farmAlerts = computed(() => this.alerts().filter(alert => alert.explotacionId === this.farmId()));
  readonly protocolAnimals = computed(() => {
    const available = [...this.animals()];
    const match = this.protocolAnimalMatch();
    if (match && !available.some(animal => animal.id === match.id)) available.push(match);
    return available;
  });
  readonly healthStatuses = HEALTH_STATUSES;

  readonly addForm = this.fb.group({
    crotal: ['', [Validators.required, Validators.maxLength(40)]],
    nombre: ['', Validators.maxLength(100)],
    subtipo: this.fb.control(''),
    ubicacion: this.fb.control(''),
    subtipoPersonalizado: this.fb.control(''),
    ubicacionPersonalizada: this.fb.control(''),
    estadoSanitario: this.fb.control<GanaderiaEstadoSanitario>('SANO'),
    cabezas: this.fb.control(1, [Validators.required, Validators.min(1)])
  });
  readonly editSubtipo = signal('');
  readonly editUbicacion = signal('');

  constructor() {
    this.hud.activeFarmId.set(this.farmId());
    this.hud.selectedIds.set([]);
    this.alertService.alerts.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(alerts => this.alerts.set(alerts));
    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
      const tab = params.get('tab');
      if (tab === 'animales' || tab === 'protocolos' || tab === 'alertas') {
        this.tab.set(tab);
      } else {
        this.tab.set('animales');
      }
    });
    const type = this.route.snapshot.queryParamMap.get('type');
    if (type === 'UBICACION' || type === 'ESTADO_SANITARIO' || type === 'SUBTIPO') {
      this.groupType.set(type);
    }
    const initialPage = Number(this.route.snapshot.queryParamMap.get('page') ?? '0');
    this.page.set(Number.isFinite(initialPage) && initialPage >= 0 ? initialPage : 0);
    this.search.set(this.route.snapshot.queryParamMap.get('search') ?? '');
    const groupId = this.route.snapshot.queryParamMap.get('group');
    if (groupId) {
      this.selectedGroup.set({
        groupId,
        label: groupId,
        heads: 0,
        percentage: 0,
        activeAlerts: 0,
        pendingActions: 0
      });
    }
    this.searchChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(search => {
      this.page.set(0);
      void this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { search: search || null, page: 0 },
        queryParamsHandling: 'merge'
      });
      this.loadAnimals();
    });
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
      const farmId = Number(params.get('farmId'));
      if (!Number.isInteger(farmId) || farmId <= 0) {
        this.error.set('La explotación indicada no es válida.');
        return;
      }
      if (farmId !== this.farmId()) {
        this.farmId.set(farmId);
        this.hud.activeFarmId.set(farmId);
        this.hud.selectedIds.set([]);
        this.selectedGroup.set(null);
      }
      this.loadGroups(this.route.snapshot.queryParamMap.get('group'));
      this.loadOptions();
      this.loadFarmName();
      this.loadProtocols();
      this.loadInventory();
    });
  }

  private loadFarmName(): void {
    this.livestock.farmById(this.farmId()).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: farm => {
        this.farmName.set(farm.name);
        this.hud.activeFarmName.set(farm.name);
      },
      error: () => this.error.set('No se pudo cargar la explotación.')
    });
  }

  setTab(tab: FarmTab): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { tab },
      queryParamsHandling: 'merge'
    });
  }

  onAnimalSearch(search: string): void {
    this.search.set(search);
    this.searchChanges.next(search);
  }

  searchProtocolAnimal(): void {
    const crotal = this.protocolAnimalSearch().trim();
    if (!crotal) return;
    this.service.findByCrotal(this.farmId(), crotal).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: animal => {
        this.protocolAnimalMatch.set(animal);
        this.protocolAnimalId.set(animal.id);
        this.error.set('');
      },
      error: () => {
        this.protocolAnimalMatch.set(null);
        this.protocolAnimalId.set(null);
        this.error.set('No se encontró ese crotal en esta explotación.');
      }
    });
  }

  stockOfLot(lotId: number): number {
    return this.movements().filter(movement => movement.loteStockId === lotId)
      .reduce((total, movement) => total + movement.cantidad, 0);
  }

  availableLots(): LoteStock[] {
    return this.lots().filter(lot =>
      (this.protocolMedicationId() === null || lot.medicamentoId === this.protocolMedicationId())
      && this.stockOfLot(lot.id) > 0);
  }

  onMedicationChange(medicationId: number | null): void {
    this.protocolMedicationId.set(medicationId);
    this.protocolLotId.set(null);
    this.protocolQuantity.set(null);
  }

  createProtocol(): void {
    const title = this.protocolTitle().trim();
    const medicationId = this.protocolMedicationId();
    const lotId = this.protocolLotId();
    const quantity = this.protocolQuantity();
    const withdrawalDays = this.protocolWithdrawalDays();
    if (!title) {
      this.error.set('El título del protocolo es obligatorio.');
      return;
    }
    if ((withdrawalDays !== null && withdrawalDays < 0)
      || (this.protocolNextCycleDays() !== null && (this.protocolNextCycleDays() ?? 0) < 0)) {
      this.error.set('Los días de retirada y del próximo ciclo no pueden ser negativos.');
      return;
    }
    if (medicationId !== null
      && (lotId === null || quantity === null || quantity <= 0 || withdrawalDays === null || withdrawalDays < 0)) {
      this.error.set('Con medicamento, selecciona lote, cantidad válida y días de retirada.');
      return;
    }
    if (medicationId !== null && lotId !== null && quantity !== null && quantity > this.stockOfLot(lotId)) {
      this.error.set('La cantidad supera el stock disponible del lote.');
      return;
    }
    const protocol: ProtocolEvent = {
      eventType: this.protocolType(),
      title,
      applicationDate: this.protocolDate(),
      withdrawalDays: withdrawalDays ?? 0,
      nextCycleDays: this.protocolNextCycleDays() ?? 0,
      status: 'PROGRAMADO',
      farmId: this.farmId(),
      animalId: this.protocolAnimalId() ?? undefined,
      loteStockId: medicationId === null ? undefined : lotId ?? undefined,
      cantidadUsada: medicationId === null ? undefined : quantity ?? undefined
    };
    this.error.set('');
    this.livestock.createProtocol(protocol).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.protocolTitle.set('');
        this.protocolMedicationId.set(null);
        this.protocolLotId.set(null);
        this.protocolQuantity.set(null);
        this.protocolWithdrawalDays.set(null);
        this.protocolNextCycleDays.set(0);
        this.protocolAnimalId.set(null);
        this.protocolAnimalSearch.set('');
        this.protocolAnimalMatch.set(null);
        this.toast.set('Protocolo registrado correctamente.');
        this.refreshAfterMutation();
      },
      error: () => this.error.set('No se pudo registrar el protocolo ni descontar el stock.')
    });
  }

  applyQuickProtocol(): void {
    const ids = this.selectedIds();
    const title = this.bulkProtocolTitle().trim();
    if (ids.length === 0 || !title) {
      this.error.set('Selecciona animales y escribe el título del protocolo.');
      return;
    }
    this.service.aplicarProtocoloMasivo(this.farmId(), {
      animalIds: ids,
      eventType: this.bulkProtocolType(),
      title,
      applicationDate: new Date().toISOString().slice(0, 10),
      withdrawalDays: 0
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: count => {
        this.toast.set(`Actuación aplicada a ${count} animales.`);
        this.bulkProtocolTitle.set('');
        this.refreshAfterMutation();
      },
      error: () => this.error.set('No se pudo aplicar la vacunación o revisión.')
    });
  }

  openAddDrawer(): void {
    this.addDrawerOpen.set(true);
  }

  closeAddDrawer(): void {
    this.addDrawerOpen.set(false);
  }

  saveAnimal(addAnother = false): void {
    if (this.addForm.invalid) {
      this.addForm.markAllAsTouched();
      this.error.set('Completa el crotal y un número válido de cabezas.');
      return;
    }
    const value = this.addForm.getRawValue();
    this.service.altaAnimal(this.farmId(), {
      crotal: value.crotal.trim().toUpperCase(),
      nombre: value.nombre.trim() || undefined,
      subtipo: value.subtipoPersonalizado.trim()
        || value.subtipo || undefined,
      ubicacion: value.ubicacionPersonalizada.trim()
        || value.ubicacion || undefined,
      estadoSanitario: value.estadoSanitario,
      cabezas: value.cabezas
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.toast.set('Animal o lote registrado correctamente.');
        this.refreshAfterMutation();
        if (addAnother) {
          this.addForm.reset({
            crotal: '',
            nombre: '',
            subtipo: value.subtipo,
            ubicacion: value.ubicacion,
            estadoSanitario: value.estadoSanitario,
            cabezas: value.cabezas
          });
        } else {
          this.closeAddDrawer();
        }
      },
      error: () => this.error.set('No se pudo registrar el animal o lote.')
    });
  }

  aplicarSubtipoMasivo(): void {
    const ids = this.selectedIds();
    if (!ids.length || !this.subtypeAction()) return;
    this.service.clasificarAnimales(this.farmId(), {
      animalIds: ids,
      subtipo: this.subtypeAction()
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.toast.set(`Subtipo aplicado a ${ids.length} animales.`);
        this.refreshAfterMutation();
      },
      error: () => this.error.set('No se pudo aplicar el subtipo.')
    });
  }

  aplicarUbicacionMasiva(): void {
    const ids = this.selectedIds();
    if (!ids.length || !this.locationAction()) return;
    this.service.clasificarAnimales(this.farmId(), {
      animalIds: ids,
      ubicacion: this.locationAction()
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.toast.set(`Ubicación aplicada a ${ids.length} animales.`);
        this.refreshAfterMutation();
      },
      error: () => this.error.set('No se pudo aplicar la ubicación.')
    });
  }

  deseleccionar(): void {
    this.hud.selectedIds.set([]);
  }

  aplicarEstadoMasivo(): void {
    const ids = this.selectedIds();
    if (!ids.length) return;
    this.service.aplicarEstadoMasivo(this.farmId(), {
      animalIds: ids,
      estadoSanitario: this.bulkHealthStatus()
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: count => {
        this.toast.set(`Estado aplicado a ${count} animales.`);
        this.refreshAfterMutation();
      },
      error: () => this.error.set('No se pudo aplicar el estado sanitario.')
    });
  }

  clasificarIndividual(animal: GanaderiaAnimal): void {
    this.service.clasificarAnimales(this.farmId(), {
      animalIds: [animal.id],
      subtipo: this.editSubtipo().trim() || undefined,
      ubicacion: this.editUbicacion().trim() || undefined
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.toast.set('Clasificación individual actualizada.');
        this.refreshAfterMutation();
      },
      error: () => this.error.set('No se pudo actualizar la clasificación.')
    });
  }

  updateAnimalStatus(animal: GanaderiaAnimal): void {
    this.livestock.updateAnimalStatus(animal.id, this.selectedHealthStatus(), this.statusNotes())
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: () => {
          this.toast.set('Estado sanitario actualizado.');
          this.statusNotes.set('');
          this.selectedAnimal.update(current => current
            ? { ...current, estadoSanitario: this.selectedHealthStatus(), healthStatus: this.selectedHealthStatus() }
            : current);
          this.refreshAfterMutation();
        },
        error: () => this.error.set('No se pudo actualizar el estado sanitario del animal.')
      });
  }

  markAlertRead(alert: AlertaSistema): void {
    this.livestock.markAlertRead(alert.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: updated => this.alertService.setInitialAlerts([updated]),
      error: () => this.error.set('No se pudo marcar la alerta como leída.')
    });
  }

  confirmAlert(alert: AlertaSistema): void {
    this.livestock.confirmAlert(alert.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: updated => this.alertService.setInitialAlerts([updated]),
      error: () => this.error.set('No se pudo confirmar la ejecución en granja.')
    });
  }

  private loadOptions(): void {
    this.service.findOpcionesClasificacion(this.farmId()).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: opciones => this.opciones.set(opciones),
      error: () => this.error.set('No se pudieron cargar las opciones de clasificación.')
    });
  }

  private refreshAfterMutation(): void {
    this.hud.selectedIds.set([]);
    this.loadGroups(this.selectedGroup()?.groupId ?? null);
    this.loadOptions();
    this.loadProtocols();
    this.loadInventory();
  }

  setGroupType(type: GanaderiaGroupType): void {
    this.groupType.set(type);
    this.selectedGroup.set(null);
    this.animals.set([]);
    this.hud.activeGroupLabel.set('');
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { type, group: null, page: 0, search: null },
      queryParamsHandling: 'merge'
    });
    this.page.set(0);
    this.search.set('');
    this.hud.selectedIds.set([]);
    this.loadGroups(null);
  }

  selectGroup(group: GanaderiaGroup): void {
    this.selectedGroup.set(group);
    this.hud.activeGroupLabel.set(group.label);
    this.page.set(0);
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { type: this.groupType(), group: group.groupId, page: 0, search: this.search() || null },
      queryParamsHandling: 'merge'
    });
    this.loadAnimals();
  }

  loadAnimals(): void {
    const group = this.selectedGroup();
    if (!group) return;
    this.loading.set(true);
    this.error.set('');
    this.service.findAnimals(
      this.farmId(), group.groupId, this.groupType(),
      this.search(), this.page(), this.size(), 'earTag,asc'
    ).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (page: GanaderiaAnimalPage) => {
        this.animals.set(page.content);
        this.totalElements.set(page.totalElements);
        this.totalPages.set(page.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('No se pudo cargar el grupo seleccionado.');
      }
    });
  }

  setPage(page: number): void {
    if (page < 0 || (this.totalPages() > 0 && page >= this.totalPages())) return;
    this.page.set(page);
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page, search: this.search() || null },
      queryParamsHandling: 'merge'
    });
    this.loadAnimals();
  }

  toggleSelected(animalId: number): void {
    this.hud.selectedIds.update(ids =>
      ids.includes(animalId)
        ? ids.filter(id => id !== animalId)
        : [...ids, animalId]);
  }

  openDetail(animal: GanaderiaAnimal): void {
    this.selectedAnimal.set(animal);
    this.editSubtipo.set(animal.subtipo ?? '');
    this.editUbicacion.set(animal.ubicacion ?? '');
    this.selectedHealthStatus.set(animal.estadoSanitario ?? 'SANO');
    this.statusNotes.set(animal.notas ?? '');
  }

  closeDetail(): void {
    this.selectedAnimal.set(null);
  }

  retry(): void {
    this.error.set('');
    if (this.tab() === 'protocolos') {
      this.loadProtocols();
      this.loadInventory();
    } else if (this.tab() === 'alertas') {
      this.livestock.alerts().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: alerts => this.alertService.setInitialAlerts(alerts),
        error: () => this.error.set('No se pudieron cargar las alertas.')
      });
    } else if (this.selectedGroup()) {
      this.loadAnimals();
    } else {
      this.loadGroups(null);
    }
  }

  private loadGroups(preferredGroupId: string | null = null): void {
    this.loading.set(true);
    this.error.set('');
    this.service.findGroups(this.farmId(), this.groupType()).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: groups => {
        this.groups.set(groups);
        this.loading.set(false);
        const sum = groups.reduce((total, group) => total + group.heads, 0);
        this.farmHeads.set(sum);
        this.hud.totalHeads.set(sum);
        this.hud.activeAlerts.set(groups.reduce((total, group) => total + group.activeAlerts, 0));
        this.hud.pendingActions.set(groups.reduce((total, group) => total + group.pendingActions, 0));
        const selected = preferredGroupId
          ? groups.find(group => group.groupId === preferredGroupId)
          : undefined;
        this.selectedGroup.set(selected ?? null);
        this.hud.activeGroupLabel.set(selected?.label ?? '');
        if (selected) this.loadAnimals();
      },
      error: () => {
        this.loading.set(false);
        this.error.set('No se pudieron cargar los grupos de la explotación.');
      }
    });
  }

  private loadProtocols(): void {
    this.livestock.protocols().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: protocols => this.protocols.set(protocols.filter(protocol => protocol.farmId === this.farmId())),
      error: () => this.error.set('No se pudo cargar el historial de protocolos.')
    });
  }

  private loadInventory(): void {
    this.livestock.medicamentos().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: medications => this.medications.set(medications),
      error: () => this.error.set('No se pudo cargar el catálogo de medicamentos.')
    });
    this.livestock.lotes().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: lots => this.lots.set(lots),
      error: () => this.error.set('No se pudieron cargar los lotes de inventario.')
    });
    this.livestock.movimientos().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: movements => this.movements.set(movements),
      error: () => this.error.set('No se pudo cargar el stock de inventario.')
    });
  }
}
