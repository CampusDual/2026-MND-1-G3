import { Component, OnInit, inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { AlertService } from '../core/alert.service';
import {
  AlertaSistema,
  Animal,
  Farm,
  LoteStock,
  LivestockService,
  Medicamento,
  MovimientoInventario
} from '../core/livestock.service';

interface ConsultaReceta {
  fecha: string;
  animalId: number;
  loteStockId: number;
  cantidad: number;
  motivo: string;
}

/** Vista operativa de consultas, recetas y trazabilidad clínica. */
@Component({
  selector: 'app-clinica-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './clinica-dashboard.component.html',
  styleUrl: './clinica-dashboard.component.scss'
})
export class ClinicaDashboardComponent implements OnInit, OnDestroy {
  private readonly service = inject(LivestockService);
  private readonly alertService = inject(AlertService);
  private alertSubscription?: Subscription;

  activeTab: 'consultas' | 'inventario' = 'consultas';
  farms: Farm[] = [];
  animals: Animal[] = [];
  medicamentos: Medicamento[] = [];
  lotes: LoteStock[] = [];
  movimientos: MovimientoInventario[] = [];
  alerts: AlertaSistema[] = [];
  selectedMedicationId: number | null = null;
  entryLotId: number | null = null;
  entryQuantity = 0;
  error = '';
  loading = false;
  consultation: ConsultaReceta = this.emptyConsultation();
  consultas: ConsultaReceta[] = [];

  ngOnInit(): void {
    this.alertService.connect();
    this.alertSubscription = this.alertService.alerts.subscribe(alerts => this.alerts = alerts);
    this.load();
  }

  ngOnDestroy(): void {
    this.alertSubscription?.unsubscribe();
  }

  get unreadAlerts(): number {
    return this.alerts.filter(alert => !alert.leida).length;
  }

  get lotesDisponibles(): LoteStock[] {
    return this.lotes.filter(lote =>
      (this.selectedMedicationId === null || lote.medicamentoId === this.selectedMedicationId)
      && this.stockOfLote(lote.id) > 0);
  }

  stockOfLote(loteId: number): number {
    return this.movimientos
      .filter(movimiento => movimiento.loteStockId === loteId)
      .reduce((total, movimiento) => total + movimiento.cantidad, 0);
  }

  stockOfMedication(medicamentoId: number): number {
    return this.lotes
      .filter(lote => lote.medicamentoId === medicamentoId)
      .reduce((total, lote) => total + this.stockOfLote(lote.id), 0);
  }

  loteNumber(loteId: number): string {
    return this.lotes.find(lote => lote.id === loteId)?.numeroLote ?? String(loteId);
  }

  animalName(animalId: number | undefined): string {
    if (!animalId) return 'Sin animal';
    const animal = this.animals.find(item => item.id === animalId);
    return animal ? `${animal.name || animal.earTag} · ${animal.earTag}` : String(animalId);
  }

  farmName(explotacionId: number | undefined): string {
    if (!explotacionId) return 'Sin explotación';
    return this.farms.find(farm => farm.id === explotacionId)?.name ?? String(explotacionId);
  }

  onMedicationChange(): void {
    this.consultation.loteStockId = 0;
  }

  registerConsultation(): void {
    if (!this.consultation.animalId || !this.consultation.loteStockId
      || !this.consultation.cantidad || this.consultation.cantidad <= 0) {
      this.error = 'Completa animal, lote y cantidad para registrar la consulta.';
      return;
    }
    const animal = this.animals.find(item => item.id === this.consultation.animalId);
    this.service.registrarMovimiento({
      loteStockId: this.consultation.loteStockId,
      cantidad: this.consultation.cantidad,
      tipo: 'SALIDA_CLINICA',
      animalId: this.consultation.animalId,
      explotacionId: animal?.farmId
    }).subscribe({
      next: () => {
        this.consultas = [...this.consultas, { ...this.consultation }];
        this.consultation = this.emptyConsultation();
        this.load();
      },
      error: () => this.error = 'No se pudo registrar la salida clínica.'
    });
  }

  registerPurchaseEntry(): void {
    if (!this.entryLotId || this.entryQuantity <= 0) {
      this.error = 'Selecciona un lote y una cantidad válida.';
      return;
    }
    this.service.registrarMovimiento({
      loteStockId: this.entryLotId,
      cantidad: this.entryQuantity,
      tipo: 'ENTRADA_COMPRA'
    }).subscribe({
      next: () => {
        this.entryQuantity = 0;
        this.entryLotId = null;
        this.load();
      },
      error: () => this.error = 'No se pudo registrar la entrada de compra.'
    });
  }

  markAlertRead(alert: AlertaSistema): void {
    this.service.markAlertRead(alert.id).subscribe({
      next: updated => this.alerts = this.alerts.map(item => item.id === updated.id ? updated : item),
      error: () => this.error = 'No se pudo marcar la alerta como leída.'
    });
  }

  private load(): void {
    this.loading = true;
    this.service.farms().subscribe(data => this.farms = data);
    this.service.animals().subscribe(data => this.animals = data);
    this.service.medicamentos().subscribe(data => this.medicamentos = data);
    this.service.lotes().subscribe(data => this.lotes = data);
    this.service.alerts().subscribe({
      next: data => {
        this.alertService.setInitialAlerts(data);
        this.loading = false;
      },
      error: () => { this.error = 'No se pudieron cargar los datos de clínica.'; this.loading = false; }
    });
    this.service.movimientos().subscribe({
      next: data => this.movimientos = data,
      error: () => this.error = 'No se pudo cargar el libro mayor.'
    });
  }

  private emptyConsultation(): ConsultaReceta {
    return {
      fecha: new Date().toISOString().slice(0, 10),
      animalId: 0,
      loteStockId: 0,
      cantidad: 0,
      motivo: ''
    };
  }
}
