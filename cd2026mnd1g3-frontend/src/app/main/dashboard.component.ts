import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  Animal,
  AnimalHealthStatus,
  Farm,
  LivestockService,
  Notification,
  ProtocolEvent
} from '../core/livestock.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private readonly service = inject(LivestockService);
  farms: Farm[] = [];
  animals: Animal[] = [];
  protocols: ProtocolEvent[] = [];
  notifications: Notification[] = [];
  protocol: ProtocolEvent = this.emptyProtocol();
  selectedFarmId: number | null = null;
  selectedStatus: AnimalHealthStatus = 'SANO';
  statusNotes = '';
  error = '';
  loading = false;

  ngOnInit(): void { this.load(); }
  get withdrawalCount(): number {
    return this.animals.filter(item => item.estadoSanitario === 'EN_RETIRADA_MEDICAMENTO').length;
  }
  get reminderCount(): number {
    const today = new Date().toISOString().slice(0, 10);
    return this.protocols.filter(item => !!item.applicationDate && item.applicationDate >= today).length;
  }
  get pendingNotifications(): number {
    return this.notifications.filter(item => item.sendStatus === 'PENDIENTE').length;
  }
  get confirmedNotifications(): number {
    return this.notifications.filter(item => item.sendStatus === 'CONFIRMADO_GANADERO').length;
  }
  get filteredAnimals(): Animal[] {
    return this.selectedFarmId === null
      ? this.animals
      : this.animals.filter(item => item.farmId === this.selectedFarmId);
  }
  animalCount(farmId: number | undefined): number { return this.animals.filter(item => item.farmId === farmId).length; }
  farmName(farmId: number): string {
    return this.farms.find(farm => farm.id === farmId)?.name ?? '—';
  }
  createProtocol(): void {
    this.error = '';
    this.loading = true;
    this.service.createProtocol(this.protocol).subscribe({
      next: () => { this.protocol = this.emptyProtocol(); this.load(); },
      error: () => { this.error = 'No se pudo registrar el protocolo.'; this.loading = false; }
    });
  }
  runDailyReview(): void {
    this.service.runDailyReview().subscribe({
      next: () => this.load(),
      error: () => this.error = 'No se pudo ejecutar la revisión diaria.'
    });
  }
  updateStatus(animal: Animal): void {
    if (!animal.id) {
      this.error = 'El animal no tiene un identificador válido.';
      return;
    }
    this.service.updateAnimalStatus(animal.id, this.selectedStatus, this.statusNotes).subscribe({
      next: () => { this.statusNotes = ''; this.load(); },
      error: () => this.error = 'No se pudo actualizar el estado sanitario.'
    });
  }
  statusLabel(status: string | undefined): string {
    return status ?? 'SANO';
  }
  private load(): void {
    this.loading = true;
    this.service.farms().subscribe(data => this.farms = data);
    this.service.animals().subscribe(data => this.animals = data);
    this.service.protocols().subscribe(data => this.protocols = data);
    this.service.notifications().subscribe({
      next: data => { this.notifications = data; this.loading = false; },
      error: () => { this.error = 'No se pudieron cargar los datos del dashboard.'; this.loading = false; }
    });
  }
  private emptyProtocol(): ProtocolEvent {
    return {
      eventType: 'TRATAMIENTO_MEDICO',
      title: '',
      applicationDate: new Date().toISOString().slice(0, 10),
      withdrawalDays: 0,
      nextCycleDays: 0,
      farmId: 0,
      status: 'PROGRAMADO'
    };
  }
}
