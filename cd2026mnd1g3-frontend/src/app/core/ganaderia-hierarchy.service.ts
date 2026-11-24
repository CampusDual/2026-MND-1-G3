import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import {
  GanaderiaAnimalPage,
  GanaderiaFarmSummary,
  GanaderiaFarmPage,
  AltaGanaderiaAnimalRequest,
  ClasificacionGanaderiaRequest,
  OpcionesClasificacion,
  GanaderiaAnimal,
  GanaderiaBulkEstadoRequest,
  GanaderiaBulkProtocoloRequest,
  GanaderiaOperacionCampo,
  GanaderiaGroup,
  GanaderiaGroupType
} from './ganaderia-hierarchy.models';

@Injectable({ providedIn: 'root' })
export class GanaderiaHierarchyService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);

  private options(): { headers: HttpHeaders } {
    return { headers: new HttpHeaders({ Authorization: this.auth.getAuthHeader() }) };
  }

  findFarms(search: string, page: number, size: number): Observable<GanaderiaFarmPage> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'name,asc');
    if (search) params = params.set('search', search);
    return this.http.get<GanaderiaFarmPage>(
      '/api/ganaderia/explotaciones/resumen',
      { ...this.options(), params });
  }

  findGroups(farmId: number, type: GanaderiaGroupType): Observable<GanaderiaGroup[]> {
    const params = new HttpParams().set('type', type);
    return this.http.get<GanaderiaGroup[]>(
      `/api/ganaderia/explotaciones/${farmId}/grupos`,
      { ...this.options(), params });
  }

  findAnimals(
    farmId: number,
    groupId: string,
    type: GanaderiaGroupType,
    search: string,
    page: number,
    size: number,
    sort: string
  ): Observable<GanaderiaAnimalPage> {
    let params = new HttpParams()
      .set('type', type)
      .set('page', page)
      .set('size', size)
      .set('sort', sort);
    if (search) params = params.set('search', search);
    return this.http.get<GanaderiaAnimalPage>(
      `/api/ganaderia/explotaciones/${farmId}/grupos/${encodeURIComponent(groupId)}/animales`,
      { ...this.options(), params });
  }

  altaAnimal(
    farmId: number,
    request: AltaGanaderiaAnimalRequest
  ): Observable<GanaderiaAnimal> {
    return this.http.post<GanaderiaAnimal>(
      `/api/ganaderia/explotaciones/${farmId}/animales`,
      request,
      this.options());
  }

  clasificarAnimales(
    farmId: number,
    request: ClasificacionGanaderiaRequest
  ): Observable<void> {
    return this.http.patch<void>(
      `/api/ganaderia/explotaciones/${farmId}/animales/clasificar`,
      request,
      this.options());
  }

  findOpcionesClasificacion(
    farmId: number
  ): Observable<OpcionesClasificacion> {
    return this.http.get<OpcionesClasificacion>(
      `/api/ganaderia/explotaciones/${farmId}/opciones-clasificacion`,
      this.options());
  }

  syncOperacionesCampo(
    operaciones: GanaderiaOperacionCampo[]
  ): Observable<unknown[]> {
    return this.http.post<unknown[]>(
      '/api/ganaderia/operaciones-campo/sync',
      {
        operaciones: operaciones.map(operation => ({
          operationId: operation.operationId,
          farmId: operation.farmId,
          animalId: operation.animalId,
          tipoOperacion: operation.tipoOperacion,
          detalle: operation.detalle
        }))
      },
      this.options());
  }

  aplicarEstadoMasivo(
    farmId: number,
    request: GanaderiaBulkEstadoRequest
  ): Observable<number> {
    return this.http.patch<number>(
      `/api/ganaderia/explotaciones/${farmId}/animales/estado`,
      request,
      this.options());
  }

  aplicarProtocoloMasivo(
    farmId: number,
    request: GanaderiaBulkProtocoloRequest
  ): Observable<number> {
    return this.http.post<number>(
      `/api/ganaderia/explotaciones/${farmId}/protocolos/bloque`,
      request,
      this.options());
  }

  findByCrotal(farmId: number, crotal: string): Observable<GanaderiaAnimal> {
    const params = new HttpParams().set('crotal', crotal);
    return this.http.get<GanaderiaAnimal>(
      `/api/ganaderia/explotaciones/${farmId}/animales/buscar`,
      { ...this.options(), params });
  }
}
