import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';
import { LoginComponent } from './features/login/login.component';
import { DashboardComponent } from './features/ganaderia/dashboard.component';
import { PortalSelectorComponent } from './features/portal/portal-selector.component';
import { ClinicaDashboardComponent } from './features/clinica/clinica-dashboard.component';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{ path: 'ganaderia', component: DashboardComponent, canActivate: [authGuard, roleGuard(['FARM_MANAGER', 'VETERINARIAN', 'CLINIC_ADMIN'])] },
	{ path: 'clinica', component: ClinicaDashboardComponent, canActivate: [authGuard, roleGuard(['VETERINARIAN', 'CLINIC_ADMIN'])] },
	{ path: 'portal', component: PortalSelectorComponent, canActivate: [authGuard] },
	{ path: '', pathMatch: 'full', redirectTo: 'login' },
	{ path: '**', redirectTo: 'login' }
];
