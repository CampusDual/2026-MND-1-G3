import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EMPTY, catchError, debounceTime, distinctUntilChanged, switchMap } from 'rxjs';
import { GanaderiaHierarchyService } from '../core/ganaderia-hierarchy.service';
import { GanaderiaFarmSummary } from '../core/ganaderia-hierarchy.models';
import { GanaderiaHudStateService } from '../core/ganaderia-hud-state.service';

/** Directorio compacto y paginado de explotaciones ganaderas. */
@Component({
  selector: 'app-ganaderia-directorio',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './ganaderia-directorio.component.html',
  styleUrl: './ganaderia-directorio.component.scss'
})
export class GanaderiaDirectorioComponent {
  private readonly service = inject(GanaderiaHierarchyService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly hud = inject(GanaderiaHudStateService);

  readonly searchControl = this.fb.control('');
  readonly loading = signal(false);
  readonly error = signal('');
  readonly farms = signal<GanaderiaFarmSummary[]>([]);
  readonly page = signal(0);
  readonly size = signal(20);
  readonly totalElements = signal(0);
  readonly totalPages = signal(0);

  constructor() {
    this.hud.activeFarmId.set(null);
    this.hud.activeFarmName.set('');
    this.hud.activeGroupLabel.set('');
    this.hud.selectedIds.set([]);
    const initialSearch = this.route.snapshot.queryParamMap.get('search') ?? '';
    const initialPage = Number(this.route.snapshot.queryParamMap.get('page') ?? '0');
    this.searchControl.setValue(initialSearch);
    this.page.set(Number.isFinite(initialPage) && initialPage >= 0 ? initialPage : 0);
    this.load(initialSearch);
    this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(search => {
        this.page.set(0);
        void this.router.navigate([], {
          relativeTo: this.route,
          queryParams: { search: search || null, page: 0 },
          queryParamsHandling: 'merge'
        });
        this.loading.set(true);
        return this.service.findFarms(search, 0, this.size()).pipe(
          catchError(() => {
            this.loading.set(false);
            this.error.set('No se pudo cargar el directorio de explotaciones.');
            return EMPTY;
          })
        );
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: farms => {
        this.updateFarmPage(farms.content, farms.totalElements, farms.totalPages);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  retry(): void {
    this.load(this.searchControl.value);
  }

  setPage(page: number): void {
    if (page < 0 || (this.totalPages() > 0 && page >= this.totalPages())) return;
    this.page.set(page);
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { search: this.searchControl.value || null, page },
      queryParamsHandling: 'merge'
    });
    this.load(this.searchControl.value);
  }

  private load(search: string): void {
    this.loading.set(true);
    this.error.set('');
    this.service.findFarms(search, this.page(), this.size()).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: farms => {
        this.updateFarmPage(farms.content, farms.totalElements, farms.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('No se pudo cargar el directorio de explotaciones.');
      }
    });
  }

  private updateFarmPage(
    farms: GanaderiaFarmSummary[],
    totalElements: number,
    totalPages: number
  ): void {
    this.farms.set(farms);
    this.totalElements.set(totalElements);
    this.totalPages.set(totalPages);
    this.hud.visibleFarms.set(totalElements);
    this.hud.totalHeads.set(farms.reduce((sum, farm) => sum + farm.totalHeads, 0));
    this.hud.activeAlerts.set(farms.reduce((sum, farm) => sum + farm.activeAlerts, 0));
    this.hud.pendingActions.set(farms.reduce((sum, farm) => sum + farm.pendingActions, 0));
  }
}
