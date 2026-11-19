import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';

/**
 * Permite elegir el portal de trabajo de la plataforma.
 */
@Component({
  selector: 'app-portal-selector',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './portal-selector.component.html',
  styleUrl: './portal-selector.component.scss'
})
export class PortalSelectorComponent {
  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
