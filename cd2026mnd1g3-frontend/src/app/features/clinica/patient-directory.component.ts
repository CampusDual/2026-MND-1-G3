import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, switchMap, tap } from 'rxjs';
import { PatientDirectoryService, PatientFilters } from '../../core/patient-directory.service';
import { PatientDetail, PatientPage, PatientSort, PatientSpecies, PatientSummary } from '../../core/models/patient-directory.models';

type LoadState = 'loading' | 'data' | 'empty' | 'error';

@Component({
  selector: 'app-patient-directory',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule],
  templateUrl: './patient-directory.component.html',
  styleUrl: './patient-directory.component.scss'
})
export class PatientDirectoryComponent {
  private readonly service = inject(PatientDirectoryService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly searchTrigger = new Subject<void>();

  readonly state = signal<LoadState>('loading');
  readonly page = signal<PatientPage | null>(null);
  readonly selectedDetail = signal<PatientDetail | null>(null);
  readonly ownerName = signal('');
  readonly petName = signal('');
  readonly microchip = signal('');
  readonly withoutMicrochip = signal(false);
  readonly species = signal<PatientSpecies | null>(null);
  readonly breed = signal('');
  readonly currentPage = signal(0);
  readonly sort = signal<PatientSort>('name');
  readonly direction = signal<'asc' | 'desc'>('asc');

  readonly speciesOptions: readonly (PatientSpecies | null)[] = [null, 'PERRO', 'GATO', 'PAJARO', 'OTRO'];
  readonly breedsBySpecies: Readonly<Record<PatientSpecies, readonly string[]>> = {
    PERRO: ['Mestizo / Sin raza definida', 'Sin especificar', 'Labrador', 'Pastor Alemán', 'Chihuahua'],
    GATO: ['Mestizo / Sin raza definida', 'Sin especificar', 'Común Europeo', 'Persa', 'Siamés'],
    PAJARO: ['Mestizo / Sin raza definida', 'Sin especificar', 'Canario', 'Papilla', 'Periquito'],
    OTRO: ['Mestizo / Sin raza definida', 'Sin especificar', 'Conejo', 'Hurón', 'Tortuga', 'Cobaya']
  };

  constructor() {
    this.syncFromQueryParams();
    this.searchTrigger.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      tap(() => this.state.set('loading')),
      switchMap(() => this.service.search(this.filters())),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: page => {
        this.page.set(page);
        this.state.set(page.content.length === 0 ? 'empty' : 'data');
      },
      error: () => this.state.set('error')
    });
    this.load();
  }

  speciesLabel(species: PatientSpecies | null): string {
    return species ?? 'Todas';
  }

  breedCode(breed: string): string {
    if (!breed || breed === 'Sin especificar') return '__UNSPECIFIED__';
    if (breed.toLowerCase().includes('mestizo')) return '__MIXED__';
    return breed;
  }

  setSpecies(species: PatientSpecies | null): void {
    this.species.set(species);
    this.breed.set('');
    this.currentPage.set(0);
    this.persistAndLoad();
  }

  setBreed(breed: string): void {
    this.breed.set(breed);
    this.currentPage.set(0);
    this.persistAndLoad();
  }

  updateOwnerName(value: string): void {
    this.ownerName.set(value);
    this.currentPage.set(0);
    this.searchTrigger.next();
  }

  updatePetName(value: string): void {
    this.petName.set(value);
    this.currentPage.set(0);
    this.searchTrigger.next();
  }

  updateMicrochip(value: string): void {
    this.microchip.set(value);
    this.currentPage.set(0);
    this.searchTrigger.next();
  }

  setWithoutMicrochip(value: boolean): void {
    this.withoutMicrochip.set(value);
    if (value) this.microchip.set('');
    this.currentPage.set(0);
    this.persistAndLoad();
  }

  clearFilters(): void {
    this.ownerName.set('');
    this.petName.set('');
    this.microchip.set('');
    this.withoutMicrochip.set(false);
    this.species.set(null);
    this.breed.set('');
    this.currentPage.set(0);
    this.persistAndLoad();
  }

  changePage(page: number): void {
    if (page < 0) return;
    const totalPages = this.page()?.totalPages ?? 0;
    if (page >= totalPages) return;
    this.currentPage.set(page);
    this.persistAndLoad();
  }

  setSort(sort: PatientSort): void {
    if (this.sort() === sort) {
      this.direction.set(this.direction() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sort.set(sort);
      this.direction.set('asc');
    }
    this.persistAndLoad();
  }

  openPatient(patient: PatientSummary): void {
    this.service.detail(patient.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: detail => this.selectedDetail.set(detail),
      error: () => this.state.set('error')
    });
  }

  closeDrawer(): void {
    this.selectedDetail.set(null);
  }

  activeFilterCount(): number {
    return Number(Boolean(this.ownerName())) + Number(Boolean(this.petName()))
      + Number(Boolean(this.microchip()) || this.withoutMicrochip())
      + Number(Boolean(this.species())) + Number(Boolean(this.breed()));
  }

  load(): void {
    this.state.set('loading');
    const filters = this.filters();
    this.service.search(filters).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: page => {
        this.page.set(page);
        this.state.set(page.content.length === 0 ? 'empty' : 'data');
      },
      error: () => this.state.set('error')
    });
  }

  private filters(): PatientFilters {
    return {
      ownerName: this.ownerName(),
      petName: this.petName(),
      microchip: this.microchip(),
      withoutMicrochip: this.withoutMicrochip(),
      species: this.species(),
      breed: this.breedCode(this.breed()),
      page: this.currentPage(),
      size: 20,
      sort: this.sort(),
      direction: this.direction()
    };
  }

  persistAndLoad(): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        ownerName: this.ownerName() || null,
        petName: this.petName() || null,
        microchip: this.microchip() || null,
        withoutMicrochip: this.withoutMicrochip() || null,
        species: this.species() || null,
        breed: this.breed() || null,
        page: this.currentPage() || null,
        sort: this.sort(),
        direction: this.direction()
      },
      queryParamsHandling: 'merge'
    });
    this.load();
  }

  private syncFromQueryParams(): void {
    const params = this.route.snapshot.queryParamMap;
    this.ownerName.set(params.get('ownerName') ?? '');
    this.petName.set(params.get('petName') ?? '');
    this.microchip.set(params.get('microchip') ?? '');
    this.withoutMicrochip.set(params.get('withoutMicrochip') === 'true');
    const species = params.get('species') as PatientSpecies | null;
    this.species.set(['PERRO', 'GATO', 'PAJARO', 'OTRO'].includes(species ?? '') ? species : null);
    this.breed.set(params.get('breed') ?? '');
    this.currentPage.set(Math.max(0, Number(params.get('page') ?? 0) || 0));
  }

}
