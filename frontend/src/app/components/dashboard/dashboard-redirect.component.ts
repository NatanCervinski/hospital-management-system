import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard-redirect',
  template: `
    <div class="d-flex justify-content-center align-items-center" style="min-height: 200px;">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Redirecionando...</span>
      </div>
    </div>
  `,
  standalone: true
})
export class DashboardRedirectComponent implements OnInit {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.redirectToDashboard();
  }

  private redirectToDashboard(): void {
    const userType = this.authService.getUserType();
    console.log('User type:', userType);
    
    if (userType === 'FUNCIONARIO') {
      this.router.navigate(['/admin/dashboard']);
    } else if (userType === 'PACIENTE') {
      this.router.navigate(['/dashboard/paciente']);
    } else {
      console.log('No valid user type, redirecting to login');
      this.authService.logout();
      this.router.navigate(['/login']);
    }
  }
}