import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import {
  AvisoClinico,
  AvisoPage,
  EstadoAvisoClinico,
  MotivoDescarteAviso
} from './aviso-clinico.models';

@Injectable({ providedIn: 'root' })
export class AvisoClinicoService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);

  private options(): { headers: HttpHeaders } {
    return { headers: new HttpHeaders({ Authorization: this.auth.getAuthHeader() }) };
  }

  find(page: number, size: number): Observable<AvisoPage> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'fechaObjetivo,asc');
    return this.http.get<AvisoPage>(
      '/api/clinica/avisos',
      { ...this.options(), params });
  }

  marcarIntentado(id: number): Observable<AvisoClinico> {
    return this.http.post<AvisoClinico>(
      `/api/clinica/avisos/${id}/intentado`, {}, this.options());
  }

  posponer(id: number, nuevaFecha: string): Observable<AvisoClinico> {
    const params = new HttpParams().set('nuevaFecha', nuevaFecha);
    return this.http.post<AvisoClinico>(
      `/api/clinica/avisos/${id}/posponer`, {}, {
        ...this.options(),
        params
      });
  }

  cambiarEstado(
    id: number,
    estado: EstadoAvisoClinico,
    motivoDescarte?: MotivoDescarteAviso,
    confirmarFallecimiento?: boolean,
    nuevaFecha?: string
  ): Observable<AvisoClinico> {
    return this.http.post<AvisoClinico>(
      `/api/clinica/avisos/${id}/estado`,
      { estado, motivoDescarte, confirmarFallecimiento, nuevaFecha },
      this.options());
  }
}
