import { Injectable, inject, signal } from '@angular/core';
import { GanaderiaOperacionCampo } from './ganaderia-hierarchy.models';
import { GanaderiaHierarchyService } from './ganaderia-hierarchy.service';

@Injectable({ providedIn: 'root' })
export class GanaderiaOfflineService {
  private readonly hierarchy = inject(GanaderiaHierarchyService);
  private readonly storageKey = 'ganaderia-offline-queue';
  readonly pending = signal<GanaderiaOperacionCampo[]>(this.readQueue());

  enqueue(operation: Omit<GanaderiaOperacionCampo, 'operationId' | 'createdAt' | 'sincronizada'>): void {
    const queued: GanaderiaOperacionCampo = {
      ...operation,
      operationId: crypto.randomUUID(),
      createdAt: new Date().toISOString(),
      sincronizada: false
    };
    const next = [...this.pending(), queued];
    this.pending.set(next);
    localStorage.setItem(this.storageKey, JSON.stringify(next));
  }

  syncPending(): void {
    if (!navigator.onLine || this.pending().length === 0) return;
    this.hierarchy.syncOperacionesCampo(this.pending()).subscribe({
      next: () => {
        this.pending.set([]);
        localStorage.removeItem(this.storageKey);
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
