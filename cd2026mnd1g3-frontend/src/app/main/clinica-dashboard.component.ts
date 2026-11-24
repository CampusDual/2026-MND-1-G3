import { Component, OnInit, inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { AlertService } from '../core/alert.service';
import { ClinicaPacienteService } from '../core/clinica-paciente.service';
import { PacienteClinica } from '../core/clinica-paciente.models';
import {
  AlertaSistema,
  Farm,
  LoteStock,
  LivestockService,
  Medicamento,
  MovimientoInventario
} from '../core/livestock.service';

interface ConsultaReceta {
  fecha: string;
  pacienteId: number;
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
  private readonly pacienteService = inject(ClinicaPacienteService);
  private alertSubscription?: Subscription;
  private pacienteSubscription?: Subscription;

  activeTab: 'consultas' | 'inventario' = 'consultas';
  farms: Farm[] = [];
  pacientes: PacienteClinica[] = [];
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
    this.pacienteSubscription = this.pacienteService.pacientesActualizados
      .subscribe(() => this.loadPacientes());
    this.load();
  }

  ngOnDestroy(): void {
    this.alertSubscription?.unsubscribe();
    this.pacienteSubscription?.unsubscribe();
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

  pacienteLabel(pacienteId: number | undefined): string {
    if (!pacienteId) return 'Sin paciente';
    const paciente = this.pacientes.find(item => item.id === pacienteId);
    if (!paciente) return String(pacienteId);
    return `${paciente.nombre} — ${paciente.especie}/${paciente.raza || 'Sin especificar'} (Propietario: ${paciente.propietario.nombre} ${paciente.propietario.apellidos})`;
  }

  farmName(explotacionId: number | undefined): string {
    if (!explotacionId) return 'Sin explotación';
    return this.farms.find(farm => farm.id === explotacionId)?.name ?? String(explotacionId);
  }

  onMedicationChange(): void {
    this.consultation.loteStockId = 0;
  }

  registerConsultation(): void {
    if (!this.consultation.pacienteId || !this.consultation.loteStockId
      || !this.consultation.cantidad || this.consultation.cantidad <= 0) {
      this.error = 'Completa paciente, lote y cantidad para registrar la consulta.';
      return;
    }
    const fechaConsulta = `${this.consultation.fecha}T${new Date().toISOString().slice(11, 19)}`;
    this.pacienteService.crearConsulta({
      pacienteId: this.consultation.pacienteId,
      fechaConsulta,
      motivo: this.consultation.motivo
    }).subscribe({
      next: consulta => {
        this.consultas = [...this.consultas, {
          fecha: consulta.fechaConsulta.slice(0, 10),
          pacienteId: consulta.pacienteId,
          loteStockId: this.consultation.loteStockId,
          cantidad: this.consultation.cantidad,
          motivo: consulta.motivo || ''
        }];
        if (this.selectedMedicationId !== null && this.consultation.loteStockId > 0) {
          this.service.registrarMovimiento({
            loteStockId: this.consultation.loteStockId,
            cantidad: this.consultation.cantidad,
            tipo: 'SALIDA_CLINICA',
            animalId: undefined,
            explotacionId: undefined
          }).subscribe({
            next: () => this.load(),
            error: () => this.error = 'La consulta se creó, pero no se pudo registrar la salida clínica.'
          });
        } else {
          this.consultation = this.emptyConsultation();
          this.load();
        }
      },
      error: () => this.error = 'No se pudo registrar la consulta clínica.'
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
    this.loadPacientes();
    this.pacienteService.findConsultas().subscribe({
      next: consultas => this.consultas = consultas.map(consulta => ({
        fecha: consulta.fechaConsulta.slice(0, 10),
        pacienteId: consulta.pacienteId,
        loteStockId: 0,
        cantidad: 0,
        motivo: consulta.motivo || ''
      })),
      error: () => this.error = 'No se pudieron cargar las consultas clínicas.'
    });
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

  private loadPacientes(): void {
    this.pacienteService.findPacientes(100).subscribe({
      next: page => this.pacientes = page.content,
      error: () => this.error = 'No se pudieron cargar los pacientes de clínica.'
    });
  }

  private emptyConsultation(): ConsultaReceta {
    return {
      fecha: new Date().toISOString().slice(0, 10),
      pacienteId: 0,
      loteStockId: 0,
      cantidad: 0,
      motivo: ''
    };
  }
}
