import { Component, input } from '@angular/core';

@Component({
  selector: 'app-toast-notification',
  standalone: true,
  template: `<div class="toast" role="status" [hidden]="!message()">{{ message() }}</div>
  <style>.toast{position:fixed;z-index:1100;right:16px;bottom:16px;max-width:min(360px,calc(100vw - 32px));border-radius:10px;background:#17352d;color:#fff;padding:14px 16px;box-shadow:0 12px 30px #17352d33;font-weight:800}</style>`
})
export class ToastNotificationComponent {
  readonly message = input<string>('');
}
