import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  Animal,
  AnimalHealthStatus,
  Farm,
  LivestockService,
  ProtocolEvent,
  Medicamento,
  LoteStock,
  MovimientoInventario,
  AlertaSistema
} from '../../core/livestock.service';
import { WebSocketService } from '../../core/websocket/websocket.service';
import { Subscription } from 'rxjs';
import { SkeletonLoaderComponent } from '../../shared/ui/skeleton-loader.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state.component';
import { ErrorBannerComponent } from '../../shared/ui/error-banner.component';
import { ConfirmDialogComponent } from '../../shared/ui/confirm-dialog.component';
import { ToastNotificationComponent } from '../../shared/ui/toast-notification.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink, SkeletonLoaderComponent, EmptyStateComponent, ErrorBannerComponent, ConfirmDialogComponent, ToastNotificationComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private readonly service = inject(LivestockService);
  private readonly webSocketService = inject(WebSocketService);
  private alertSubscription?: Subscription;
  farms: Farm[] = [];
  animals: Animal[] = [];
  protocols: ProtocolEvent[] = [];
  alerts: AlertaSistema[] = [];
  medicamentos: Medicamento[] = [];
  lotes: LoteStock[] = [];
  movimientos: MovimientoInventario[] = [];
  protocol: ProtocolEvent = this.emptyProtocol();
  selectedFarmId: number | null = null;
  selectedStatus: AnimalHealthStatus = 'SANO';
  statusNotes = '';
  error = '';
  loading = false;
  isSubmitting = false;
  toastMessage = '';
  confirmStatus: { animal: Animal; status: AnimalHealthStatus } | null = null;
  readonly protocolForm = inject(FormBuilder).nonNullable.group({
    farmId: [0, [Validators.required, Validators.min(1)]],
    animalId: [0],
    title: ['', [Validators.required, Validators.minLength(3)]],
    eventType: ['TRATAMIENTO_MEDICO' as 'VACUNACION' | 'TRATAMIENTO_MEDICO' | 'PARTO' | 'REVISION_PERIODICA', Validators.required],
    applicationDate: [new Date().toISOString().slice(0, 10), Validators.required],
    loteStockId: [0],
    cantidadUsada: [0, [Validators.min(0)]],
    withdrawalDays: [0, [Validators.required, Validators.min(0)]],
    nextCycleDays: [0, [Validators.required, Validators.min(0)]]
  });

  ngOnInit(): void {
    this.webSocketService.connect();
    this.alertSubscription = this.webSocketService.alerts.subscribe(alerts => this.alerts = [...alerts]);
    this.load();
  }
  ngOnDestroy(): void {
    this.alertSubscription?.unsubscribe();
  }
  get unreadAlerts(): number {
    return this.alerts.filter(alert => !alert.leida).length;
  }
  get withdrawalCount(): number {
    return this.animals.filter(item => item.estadoSanitario === 'EN_RETIRADA_MEDICAMENTO').length;
  }
  get reminderCount(): number {
    const today = new Date().toISOString().slice(0, 10);
    return this.protocols.filter(item => !!item.applicationDate && item.applicationDate >= today).length;
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
    if (this.protocolForm.invalid) {
      this.protocolForm.markAllAsTouched();
      this.error = 'Completa los datos obligatorios del protocolo.';
      return;
    }
    const raw = this.protocolForm.getRawValue();
    const stock = raw.loteStockId ? this.stockOfLote(raw.loteStockId) : 0;
    if (raw.loteStockId && (raw.cantidadUsada <= 0 || raw.cantidadUsada > stock)) {
      this.error = 'La cantidad usada debe ser mayor que cero y no superar el stock disponible.';
      return;
    }
    this.protocol = {
      eventType: raw.eventType,
      title: raw.title.trim(),
      applicationDate: raw.applicationDate,
      withdrawalDays: raw.withdrawalDays,
      nextCycleDays: raw.nextCycleDays,
      farmId: raw.farmId,
      animalId: raw.animalId || undefined,
      loteStockId: raw.loteStockId || undefined,
      cantidadUsada: raw.loteStockId ? raw.cantidadUsada : undefined,
      status: 'PROGRAMADO'
    };
    this.error = '';
    this.isSubmitting = true;
    this.service.createProtocol(this.protocol).subscribe({
      next: () => {
        this.protocolForm.reset(this.emptyProtocolForm());
        this.toastMessage = 'Protocolo registrado correctamente.';
        this.load();
      },
      error: () => { this.error = 'No se pudo registrar el protocolo.'; },
      complete: () => { this.isSubmitting = false; }
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
    if (this.selectedStatus === 'ENFERMO') {
      this.confirmStatus = { animal, status: this.selectedStatus };
      return;
    }
    this.executeStatusUpdate(animal, this.selectedStatus);
  }
  executeStatusUpdate(animal: Animal, status: AnimalHealthStatus): void {
    if (!animal.id) return;
    this.service.updateAnimalStatus(animal.id, status, this.statusNotes).subscribe({
      next: () => { this.statusNotes = ''; this.toastMessage = 'Estado sanitario actualizado.'; this.load(); },
      error: () => this.error = 'No se pudo actualizar el estado sanitario.'
    });
  }
  statusLabel(status: string | undefined): string {
    return status ?? 'SANO';
  }
  filteredLotes(): LoteStock[] {
    if (!this.protocol.loteStockId) {
      const selectedMedication = this.medicamentos.find(medicamento => medicamento.id === this.protocolMedicationId);
      if (!selectedMedication) {
        return this.lotes;
      }
    }
    return this.lotes.filter(lote =>
      this.protocolMedicationId === null || lote.medicamentoId === this.protocolMedicationId);
  }
  protocolMedicationId: number | null = null;
  onMedicationChange(): void {
    this.protocol.loteStockId = undefined;
    this.protocolForm.patchValue({ loteStockId: 0, cantidadUsada: 0 });
  }
  stockOfLote(loteId: number): number {
    return this.movimientos
      .filter(movimiento => movimiento.loteStockId === loteId)
      .reduce((total, movimiento) => total + movimiento.cantidad, 0);
  }
  lotesWithStock(): LoteStock[] {
    return this.lotes.filter(lote =>
      (this.protocolMedicationId === null || lote.medicamentoId === this.protocolMedicationId)
      && this.stockOfLote(lote.id) > 0);
  }
  markAlertRead(alert: AlertaSistema): void {
    this.service.markAlertRead(alert.id).subscribe({
      next: updated => this.replaceAlert(updated),
      error: () => this.error = 'No se pudo marcar la alerta como leída.'
    });
  }
  confirmAlert(alert: AlertaSistema): void {
    this.service.confirmAlert(alert.id).subscribe({
      next: updated => this.replaceAlert(updated),
      error: () => this.error = 'No se pudo confirmar la ejecución.'
    });
  }
  alertLabel(alert: AlertaSistema): string {
    if (alert.tipoAlerta === 'URGENCIA_ANIMAL_ENFERMO') return 'Urgencia animal enfermo';
    if (alert.tipoAlerta === 'FIN_RETIRADA_MEDICAMENTO') return 'Fin de retirada';
    if (alert.tipoAlerta === 'STOCK_BAJO') return 'Stock bajo';
    return 'Recordatorio cíclico';
  }
  private replaceAlert(updated: AlertaSistema): void {
    this.alerts = this.alerts.map(alert => alert.id === updated.id ? updated : alert);
  }
  load(): void {
    this.loading = true;
    this.service.farms().subscribe(data => this.farms = data);
    this.service.animals().subscribe(data => this.animals = data);
    this.service.protocols().subscribe(data => this.protocols = data);
    this.service.medicamentos().subscribe(data => this.medicamentos = data);
    this.service.lotes().subscribe(data => this.lotes = data);
    this.service.movimientos().subscribe(data => this.movimientos = data);
    this.service.alerts().subscribe({
      next: data => {
        this.webSocketService.setInitialAlerts(data);
        this.loading = false;
      },
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
  private emptyProtocolForm(): {
    farmId: number;
    animalId: number;
    title: string;
    eventType: 'VACUNACION' | 'TRATAMIENTO_MEDICO' | 'PARTO' | 'REVISION_PERIODICA';
    applicationDate: string;
    loteStockId: number;
    cantidadUsada: number;
    withdrawalDays: number;
    nextCycleDays: number;
  } {
    return {
      farmId: 0,
      animalId: 0,
      title: '',
      eventType: 'TRATAMIENTO_MEDICO',
      applicationDate: new Date().toISOString().slice(0, 10),
      loteStockId: 0,
      cantidadUsada: 0,
      withdrawalDays: 0,
      nextCycleDays: 0
    };
  }
}
