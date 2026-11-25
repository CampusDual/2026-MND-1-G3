import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AltaRecetaClinicaRequest,
  RecetaClinica
} from './clinica-paciente.models';

@Injectable({ providedIn: 'root' })
export class RecetaClinicaService {
  private readonly http = inject(HttpClient);

  find(): Observable<RecetaClinica[]> {
    return this.http.get<RecetaClinica[]>('/api/clinica/recetas');
  }

  crear(request: AltaRecetaClinicaRequest): Observable<RecetaClinica> {
    return this.http.post<RecetaClinica>('/api/clinica/recetas', request);
  }
}
