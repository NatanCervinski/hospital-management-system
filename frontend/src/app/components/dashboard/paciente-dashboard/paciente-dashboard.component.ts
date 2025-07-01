import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService, User } from '../../../services/auth.service';
import { PacienteService } from '../../../services/paciente.service';
import { PacienteDetalhes, Agendamento } from '../../../interfaces/paciente.interfaces';
import { BuyPointsModalComponent } from '../buy-points-modal/buy-points-modal.component';
import { Subscription, forkJoin } from 'rxjs';
import { CommonModule } from '@angular/common';
import { HistoryPointsModalComponent } from '../history-points-modal/history-points-modal.component';
import { AgendarConsultaModalComponent } from '../agendar-consulta-modal/agendar-consulta-modal.component';

@Component({
  selector: 'app-paciente-dashboard',
  imports: [CommonModule, BuyPointsModalComponent, HistoryPointsModalComponent, AgendarConsultaModalComponent],
  templateUrl: './paciente-dashboard.component.html',
  styleUrl: './paciente-dashboard.component.scss'
})
export class PacienteDashboardComponent implements OnInit, OnDestroy {
  user: User | null = null;
  pacienteDetalhes: PacienteDetalhes | null = null;
  agendamentos: any[] = [];
  isLoading = true;
  isAuthenticated = false;
  error: string | null = null;
  private subscription = new Subscription();

  constructor(
    private authService: AuthService,
    private pacienteService: PacienteService,
    public router: Router
  ) { }

  ngOnInit(): void {
    // Get current user immediately from auth service
    this.user = this.authService.getCurrentUser();
    this.isAuthenticated = !!this.user;

    if (this.user) {
      this.loadPacienteData(this.user.pacienteId);
    } else {
      this.isLoading = false;
    }

    // Subscribe to user changes
    this.subscription.add(
      this.authService.currentUser$.subscribe({
        next: (user) => {
          this.user = user;
          this.isAuthenticated = !!user;
          if (user && !this.pacienteDetalhes) {
            this.loadPacienteData(user.pacienteId);
          } else if (!user) {
            this.isLoading = false;
          }
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  get userName(): string {
    return this.user?.nome || this.user?.email || 'Paciente';
  }

  get userEmail(): string {
    return this.user?.email || '';
  }

  private loadPacienteData(pacienteId: string): void {
    this.isLoading = true;
    this.error = null;

    this.subscription.add(
      forkJoin({
        detalhes: this.pacienteService.getPacienteDetalhes(pacienteId),
        agendamentos: this.pacienteService.getAgendamentos(pacienteId)
      }).subscribe({
        next: (data) => {
          this.pacienteDetalhes = data.detalhes;
          this.agendamentos = data.agendamentos;
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading patient data:', error);
          this.isLoading = false;
        }
      })
    );
  }

  isCheckInAvailable(agendamento: Agendamento): boolean {
    if (agendamento.status !== 'CRIADO') {
      return false;
    }

    const now = new Date();
    const appointmentDate = new Date(agendamento.consulta?.dataHora || '');
    const hoursDifference = (appointmentDate.getTime() - now.getTime()) / (1000 * 60 * 60);

    return hoursDifference <= 48 && hoursDifference >= 0;
  }

  cancelarAgendamento(agendamento: Agendamento): void {
    if (confirm(`Tem certeza que deseja cancelar o agendamento ${agendamento.codigoAgendamento}?`)) {
      this.subscription.add(
        this.pacienteService.cancelarAgendamento(agendamento.codigoAgendamento).subscribe({
          next: () => {
            agendamento.status = 'CANCELADO';
            alert('Agendamento cancelado com sucesso!');
          },
          error: (error) => {
            console.error('Error cancelling appointment:', error);
            alert('Erro ao cancelar agendamento. Tente novamente.');
          }
        })
      );
    }
  }

  fazerCheckin(agendamento: Agendamento): void {
    this.subscription.add(
      this.pacienteService.fazerCheckin(agendamento.codigoAgendamento).subscribe({
        next: () => {
          agendamento.status = 'CHECK-IN';
          alert('Check-in realizado com sucesso!');
        },
        error: (error) => {
          console.error('Error during check-in:', error);
          alert('Erro ao fazer check-in. Tente novamente.');
        }
      })
    );
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'CRIADO':
        return 'bg-primary';
      case 'CHECK-IN':
        return 'bg-info';
      case 'REALIZADO':
      case 'COMPARECEU':
        return 'bg-success';
      case 'CANCELADO':
        return 'bg-secondary';
      case 'FALTOU':
        return 'bg-danger';
      default:
        return 'bg-light text-dark';
    }
  }

  onPurchaseSuccess(): void {
    // Reload dashboard data to reflect updated points balance
    if (this.user) {
      this.loadPacienteData(this.user.pacienteId);
    }
  }
}
