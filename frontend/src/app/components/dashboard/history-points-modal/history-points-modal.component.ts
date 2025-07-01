import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PacienteService } from '../../../services/paciente.service';
import { AuthService } from '../../../services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-history-points-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './history-points-modal.component.html',
  styleUrls: ['./history-points-modal.component.scss']
})
export class HistoryPointsModalComponent implements OnInit, OnDestroy {
  historico: any[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  private subscription = new Subscription();

  constructor(
    private pacienteService: PacienteService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (!user) {
      this.errorMessage = 'Usuário não autenticado.';
      return;
    }

    this.isLoading = true;
    this.subscription.add(
      this.pacienteService.getSaldoEHistorico(user.pacienteId).subscribe({
        next: (res) => {
          this.historico = res.historicoTransacoes || [];
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = 'Erro ao carregar histórico de transações.';
          console.error(err);
          this.isLoading = false;
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  formatarData(data: string): string {
    return new Date(data).toLocaleString('pt-BR');
  }
}
