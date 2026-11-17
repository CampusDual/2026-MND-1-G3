import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export type AnimalHealthStatus = 'SANO' | 'EN_TRATAMIENTO' | 'EN_RETIRADA_MEDICAMENTO' | 'ENFERMO' | 'CUARENTENA';
export type ProtocolEventType = 'VACUNACION' | 'TRATAMIENTO_MEDICO' | 'PARTO' | 'REVISION_PERIODICA';
export type NotificationStatus = 'PENDIENTE' | 'ENVIADO' | 'CONFIRMADO_GANADERO';

export interface Farm {
  id?: number;
  name: string;
  regaCode: string;
  mainSpecies: string;
  managerName: string;
  whatsappPhone: string;
  veterinarianAssignedPhone?: string;
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
  medication?: string;
  medicationBatch?: string;
  applicationDate: string;
  withdrawalDays: number;
  withdrawalEndDate?: string;
  nextReminderDays?: number;
  nextCycleDays?: number;
  status?: string;
  animalId?: number;
  farmId: number;
}
export interface Notification { id: number; destinationPhone: string; message: string; scheduledAt: string; sentAt?: string; sendStatus: string; protocolEventId: number; }

@Injectable({ providedIn: 'root' })
export class LivestockService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private options(): { headers: HttpHeaders } { return { headers: new HttpHeaders({ Authorization: this.auth.getAuthHeader() }) }; }
  farms(): Observable<Farm[]> { return this.http.get<Farm[]>('/api/explotaciones', this.options()); }
  animals(): Observable<Animal[]> { return this.http.get<Animal[]>('/api/animales', this.options()); }
  protocols(): Observable<ProtocolEvent[]> { return this.http.get<ProtocolEvent[]>('/api/protocolos', this.options()); }
  notifications(): Observable<Notification[]> { return this.http.get<Notification[]>('/api/notificaciones', this.options()); }
  createProtocol(protocol: ProtocolEvent): Observable<ProtocolEvent> { return this.http.post<ProtocolEvent>('/api/protocolos-ciclicos', protocol, this.options()); }
  simulateSend(id: number): Observable<Notification> { return this.http.post<Notification>(`/api/notificaciones/${id}/simular-envio`, {}, this.options()); }
  confirmNotification(id: number): Observable<Notification> { return this.http.post<Notification>(`/api/notificaciones/${id}/simular-confirmacion-ganadero`, {}, this.options()); }
  runDailyReview(): Observable<void> { return this.http.post<void>('/api/protocolos-ciclicos/ejecutar-revision-diaria', {}, this.options()); }
  updateAnimalStatus(id: number, status: AnimalHealthStatus, notes: string): Observable<void> {
    const params = { estado: status, notas: notes };
    return this.http.post<void>(`/api/protocolos-ciclicos/animales/${id}/estado`, {}, { ...this.options(), params });
  }
}
