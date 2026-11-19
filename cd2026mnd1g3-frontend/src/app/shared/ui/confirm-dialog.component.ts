import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  template: `<div class="backdrop" role="presentation" (click)="cancel.emit()">
    <section class="dialog" role="dialog" aria-modal="true" aria-labelledby="confirm-title" (click)="$event.stopPropagation()">
      <h2 id="confirm-title">{{ title() }}</h2>
      <p>{{ message() }}</p>
      <div class="actions">
        <button type="button" class="secondary" (click)="cancel.emit()">Cancelar</button>
        <button type="button" class="primary" (click)="confirm.emit()">Confirmar</button>
      </div>
    </section>
  </div>
  <style>.backdrop{position:fixed;z-index:1000;inset:0;display:grid;place-items:center;padding:16px;background:#17352d80}.dialog{width:min(420px,100%);border-radius:16px;background:#fff;padding:24px;color:#17352d}.dialog h2{margin-top:0}.actions{display:flex;justify-content:flex-end;gap:10px}.actions button{min-height:44px;border-radius:9px;padding:10px 14px;font-weight:800}.secondary{border:1px solid #b9cbc3;background:#fff;color:#17352d}.primary{border:0;background:#0d6e52;color:#fff}</style>`
})
export class ConfirmDialogComponent {
  readonly title = input<string>('Confirmar acción');
  readonly message = input<string>('Esta acción debe confirmarse.');
  readonly confirm = output<void>();
  readonly cancel = output<void>();
}
