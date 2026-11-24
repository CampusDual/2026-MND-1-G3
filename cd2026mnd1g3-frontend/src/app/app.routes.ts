import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { LoginComponent } from './auth/login.component';
import { ItWorksComponent } from './main/it-works.component';
import { DashboardComponent } from './main/dashboard.component';
import { PortalSelectorComponent } from './main/portal-selector.component';
import { ClinicaDashboardComponent } from './main/clinica-dashboard.component';
import { ClinicaPacienteAltaComponent } from './main/clinica-paciente-alta.component';
import { clinicaPacienteAltaGuard } from './main/guards/clinica-paciente-alta.guard';
import { ClinicaAvisosComponent } from './main/clinica-avisos.component';
import { GanaderiaDirectorioComponent } from './main/ganaderia-directorio.component';
import { GanaderiaExplotacionComponent } from './main/ganaderia-explotacion.component';
import { GanaderiaHudComponent } from './main/ganaderia-hud.component';
import { DashboardComponent as GanaderiaCentroControlComponent } from './main/dashboard.component';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{ path: 'it-works', component: ItWorksComponent, canActivate: [authGuard] },
	{ path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
	{
		path: 'ganaderia',
		component: GanaderiaHudComponent,
		canActivate: [authGuard],
		children: [
			{ path: '', component: GanaderiaDirectorioComponent, pathMatch: 'full' },
			{ path: 'directorio', redirectTo: '', pathMatch: 'full' },
			{ path: 'centro-control', component: GanaderiaCentroControlComponent },
			{ path: 'explotaciones/:farmId', component: GanaderiaExplotacionComponent }
		]
	},
	{ path: 'clinica', component: ClinicaDashboardComponent, canActivate: [authGuard] },
	{ path: 'clinica/pacientes/alta', component: ClinicaPacienteAltaComponent, canActivate: [authGuard], canDeactivate: [clinicaPacienteAltaGuard] },
	{ path: 'clinica/avisos', component: ClinicaAvisosComponent, canActivate: [authGuard] },
	{ path: 'portal', component: PortalSelectorComponent, canActivate: [authGuard] },
	{ path: '', pathMatch: 'full', redirectTo: 'login' },
	{ path: '**', redirectTo: 'login' }
];
