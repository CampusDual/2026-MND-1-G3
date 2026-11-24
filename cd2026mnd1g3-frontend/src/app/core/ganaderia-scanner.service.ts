import { Injectable, inject, signal } from '@angular/core';
import { GanaderiaHierarchyService } from './ganaderia-hierarchy.service';

@Injectable({ providedIn: 'root' })
export class GanaderiaScannerService {
  private readonly hierarchy = inject(GanaderiaHierarchyService);
  readonly active = signal(false);
  readonly input = signal('');
  readonly message = signal('');

  toggle(): void {
    this.active.update(value => !value);
    this.message.set(this.active()
      ? 'Escáner activo. Introduce un crotal y pulsa Enter.'
      : '');
  }

  clear(): void {
    this.input.set('');
  }
}
