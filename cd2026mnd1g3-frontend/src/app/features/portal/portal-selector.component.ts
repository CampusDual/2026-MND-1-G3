import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { WebSocketService } from '../../core/websocket/websocket.service';
import { AuthService } from '../../core/auth/auth.service';

/**
 * Permite elegir el portal de trabajo de la plataforma.
 */
@Component({
  selector: 'app-portal-selector',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './portal-selector.component.html',
  styleUrl: './portal-selector.component.scss'
})
export class PortalSelectorComponent {
  readonly authService = inject(AuthService);
  private readonly webSocketService = inject(WebSocketService);
  constructor(private readonly router: Router) {}

  logout(): void {
    this.webSocketService.disconnect();
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
