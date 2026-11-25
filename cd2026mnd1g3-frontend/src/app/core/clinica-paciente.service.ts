import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AltaPacienteRequest,
  AltaConsultaClinicaRequest,
  AltaPacienteResponse,
  EspecieMascota,
  PacienteClinica,
  PacientePage,
  ConsultaClinica,
  PropietarioClinica
} from './clinica-paciente.models';

@Injectable({ providedIn: 'root' })
export class ClinicaPacienteService {
  private readonly http = inject(HttpClient);

  buscarPropietarios(termino: string): Observable<PropietarioClinica[]> {
    const params = new HttpParams().set('termino', termino);
    return this.http.get<PropietarioClinica[]>('/api/clinica/pacientes/propietarios/buscar', { params });
  }

  buscarDuplicado(telefono: string, dniNif: string): Observable<PropietarioClinica | null> {
    let params = new HttpParams();
    if (telefono) params = params.set('telefono', telefono);
    if (dniNif) params = params.set('dniNif', dniNif);
    return this.http.get<PropietarioClinica | null>('/api/clinica/pacientes/propietarios/duplicado', { params });
  }

  mascotasPropietario(propietarioId: number): Observable<PacienteClinica[]> {
    return this.http.get<PacienteClinica[]>(`/api/clinica/pacientes/propietarios/${propietarioId}/mascotas`);
  }

  razas(especie: EspecieMascota): Observable<string[]> {
    const params = new HttpParams().set('especie', especie);
    return this.http.get<string[]>('/api/clinica/pacientes/razas', { params });
  }

  alta(request: AltaPacienteRequest): Observable<AltaPacienteResponse> {
    return this.http.post<AltaPacienteResponse>('/api/clinica/pacientes/alta', request);
  }

  findPacientes(
    search = '',
    page = 0,
    size = 20
  ): Observable<PacientePage> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'nombre,asc');
    if (search.trim()) params = params.set('nombre', search.trim());
    return this.http.get<PacientePage>('/api/clinica/pacientes', { params });
  }

  findPaciente(id: number): Observable<PacienteClinica> {
    return this.http.get<PacienteClinica>(`/api/clinica/pacientes/${id}`);
  }

  findConsultas(): Observable<ConsultaClinica[]> {
    return this.http.get<ConsultaClinica[]>('/api/clinica/consultas');
  }

  crearConsulta(request: AltaConsultaClinicaRequest): Observable<ConsultaClinica> {
    return this.http.post<ConsultaClinica>(
      '/api/clinica/consultas',
      request);
  }

}
