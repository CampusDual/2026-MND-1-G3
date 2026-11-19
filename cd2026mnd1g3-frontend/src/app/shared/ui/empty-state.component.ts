import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  template: `<div class="empty-state" role="status">
    <strong>{{ title() }}</strong>
    <p>{{ description() }}</p>
    <button type="button" (click)="action.emit()">{{ actionLabel() }}</button>
  </div>
  <style>.empty-state{min-height:140px;display:grid;place-items:center;align-content:center;text-align:center;padding:20px;border:1px dashed #b9cbc3;border-radius:12px;background:#fbfdfc}.empty-state p{margin:6px 0 14px;color:#667872}.empty-state button{min-height:44px;border:0;border-radius:9px;padding:10px 16px;background:#0d6e52;color:#fff;font-weight:800}</style>`
})
export class EmptyStateComponent {
  readonly title = input<string>('No hay datos');
  readonly description = input<string>('');
  readonly actionLabel = input<string>('Actualizar');
  readonly action = output<void>();
}
