import { CanDeactivateFn } from '@angular/router';
import { ClinicaPacienteAltaComponent } from '../clinica-paciente-alta.component';

export const clinicaPacienteAltaGuard: CanDeactivateFn<ClinicaPacienteAltaComponent> =
  (component): boolean => {
    if (!component.dirty()) {
      return true;
    }
    return window.confirm('Hay datos sin guardar. ¿Quieres salir del alta de mascota?');
  };
