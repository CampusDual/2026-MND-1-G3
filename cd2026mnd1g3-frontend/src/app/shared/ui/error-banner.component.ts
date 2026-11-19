import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-error-banner',
  standalone: true,
  template: `<div class="error-banner" role="alert">
    <span>{{ message() }}</span>
    <button type="button" (click)="retry.emit()">Reintentar</button>
  </div>
  <style>.error-banner{display:flex;justify-content:space-between;align-items:center;gap:12px;margin:12px 0;padding:12px 14px;border:1px solid #d59a9a;border-radius:10px;background:#fff2f2;color:#802b2b}.error-banner button{min-height:44px;border:1px solid #802b2b;border-radius:9px;background:#fff;color:#802b2b;font-weight:800}</style>`
})
export class ErrorBannerComponent {
  readonly message = input<string>('No se pudieron cargar los datos.');
  readonly retry = output<void>();
}
