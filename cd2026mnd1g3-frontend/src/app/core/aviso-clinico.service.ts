import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AvisoClinico,
  AvisoClinicoRequest,
  AvisoPage,
  EstadoAvisoClinico,
  MotivoDescarteAviso
} from './aviso-clinico.models';

@Injectable({ providedIn: 'root' })
export class AvisoClinicoService {
  private readonly http = inject(HttpClient);

  crear(request: AvisoClinicoRequest): Observable<AvisoClinico> {
    return this.http.post<AvisoClinico>('/api/clinica/avisos', request);
  }

  find(page: number, size: number): Observable<AvisoPage> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'fechaObjetivo,asc');
    return this.http.get<AvisoPage>('/api/clinica/avisos', { params });
  }

  marcarIntentado(id: number): Observable<AvisoClinico> {
    return this.http.post<AvisoClinico>(`/api/clinica/avisos/${id}/intentado`, {});
  }

  posponer(id: number, nuevaFecha: string): Observable<AvisoClinico> {
    const params = new HttpParams().set('nuevaFecha', nuevaFecha);
    return this.http.post<AvisoClinico>(`/api/clinica/avisos/${id}/posponer`, {}, { params });
  }

  cambiarEstado(
    id: number,
    estado: EstadoAvisoClinico,
    motivoDescarte?: MotivoDescarteAviso,
    motivoTexto?: string,
    confirmarFallecimiento?: boolean,
    nuevaFecha?: string
  ): Observable<AvisoClinico> {
    return this.http.post<AvisoClinico>(`/api/clinica/avisos/${id}/estado`,
      { estado, motivoDescarte, motivoTexto, confirmarFallecimiento, nuevaFecha });
  }
}
