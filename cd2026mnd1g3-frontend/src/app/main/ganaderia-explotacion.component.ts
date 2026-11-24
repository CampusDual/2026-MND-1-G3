import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { GanaderiaHierarchyService } from '../core/ganaderia-hierarchy.service';
import {
  GanaderiaAnimal,
  GanaderiaAnimalPage,
  GanaderiaGroup,
  GanaderiaGroupType
} from '../core/ganaderia-hierarchy.models';
import { OpcionesClasificacion } from '../core/ganaderia-hierarchy.models';
import { GanaderiaHudStateService } from '../core/ganaderia-hud-state.service';

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

  readonly farmId = signal(Number(this.route.snapshot.paramMap.get('farmId')));
  readonly farmName = signal('Explotación');
  readonly farmHeads = signal(0);
  readonly groupType = signal<GanaderiaGroupType>('SUBTIPO');
  readonly groups = signal<GanaderiaGroup[]>([]);
  readonly selectedGroup = signal<GanaderiaGroup | null>(null);
  readonly animals = signal<GanaderiaAnimal[]>([]);
  readonly page = signal(0);
  readonly size = signal(20);
  readonly totalElements = signal(0);
  readonly totalPages = signal(0);
  readonly search = signal('');
  readonly selectedIds = signal<number[]>([]);
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

  readonly addForm = this.fb.group({
    crotal: ['', [Validators.required, Validators.maxLength(40)]],
    nombre: ['', Validators.maxLength(100)],
    subtipo: this.fb.control(''),
    ubicacion: this.fb.control(''),
    subtipoPersonalizado: this.fb.control(''),
    ubicacionPersonalizada: this.fb.control(''),
    estadoSanitario: this.fb.control<'SANO' | 'EN_TRATAMIENTO' | 'ENFERMO'>('SANO'),
    cabezas: this.fb.control(1, [Validators.required, Validators.min(1)])
  });
  readonly editSubtipo = signal('');
  readonly editUbicacion = signal('');
  readonly editEstado = signal<'SANO' | 'EN_TRATAMIENTO' | 'ENFERMO'>('SANO');

  constructor() {
    this.hud.activeFarmId.set(this.farmId());
    this.hud.selectedIds.set([]);
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
    this.loadGroups();
    this.loadOptions();
    this.loadFarmName();
  }

  private loadFarmName(): void {
    this.service.findFarms('', 0, 100).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(page => {
      const farm = page.content.find(item => item.farmId === this.farmId());
      if (farm) {
        this.farmName.set(farm.name);
        this.farmHeads.set(farm.totalHeads);
        this.hud.activeFarmName.set(farm.name);
      }
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
    this.selectedIds.set([]);
    this.hud.selectedIds.set([]);
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

  private loadOptions(): void {
    this.service.findOpcionesClasificacion(this.farmId()).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: opciones => this.opciones.set(opciones),
      error: () => this.error.set('No se pudieron cargar las opciones de clasificación.')
    });
  }

  private refreshAfterMutation(): void {
    this.loadGroups();
    this.loadOptions();
    if (this.selectedGroup()) {
      this.loadAnimals();
    }
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
    this.loadGroups();
  }

  selectGroup(group: GanaderiaGroup): void {
    this.selectedGroup.set(group);
    this.hud.activeGroupLabel.set(group.label);
    this.page.set(0);
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { type: this.groupType(), group: group.groupId, page: 0 },
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
    this.selectedIds.update(ids =>
      ids.includes(animalId)
        ? ids.filter(id => id !== animalId)
        : [...ids, animalId]);
    this.hud.selectedIds.set(this.selectedIds());
  }

  openDetail(animal: GanaderiaAnimal): void {
    this.selectedAnimal.set(animal);
    this.editSubtipo.set(animal.subtipo ?? '');
    this.editUbicacion.set(animal.ubicacion ?? '');
  }

  closeDetail(): void {
    this.selectedAnimal.set(null);
  }

  retry(): void {
    if (this.selectedGroup()) {
      this.loadAnimals();
    } else {
      this.loadGroups();
    }
  }

  private loadGroups(): void {
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
      },
      error: () => {
        this.loading.set(false);
        this.error.set('No se pudieron cargar los grupos de la explotación.');
      }
    });
  }
}
