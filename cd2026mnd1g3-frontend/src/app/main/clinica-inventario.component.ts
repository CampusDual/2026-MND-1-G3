import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { httpErrorMessage } from '../core/http-error-message';
import {
  LoteStock,
  LivestockService,
  Medicamento,
  MovimientoInventario
} from '../core/livestock.service';

/** Gestión del stock clínico y su libro mayor. */
@Component({
  selector: 'app-clinica-inventario',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule],
  templateUrl: './clinica-inventario.component.html',
  styleUrl: './clinica-inventario.component.scss'
})
export class ClinicaInventarioComponent implements OnInit {
  private readonly service = inject(LivestockService);
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = signal(false);
  readonly error = signal('');
  readonly medicamentos = signal<Medicamento[]>([]);
  readonly lotes = signal<LoteStock[]>([]);
  readonly movimientos = signal<MovimientoInventario[]>([]);
  readonly entryLotId = signal<number | null>(null);
  readonly entryQuantity = signal(0);

  ngOnInit(): void {
    this.load();
  }

  stockOfLot(lotId: number): number {
    return this.movimientos()
      .filter(movement => movement.loteStockId === lotId)
      .reduce((total, movement) => total + movement.cantidad, 0);
  }

  stockOfMedication(medicamentoId: number): number {
    return this.lotes()
      .filter(lot => lot.medicamentoId === medicamentoId)
      .reduce((total, lot) => total + this.stockOfLot(lot.id), 0);
  }

  loteNumber(lotId: number): string {
    return this.lotes().find(lot => lot.id === lotId)?.numeroLote ?? String(lotId);
  }

  registerPurchaseEntry(): void {
    const lotId = this.entryLotId();
    const quantity = this.entryQuantity();
    if (!lotId || quantity <= 0) {
      this.error.set('Selecciona un lote y una cantidad válida.');
      return;
    }
    this.error.set('');
    this.service.registrarMovimiento({
      loteStockId: lotId,
      cantidad: quantity,
      tipo: 'ENTRADA_COMPRA'
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.entryQuantity.set(0);
        this.entryLotId.set(null);
        this.load();
      },
      error: error => this.error.set(httpErrorMessage(error, 'No se pudo registrar la entrada de compra.'))
    });
  }

  private load(): void {
    this.loading.set(true);
    this.error.set('');
    forkJoin({
      medications: this.service.medicamentos(),
      lots: this.service.lotes(),
      movements: this.service.movimientos()
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: data => {
        this.medicamentos.set(data.medications);
        this.lotes.set(data.lots);
        this.movimientos.set(data.movements);
        this.loading.set(false);
      },
      error: error => {
        this.loading.set(false);
        this.error.set(httpErrorMessage(error, 'No se pudo cargar el inventario.'));
      }
    });
  }
}
