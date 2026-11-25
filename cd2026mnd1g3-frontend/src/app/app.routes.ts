import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { LoginComponent } from './auth/login.component';
import { ItWorksComponent } from './main/it-works.component';
import { PortalSelectorComponent } from './main/portal-selector.component';
import { ClinicaShellComponent } from './main/clinica-shell.component';
import { ClinicaInicioComponent } from './main/clinica-inicio.component';
import { ClinicaInventarioComponent } from './main/clinica-inventario.component';
import { ClinicaPacienteAltaComponent } from './main/clinica-paciente-alta.component';
import { ClinicaPacientesComponent } from './main/clinica-pacientes.component';
import { ClinicaPacienteFichaComponent } from './main/clinica-paciente-ficha.component';
import { clinicaPacienteAltaGuard } from './main/guards/clinica-paciente-alta.guard';
import { ClinicaAvisosComponent } from './main/clinica-avisos.component';
import { GanaderiaDirectorioComponent } from './main/ganaderia-directorio.component';
import { GanaderiaExplotacionComponent } from './main/ganaderia-explotacion.component';
import { GanaderiaHudComponent } from './main/ganaderia-hud.component';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{ path: 'it-works', component: ItWorksComponent, canActivate: [authGuard] },
	{ path: 'dashboard', redirectTo: 'ganaderia', pathMatch: 'full' },
	{
		path: 'ganaderia',
		component: GanaderiaHudComponent,
		canActivate: [authGuard],
		children: [
			{ path: '', component: GanaderiaDirectorioComponent, pathMatch: 'full' },
			{ path: 'directorio', redirectTo: '', pathMatch: 'full' },
			{ path: 'centro-control', redirectTo: '', pathMatch: 'full' },
			{ path: 'explotaciones/:farmId', component: GanaderiaExplotacionComponent }
		]
	},
	{
		path: 'clinica',
		component: ClinicaShellComponent,
		canActivate: [authGuard],
		children: [
			{ path: '', component: ClinicaInicioComponent, pathMatch: 'full' },
			{ path: 'pacientes', component: ClinicaPacientesComponent },
			{ path: 'pacientes/nueva', component: ClinicaPacienteAltaComponent, canDeactivate: [clinicaPacienteAltaGuard] },
			{ path: 'pacientes/alta', redirectTo: 'pacientes/nueva', pathMatch: 'full' },
			{ path: 'pacientes/:id', component: ClinicaPacienteFichaComponent },
			{ path: 'avisos', component: ClinicaAvisosComponent },
			{ path: 'inventario', component: ClinicaInventarioComponent }
		]
	},
	{ path: 'portal', component: PortalSelectorComponent, canActivate: [authGuard] },
	{ path: '', pathMatch: 'full', redirectTo: 'login' },
	{ path: '**', redirectTo: 'login' }
];
