import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { AuthService } from './auth/auth.service';

export type AnimalHealthStatus = 'SANO' | 'EN_TRATAMIENTO' | 'EN_RETIRADA_MEDICAMENTO' | 'ENFERMO' | 'CUARENTENA';
export type ProtocolEventType = 'VACUNACION' | 'TRATAMIENTO_MEDICO' | 'PARTO' | 'REVISION_PERIODICA';
export type DestinatarioRol = 'VETERINARIAN' | 'FARM_MANAGER' | 'CLINIC_ADMIN';
export type TipoAlerta = 'RECORDATORIO_CICLICO' | 'FIN_RETIRADA_MEDICAMENTO' | 'URGENCIA_ANIMAL_ENFERMO' | 'STOCK_BAJO';
export type TipoMovimiento = 'ENTRADA_COMPRA' | 'SALIDA_GANADERIA' | 'SALIDA_CLINICA' | 'MERMA_CADUCIDAD' | 'AJUSTE_ANULACION';
export type CategoriaPran = 'A' | 'B' | 'C' | 'D';

export interface Farm {
  id?: number;
  name: string;
  regaCode: string;
  mainSpecies: string;
  managerName: string;
  clinicaId?: number;
}

export interface Animal {
  id?: number;
  earTag: string;
  name?: string;
  species: string;
  birthDate: string;
  healthStatus: string;
  estadoSanitario?: AnimalHealthStatus;
  fechaFinRetirada?: string;
  notas?: string;
  farmId: number;
}

export interface ProtocolEvent {
  id?: number;
  eventType: string;
  title: string;
  applicationDate: string;
  withdrawalDays: number;
  withdrawalEndDate?: string;
  nextReminderDays?: number;
  nextCycleDays?: number;
  status?: string;
  animalId?: number;
  farmId: number;
  loteStockId?: number;
  cantidadUsada?: number;
}
export interface AlertaSistema {
  id: number;
  clinicaId: number;
  destinatarioRol: DestinatarioRol;
  tipoAlerta: TipoAlerta;
  titulo: string;
  mensaje: string;
  fechaProgramada: string;
  leida: boolean;
  confirmadaPorGranja: boolean;
  explotacionId?: number;
  animalId?: number;
}
export interface Medicamento {
  id: number;
  clinicaId: number;
  nombreComercial: string;
  principioActivo: string;
  especieDestino: string;
  categoriaPran: CategoriaPran;
  diasRetiradaDefecto: number;
  stockMinimoAlerta: number;
}
export interface LoteStock {
  id: number;
  medicamentoId: number;
  numeroLote: string;
  fechaCaducidad: string;
  proveedor: string;
}
export interface MovimientoInventario {
  id: number;
  clinicaId: number;
  loteStockId: number;
  tipoMovimiento: TipoMovimiento;
  cantidad: number;
  fechaHora: string;
  veterinarioId?: number;
  animalId?: number;
  explotacionId?: number;
  protocoloEventoId?: number;
}

@Injectable({ providedIn: 'root' })
export class LivestockService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private options(): { headers: HttpHeaders } { return { headers: new HttpHeaders({ Authorization: this.auth.getAuthHeader() }) }; }
  farms(): Observable<Farm[]> {
    return this.http.get<Farm[]>('/api/explotaciones', this.options()).pipe(
      map((farms: readonly Farm[]) => farms.map((farm: Farm) => ({
        ...farm,
        regaCode: farm.regaCode.trim().toUpperCase()
      })))
    );
  }
  animals(): Observable<Animal[]> {
    return this.http.get<Animal[]>('/api/ganaderia/animales', this.options()).pipe(
      map((animals: readonly Animal[]) => animals.map((animal: Animal) => ({
        ...animal,
        earTag: animal.earTag.trim().toUpperCase()
      })))
    );
  }
  protocols(): Observable<ProtocolEvent[]> { return this.http.get<ProtocolEvent[]>('/api/protocolos', this.options()); }
  alerts(): Observable<AlertaSistema[]> { return this.http.get<AlertaSistema[]>('/api/alertas', this.options()); }
  markAlertRead(id: number): Observable<AlertaSistema> { return this.http.post<AlertaSistema>(`/api/alertas/${id}/leida`, {}, this.options()); }
  confirmAlert(id: number): Observable<AlertaSistema> { return this.http.post<AlertaSistema>(`/api/alertas/${id}/confirmar-granja`, {}, this.options()); }
  createProtocol(protocol: ProtocolEvent): Observable<ProtocolEvent> { return this.http.post<ProtocolEvent>('/api/protocolos-ciclicos', protocol, this.options()); }
  runDailyReview(): Observable<void> { return this.http.post<void>('/api/protocolos-ciclicos/ejecutar-revision-diaria', {}, this.options()); }
  updateAnimalStatus(id: number, status: AnimalHealthStatus, notes: string): Observable<void> {
    const params = { estado: status, notas: notes };
    return this.http.post<void>(`/api/ganaderia/animales/${id}/estado`, {}, { ...this.options(), params });
  }
  medicamentos(): Observable<Medicamento[]> { return this.http.get<Medicamento[]>('/api/inventario/medicamentos', this.options()); }
  lotes(): Observable<LoteStock[]> { return this.http.get<LoteStock[]>('/api/inventario/lotes', this.options()); }
  movimientos(): Observable<MovimientoInventario[]> { return this.http.get<MovimientoInventario[]>('/api/inventario/movimientos', this.options()); }
  registrarMovimiento(movimiento: { loteStockId: number; cantidad: number; tipo: TipoMovimiento; veterinarioId?: number; animalId?: number; explotacionId?: number; protocoloEventoId?: number }): Observable<MovimientoInventario> {
    return this.http.post<MovimientoInventario>('/api/inventario/movimientos', {}, {
      ...this.options(),
      params: {
        loteStockId: movimiento.loteStockId,
        cantidad: movimiento.cantidad,
        tipo: movimiento.tipo,
        ...(movimiento.veterinarioId !== undefined ? { veterinarioId: movimiento.veterinarioId } : {}),
        ...(movimiento.animalId !== undefined ? { animalId: movimiento.animalId } : {}),
        ...(movimiento.explotacionId !== undefined ? { explotacionId: movimiento.explotacionId } : {}),
        ...(movimiento.protocoloEventoId !== undefined ? { protocoloEventoId: movimiento.protocoloEventoId } : {})
      }
    });
  }
}
