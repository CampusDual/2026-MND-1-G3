import { Injectable, inject, signal } from '@angular/core';
import { Subject } from 'rxjs';
import { GanaderiaOperacionCampo } from './ganaderia-hierarchy.models';
import { GanaderiaHierarchyService } from './ganaderia-hierarchy.service';

@Injectable({ providedIn: 'root' })
export class GanaderiaOfflineService {
  private readonly hierarchy = inject(GanaderiaHierarchyService);
  private readonly storageKey = 'ganaderia-offline-queue';
  private syncing = false;
  readonly pending = signal<GanaderiaOperacionCampo[]>(this.readQueue());
  readonly synchronizedOperationIds = new Subject<string[]>();
  readonly syncErrors = new Subject<void>();

  enqueue(operation: Omit<GanaderiaOperacionCampo, 'operationId' | 'createdAt' | 'sincronizada'>): string {
    const queued: GanaderiaOperacionCampo = {
      ...operation,
      operationId: crypto.randomUUID(),
      createdAt: new Date().toISOString(),
      sincronizada: false
    };
    const next = [...this.pending(), queued];
    try {
      localStorage.setItem(this.storageKey, JSON.stringify(next));
    } catch (error: unknown) {
      throw new Error('No se pudo guardar la incidencia en la cola local.', { cause: error });
    }
    this.pending.set(next);
    return queued.operationId;
  }

  syncPending(): void {
    if (!navigator.onLine || this.syncing || this.pending().length === 0) return;
    this.syncing = true;
    const batch = [...this.pending()];
    const sentOperationIds = new Set(batch.map(operation => operation.operationId));
    this.hierarchy.syncOperacionesCampo(batch).subscribe({
      next: responses => {
        const confirmedIds = new Set(responses
          .filter(response => response.estado === 'SINCRONIZADA')
          .map(response => response.operationId)
          .filter(operationId => sentOperationIds.has(operationId)));
        const remaining = this.pending().filter(operation =>
          !confirmedIds.has(operation.operationId));
        try {
          if (remaining.length === 0) {
            localStorage.removeItem(this.storageKey);
          } else {
            localStorage.setItem(this.storageKey, JSON.stringify(remaining));
          }
          this.pending.set(remaining);
        } catch {
          this.syncErrors.next();
          this.syncing = false;
          return;
        }
        const unconfirmedIds = [...sentOperationIds].filter(id => !confirmedIds.has(id));
        if (unconfirmedIds.length) this.syncErrors.next();
        if (confirmedIds.size) this.synchronizedOperationIds.next([...confirmedIds]);
        this.syncing = false;
        if (!unconfirmedIds.length && navigator.onLine && remaining.length > 0) {
          this.syncPending();
        }
      },
      error: () => {
        this.syncErrors.next();
        this.syncing = false;
      }
    });
  }

  private readQueue(): GanaderiaOperacionCampo[] {
    try {
      const value = localStorage.getItem(this.storageKey);
      return value ? JSON.parse(value) as GanaderiaOperacionCampo[] : [];
    } catch {
      return [];
    }
  }
}
