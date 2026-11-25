import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

/** Marco común de navegación de las pantallas de clínica. */
@Component({
  selector: 'app-clinica-shell',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './clinica-shell.component.html',
  styleUrl: './clinica-shell.component.scss'
})
export class ClinicaShellComponent {}
