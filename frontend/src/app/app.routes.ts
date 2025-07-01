import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { PatientRegistrationComponent } from './components/patient-registration/patient-registration.component';
import { FuncionarioDashboardComponent } from './components/dashboard/funcionario-dashboard/funcionario-dashboard.component';
import { PacienteDashboardComponent } from './components/dashboard/paciente-dashboard/paciente-dashboard.component';
import { AdminLayoutComponent } from './components/admin-layout/admin-layout.component';
import { FuncionarioPageComponent } from './components/funcionario-page/funcionario-page.component';
import { authGuard, loginGuard } from './guards/auth.guard';
import { ConsultaListComponent } from './components/consultas/list/consulta-list.component';
import { ConsultaDetailComponent } from './components/consultas/detail/consulta-detail.component';

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
    path: 'register/patient',
    component: PatientRegistrationComponent,
    canActivate: [loginGuard]
  },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'funcionarios',
        component: FuncionarioPageComponent
      },
      {
        path: 'dashboard',
        component: FuncionarioDashboardComponent
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: 'dashboard/funcionario',
    redirectTo: '/admin/dashboard',
    pathMatch: 'full'
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
    path: 'consultas',
    component: ConsultaListComponent,
    canActivate: [authGuard]
  },
  {
    path: 'consultas/:id',
    component: ConsultaDetailComponent,
    canActivate: [authGuard]
  },

  {
    path: '**',
    redirectTo: '/login'
  }
];
