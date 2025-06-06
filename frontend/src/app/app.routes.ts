import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { FuncionarioDashboardComponent } from './components/dashboard/funcionario-dashboard/funcionario-dashboard.component';
import { PacienteDashboardComponent } from './components/dashboard/paciente-dashboard/paciente-dashboard.component';
import { authGuard, loginGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [loginGuard]
  },
  {
    path: 'dashboard/funcionario',
    component: FuncionarioDashboardComponent,
    canActivate: [authGuard]
  },
  {
    path: 'dashboard/paciente',
    component: PacienteDashboardComponent,
    canActivate: [authGuard]
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./components/dashboard/dashboard-redirect.component').then(m => m.DashboardRedirectComponent)
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];
