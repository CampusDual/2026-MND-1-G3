import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, Subject, tap } from 'rxjs';
import { AuthService } from './auth.service';
import {
  AltaPacienteRequest,
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
  private readonly auth = inject(AuthService);
  private readonly pacientesActualizadosSubject = new Subject<void>();
  readonly pacientesActualizados = this.pacientesActualizadosSubject.asObservable();

  private options(): { headers: HttpHeaders } {
    return { headers: new HttpHeaders({ Authorization: this.auth.getAuthHeader() }) };
  }

  buscarPropietarios(termino: string): Observable<PropietarioClinica[]> {
    const params = new HttpParams().set('termino', termino);
    return this.http.get<PropietarioClinica[]>(
      '/api/clinica/pacientes/propietarios/buscar',
      { ...this.options(), params });
  }

  buscarDuplicado(telefono: string, dniNif: string): Observable<PropietarioClinica | null> {
    let params = new HttpParams();
    if (telefono) params = params.set('telefono', telefono);
    if (dniNif) params = params.set('dniNif', dniNif);
    return this.http.get<PropietarioClinica | null>(
      '/api/clinica/pacientes/propietarios/duplicado',
      { ...this.options(), params });
  }

  mascotasPropietario(propietarioId: number): Observable<PacienteClinica[]> {
    return this.http.get<PacienteClinica[]>(
      `/api/clinica/pacientes/propietarios/${propietarioId}/mascotas`,
      this.options());
  }

  razas(especie: EspecieMascota): Observable<string[]> {
    const params = new HttpParams().set('especie', especie);
    return this.http.get<string[]>(
      '/api/clinica/pacientes/razas',
      { ...this.options(), params });
  }

  alta(request: AltaPacienteRequest): Observable<AltaPacienteResponse> {
    return this.http.post<AltaPacienteResponse>(
      '/api/clinica/pacientes/alta', request, this.options()).pipe(
      // El dashboard clínico recargará el selector al recibir la notificación local.
      tap(() => this.pacientesActualizadosSubject.next())
    );
  }

  findPacientes(size = 100): Observable<PacientePage> {
    const params = new HttpParams()
      .set('page', '0')
      .set('size', size)
      .set('sort', 'nombre,asc');
    return this.http.get<PacientePage>(
      '/api/clinica/pacientes',
      { ...this.options(), params });
  }

  findConsultas(): Observable<ConsultaClinica[]> {
    return this.http.get<ConsultaClinica[]>(
      '/api/clinica/consultas',
      this.options());
  }

  crearConsulta(request: {
    pacienteId: number;
    fechaConsulta: string;
    motivo?: string;
    notas?: string;
    proximaVacuna?: string;
  }): Observable<ConsultaClinica> {
    return this.http.post<ConsultaClinica>(
      '/api/clinica/consultas',
      request,
      this.options());
  }
}
