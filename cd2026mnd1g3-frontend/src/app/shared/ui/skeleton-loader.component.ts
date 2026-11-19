import { Component, input } from '@angular/core';

@Component({
  selector: 'app-skeleton-loader',
  standalone: true,
  template: `<div class="skeleton" [style.height]="height()" aria-label="Cargando"></div>
    <style>.skeleton{width:100%;border-radius:10px;background:linear-gradient(90deg,#edf2ef 25%,#dfe9e4 50%,#edf2ef 75%);background-size:200% 100%;animation:pulse 1.4s infinite}@keyframes pulse{to{background-position:-200% 0}}</style>`
})
export class SkeletonLoaderComponent {
  readonly height = input<string>('96px');
}
