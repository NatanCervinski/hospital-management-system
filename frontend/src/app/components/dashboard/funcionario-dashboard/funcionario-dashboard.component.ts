import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';

import { AuthService, User } from '../../../services/auth.service';
import { ConsultaService, ConsultaResponseDTO } from '../../../services/consulta.service';

@Component({
  selector: 'app-funcionario-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './funcionario-dashboard.component.html',
  styleUrls: ['./funcionario-dashboard.component.scss']
})
export class FuncionarioDashboardComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  // Authentication state
  isAuthenticated = false;
  currentUser: User | null = null;
  
  // Consultation dashboard state
  isLoadingConsultas = false;
  consultas: ConsultaResponseDTO[] = [];
  consultasError: string | null = null;
  
  // General loading state
  isLoading = true;

  constructor(
    public router: Router,
    private authService: AuthService,
    private consultaService: ConsultaService
  ) {}

  ngOnInit(): void {
    // Subscribe to authentication state
    this.authService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        this.currentUser = user;
        this.isAuthenticated = !!user && user.tipo === 'FUNCIONARIO';
        this.isLoading = false;
        
        // Load dashboard data only if authenticated as employee
        if (this.isAuthenticated) {
          this.loadDashboardData();
        }
      });

    // Subscribe to consultation service loading state
    this.consultaService.loading$
      .pipe(takeUntil(this.destroy$))
      .subscribe(loading => {
        this.isLoadingConsultas = loading;
      });

    // Subscribe to consultation service error state
    this.consultaService.error$
      .pipe(takeUntil(this.destroy$))
      .subscribe(error => {
        this.consultasError = error;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load dashboard data - consultations for next 48 hours (R08)
   */
  loadDashboardData(): void {
    this.consultaService.getConsultasDashboard()
      .pipe(takeUntil(this.destroy$))
      .subscribe(consultas => {
        this.consultas = consultas;
      });
  }

  /**
   * Refresh dashboard data
   */
  refreshDashboard(): void {
    this.consultaService.clearError();
    this.loadDashboardData();
  }

  /**
   * Format date/time for display
   */
  formatDateTime(dateTimeString: string): string {
    return this.consultaService.formatDateTime(dateTimeString);
  }

  /**
   * Format occupancy rate as percentage
   */
  formatTaxaOcupacao(taxaOcupacao: number): string {
    return this.consultaService.formatTaxaOcupacao(taxaOcupacao);
  }

  /**
   * Get display name for current user
   */
  get userName(): string {
    return this.currentUser?.nome || 'Funcionário';
  }

  /**
   * Get email for current user
   */
  get userEmail(): string {
    return this.currentUser?.email || 'funcionario@hospital.com';
  }

  /**
   * Get user type for display
   */
  get userType(): string {
    return this.currentUser?.tipo || 'FUNCIONARIO';
  }

  /**
   * Handle manage consultation action
   */
  gerenciarConsulta(consulta: ConsultaResponseDTO): void {
    this.router.navigate(['/consultas', consulta.id]);
  }

  /**
   * Navigate to consultation management page
   */
  navegarParaConsultas(): void {
    this.router.navigate(['/consultas']);
  }

  /**
   * TrackBy function for consultation list optimization
   */
  trackByConsultaId(index: number, consulta: ConsultaResponseDTO): number {
    return consulta.id;
  }

  /**
   * Logout user
   */
  logout(): void {
    try {
      // AuthService.logout() is synchronous and returns void
      this.authService.logout();
      // Navigate to login page after successful logout
      this.router.navigate(['/login']);
    } catch (error) {
      console.error('Logout error:', error);
      // Even if logout fails, navigate to login page
      this.router.navigate(['/login']);
    }
  }
}
