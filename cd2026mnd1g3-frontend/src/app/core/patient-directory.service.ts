import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from './auth/auth.service';
import { PatientDetail, PatientPage, PatientSort, PatientSpecies } from './models/patient-directory.models';

export interface PatientFilters {
  readonly ownerName: string;
  readonly petName: string;
  readonly microchip: string;
  readonly withoutMicrochip: boolean;
  readonly species: PatientSpecies | null;
  readonly breed: string;
  readonly page: number;
  readonly size: number;
  readonly sort: PatientSort;
  readonly direction: 'asc' | 'desc';
}

@Injectable({ providedIn: 'root' })
export class PatientDirectoryService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);

  search(filters: PatientFilters): Observable<PatientPage> {
    let params = new HttpParams()
      .set('page', String(filters.page))
      .set('size', String(filters.size))
      .set('sort', filters.sort)
      .set('direction', filters.direction)
      .set('withoutMicrochip', String(filters.withoutMicrochip));
    if (filters.ownerName) params = params.set('ownerName', filters.ownerName);
    if (filters.petName) params = params.set('petName', filters.petName);
    if (filters.microchip) params = params.set('microchip', filters.microchip);
    if (filters.species) params = params.set('species', filters.species);
    if (filters.breed) params = params.set('breed', filters.breed);
    return this.http.get<PatientPage>('/api/clinica/pacientes', {
      headers: { Authorization: this.auth.getAuthHeader() },
      params
    });
  }

  detail(patientId: number): Observable<PatientDetail> {
    return this.http.get<PatientDetail>(`/api/clinica/pacientes/${patientId}`, {
      headers: { Authorization: this.auth.getAuthHeader() }
    });
  }
}
