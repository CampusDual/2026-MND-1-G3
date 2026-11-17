import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface Appointment {
  time: string;
  patient: string;
  owner: string;
  type: string;
  status: string;
}

/**
 * Vista inicial del portal de clínicas veterinarias.
 */
@Component({
  selector: 'app-clinica-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './clinica-dashboard.component.html',
  styleUrl: './clinica-dashboard.component.scss'
})
export class ClinicaDashboardComponent {
  readonly appointments: Appointment[] = [
    { time: '09:00', patient: 'Luna', owner: 'María García', type: 'Revisión anual', status: 'Confirmada' },
    { time: '10:30', patient: 'Max', owner: 'Javier Ruiz', type: 'Consulta digestiva', status: 'Pendiente' },
    { time: '12:00', patient: 'Nala', owner: 'Ana Martín', type: 'Vacunación', status: 'Confirmada' },
    { time: '16:30', patient: 'Rocky', owner: 'Pablo Sanz', type: 'Seguimiento', status: 'Pendiente' }
  ];
}
