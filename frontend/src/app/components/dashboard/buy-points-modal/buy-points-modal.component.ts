import { Component, OnInit, OnDestroy, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { PacienteService } from '../../../services/paciente.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-buy-points-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './buy-points-modal.component.html',
  styleUrl: './buy-points-modal.component.scss'
})
export class BuyPointsModalComponent implements OnInit, OnDestroy {
  @Output() purchaseSuccess = new EventEmitter<void>();

  buyPointsForm: FormGroup;
  valorCalculadoReais: number = 0;
  isLoading: boolean = false;
  errorMessage: string | null = null;
  private subscription = new Subscription();

  constructor(
    private pacienteService: PacienteService,
    private authService: AuthService
  ) {
    this.buyPointsForm = new FormGroup({
      quantidadePontos: new FormControl(1, [
        Validators.required,
        Validators.min(1),
        Validators.max(1000)
      ])
    });
  }

  ngOnInit(): void {
    this.subscription.add(
      this.buyPointsForm.get('quantidadePontos')!.valueChanges.subscribe(quantidade => {
        this.valorCalculadoReais = quantidade && quantidade > 0 ? quantidade * 5 : 0;
      })
    );

    // Valor inicial
    this.valorCalculadoReais = this.buyPointsForm.get('quantidadePontos')!.value * 5;
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  onSubmit(): void {
    if (this.buyPointsForm.valid) {
      this.isLoading = true;
      this.errorMessage = null;

      const user = this.authService.getCurrentUser();
      if (!user) {
        this.errorMessage = 'Usuário não autenticado';
        this.isLoading = false;
        return;
      }

      const valorReais = this.valorCalculadoReais;

      this.subscription.add(
        this.pacienteService.comprarPontos(user.pacienteId, valorReais).subscribe({
          next: () => {
            const quantidade = this.buyPointsForm.get('quantidadePontos')!.value;

            this.isLoading = false;
            this.purchaseSuccess.emit();
            this.resetForm();
            this.closeModal();

            setTimeout(() => {
              this.showToast(`Compra realizada com sucesso! ${quantidade} ${quantidade === 1 ? 'ponto' : 'pontos'} adicionados.`);
            }, 500);
          },
          error: (error) => {
            this.isLoading = false;
            console.error('Error buying points:', error);
            this.errorMessage = this.getErrorMessage(error);
          }
        })
      );
    } else {
      this.markFormGroupTouched();
    }
  }

  onCancel(): void {
    this.resetForm();
    this.closeModal();
  }

  private resetForm(): void {
    this.buyPointsForm.reset({ quantidadePontos: 1 });
    this.errorMessage = null;
    this.valorCalculadoReais = 5;
  }

  private closeModal(): void {
    const modalElement = document.getElementById('buyPointsModal');
    if (modalElement) {
      const modal = (window as any).bootstrap?.Modal?.getInstance(modalElement);
      if (modal) {
        modal.hide();
      }
    }
  }

  private markFormGroupTouched(): void {
    Object.keys(this.buyPointsForm.controls).forEach(key => {
      const control = this.buyPointsForm.get(key);
      control?.markAsTouched();
    });
  }

  private showToast(message: string): void {
    const toastEl = document.getElementById('toastCompraSucesso');
    if (toastEl) {
      const toastBody = toastEl.querySelector('.toast-body');
      if (toastBody) {
        toastBody.textContent = message;
      }
      const toastInstance = new (window as any).bootstrap.Toast(toastEl, { delay: 5000 });
      toastInstance.show();
    }
  }

  private getErrorMessage(error: any): string {
    if (error.status === 400) {
      return error.error?.message || 'Dados inválidos. Verifique os valores e tente novamente.';
    }
    if (error.status === 401) {
      return 'Sessão expirada. Faça login novamente.';
    }
    if (error.status === 500) {
      return 'Sistema temporariamente indisponível. Tente novamente em alguns minutos.';
    }
    if (error.status === 0) {
      return 'Falha na conexão. Verifique sua internet e tente novamente.';
    }
    return error.error?.message || 'Erro inesperado. Tente novamente.';
  }

  // Getters
  get quantidadePontos() {
    return this.buyPointsForm.get('quantidadePontos');
  }

  get isFormValid(): boolean {
    return this.buyPointsForm.valid && this.valorCalculadoReais > 0;
  }
}
