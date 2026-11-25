import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import { AvisoClinicoService } from '../core/aviso-clinico.service';
import { ClinicaPacienteService } from '../core/clinica-paciente.service';
import { LoteStock, LivestockService, Medicamento, MovimientoInventario } from '../core/livestock.service';

/** Portada de clínica con accesos rápidos y contadores disponibles. */
@Component({
  selector: 'app-clinica-inicio',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterLink],
  templateUrl: './clinica-inicio.component.html',
  styleUrl: './clinica-inicio.component.scss'
})
export class ClinicaInicioComponent {
  private readonly patientsService = inject(ClinicaPacienteService);
  private readonly noticesService = inject(AvisoClinicoService);
  private readonly inventoryService = inject(LivestockService);
  private readonly destroyRef = inject(DestroyRef);

  readonly patientCount = signal<number | null>(null);
  readonly noticeCount = signal<number | null>(null);
  readonly lowStockCount = signal<number | null>(null);

  constructor() {
    this.patientsService.findPacientes('', 0, 1).pipe(
      catchError(() => of(null)),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(result => this.patientCount.set(result?.totalElements ?? null));

    this.noticesService.find(0, 1).pipe(
      catchError(() => of(null)),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(result => this.noticeCount.set(result?.totalElements ?? null));

    this.loadLowStockCount();
  }

  private loadLowStockCount(): void {
    this.inventoryService.medicamentos().pipe(
      takeUntilDestroyed(this.destroyRef),
      catchError(() => of(null))
    ).subscribe(medications => {
      if (!medications) return;
      this.inventoryService.lotes().pipe(
        takeUntilDestroyed(this.destroyRef),
        catchError(() => of(null))
      ).subscribe(lots => {
        if (!lots) return;
        this.inventoryService.movimientos().pipe(
          takeUntilDestroyed(this.destroyRef),
          catchError(() => of(null))
        ).subscribe(movements => {
          if (!movements) return;
          this.lowStockCount.set(this.countLowStock(medications, lots, movements));
        });
      });
    });
  }

  private countLowStock(
    medications: Medicamento[],
    lots: LoteStock[],
    movements: MovimientoInventario[]
  ): number {
    const stockForLot = (lotId: number): number => movements
      .filter(movement => movement.loteStockId === lotId)
      .reduce((total, movement) => total + movement.cantidad, 0);
    return medications.filter(medication => lots
      .filter(lot => lot.medicamentoId === medication.id)
      .reduce((stock, lot) => stock + stockForLot(lot.id), 0)
      < medication.stockMinimoAlerta).length;
  }
}
