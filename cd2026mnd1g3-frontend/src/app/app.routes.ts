import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { LoginComponent } from './auth/login.component';
import { ItWorksComponent } from './main/it-works.component';
import { DashboardComponent } from './main/dashboard.component';
import { PortalSelectorComponent } from './main/portal-selector.component';
import { ClinicaDashboardComponent } from './main/clinica-dashboard.component';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{ path: 'it-works', component: ItWorksComponent, canActivate: [authGuard] },
	{ path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
	{ path: 'ganaderia', component: DashboardComponent, canActivate: [authGuard] },
	{ path: 'clinica', component: ClinicaDashboardComponent, canActivate: [authGuard] },
	{ path: 'portal', component: PortalSelectorComponent, canActivate: [authGuard] },
	{ path: '', pathMatch: 'full', redirectTo: 'login' },
	{ path: '**', redirectTo: 'login' }
];
