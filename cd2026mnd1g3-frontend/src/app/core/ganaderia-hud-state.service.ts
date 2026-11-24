import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class GanaderiaHudStateService {
  readonly activeFarmId = signal<number | null>(null);
  readonly activeFarmName = signal('');
  readonly activeGroupLabel = signal('');
  readonly selectedIds = signal<number[]>([]);
  readonly mode = signal<'clinica' | 'campo'>('clinica');
  readonly totalHeads = signal(0);
  readonly activeAlerts = signal(0);
  readonly pendingActions = signal(0);
  readonly visibleFarms = signal(0);
}
