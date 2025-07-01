import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, takeUntil, forkJoin } from 'rxjs';
import { ConsultaService, ConsultaResponseDTO, AgendamentoResponseDTO } from '../../../services/consulta.service';

@Component({
  selector: 'app-consulta-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './consulta-detail.component.html',
  styleUrls: ['./consulta-detail.component.scss']
})
export class ConsultaDetailComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  consulta: ConsultaResponseDTO | null = null;
  agendamentos: AgendamentoResponseDTO[] = [];

  loading = true;
  error: string | null = null;
  actionLoading = false;

  confirmAttendanceForm: FormGroup;
  showConfirmModal = false;
  confirmAction: 'cancel' | 'finalize' | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private consultaService: ConsultaService,
    private fb: FormBuilder
  ) {
    this.confirmAttendanceForm = this.createConfirmForm();
  }

  ngOnInit(): void {
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const consultaId = +params['id'];
      if (consultaId) {
        this.loadConsultaData(consultaId);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private createConfirmForm(): FormGroup {
    return this.fb.group({
      codigo: ['', [Validators.required, Validators.pattern(/^AGD\d+$/)]]
    });
  }

  /**
   * Load consultation data and bookings
   */
  private loadConsultaData(consultaId: number): void {
    this.loading = true;
    this.error = null;

    forkJoin({
      consulta: this.consultaService.buscarConsultaPorId(consultaId),
      agendamentos: this.consultaService.buscarAgendamentosPorConsulta(consultaId)
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: (data) => {
        this.consulta = data.consulta;
        this.agendamentos = data.agendamentos;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Erro ao carregar dados da consulta.';
        this.loading = false;
        console.error('Error loading consultation data:', error);
      }
    });
  }

  /**
   * Refresh consultation data
   */
  refresh(): void {
    if (this.consulta) {
      this.loadConsultaData(this.consulta.id);
    }
  }

  /**
   * Navigate back to consultation list
   */
  goBack(): void {
    this.router.navigate(['/consultas']);
  }

  /**
   * Confirm patient attendance (R09)
   */
  confirmarComparecimento(): void {
    if (this.confirmAttendanceForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.actionLoading = true;
    const codigo = this.confirmAttendanceForm.value.codigo;

    this.consultaService.confirmarComparecimento(codigo).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        this.showSuccessMessage('Comparecimento confirmado com sucesso!');
        this.confirmAttendanceForm.reset();
        this.refresh();
      },
      error: (error) => {
        this.showErrorMessage('Erro ao confirmar comparecimento. Verifique o código e tente novamente.');
        console.error('Error confirming attendance:', error);
      },
      complete: () => {
        this.actionLoading = false;
      }
    });
  }

  /**
   * Show confirmation modal for cancel action
   */
  showCancelConfirmation(): void {
    this.confirmAction = 'cancel';
    this.showConfirmModal = true;
  }

  /**
   * Show confirmation modal for finalize action
   */
  showFinalizeConfirmation(): void {
    this.confirmAction = 'finalize';
    this.showConfirmModal = true;
  }

  /**
   * Execute confirmed action
   */
  executeConfirmedAction(): void {
    if (!this.consulta || !this.confirmAction) return;

    this.actionLoading = true;

    let action$;
    let successMessage: string;

    if (this.confirmAction === 'cancel') {
      action$ = this.consultaService.cancelarConsulta(this.consulta.id);
      successMessage = 'Consulta cancelada com sucesso!';
    } else {
      action$ = this.consultaService.realizarConsulta(this.consulta.id);
      successMessage = 'Consulta finalizada com sucesso!';
    }

    action$.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.showSuccessMessage(successMessage);
        this.closeConfirmModal();
        this.refresh();
      },
      error: (error) => {
        const errorMessage = this.confirmAction === 'cancel'
          ? 'Erro ao cancelar consulta. Verifique se a taxa de ocupação permite cancelamento.'
          : 'Erro ao finalizar consulta. Tente novamente.';
        this.showErrorMessage(errorMessage);
        console.error('Error executing action:', error);
      },
      complete: () => {
        this.actionLoading = false;
      }
    });
  }

  /**
   * Close confirmation modal
   */
  closeConfirmModal(): void {
    this.showConfirmModal = false;
    this.confirmAction = null;
  }

  /**
   * Check if consultation can be cancelled (business rule: < 50% occupancy)
   */
  canCancelConsulta(): boolean {
    return this.consulta?.status === 'DISPONIVEL' && this.consulta?.taxaOcupacao < 0.5;
  }

  /**
   * Check if consultation can be finalized
   */
  canFinalizeConsulta(): boolean {
    return this.consulta?.status === 'DISPONIVEL';
  }

  /**
   * Format date/time for display
   */
  formatDateTime(dateTimeString: string): string {
    return this.consultaService.formatDateTime(dateTimeString);
  }

  /**
   * Format currency for display
   */
  formatCurrency(value: number): string {
    return this.consultaService.formatCurrency(value);
  }

  /**
   * Format occupancy rate as percentage
   */
  formatTaxaOcupacao(taxaOcupacao: number): string {
    return this.consultaService.formatTaxaOcupacao(taxaOcupacao);
  }

  /**
   * Get status badge class
   */
  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'DISPONIVEL': return 'bg-success';
      case 'CANCELADA': return 'bg-danger';
      case 'REALIZADA': return 'bg-secondary';
      default: return 'bg-primary';
    }
  }

  /**
   * Get booking status badge class
   */
  getBookingStatusBadgeClass(status: string): string {
    switch (status) {
      case 'CRIADO': return 'bg-primary';
      case 'CHECK_IN': return 'bg-warning';
      case 'COMPARECEU': return 'bg-success';
      case 'FALTOU': return 'bg-danger';
      case 'REALIZADO': return 'bg-secondary';
      case 'CANCELADO': return 'bg-dark';
      default: return 'bg-light text-dark';
    }
  }

  /**
   * Get confirmation modal title
   */
  getConfirmModalTitle(): string {
    return this.confirmAction === 'cancel' ? 'Confirmar Cancelamento' : 'Confirmar Finalização';
  }

  /**
   * Get confirmation modal message
   */
  getConfirmModalMessage(): string {
    if (this.confirmAction === 'cancel') {
      return 'Tem certeza que deseja cancelar esta consulta? Esta ação não pode ser desfeita e todos os agendamentos serão cancelados.';
    }
    return 'Tem certeza que deseja finalizar esta consulta? Todos os agendamentos ativos serão marcados como realizados.';
  }

  /**
   * Show success message (placeholder for toast implementation)
   */
  private showSuccessMessage(message: string): void {
    // TODO: Implement toast notification service
    console.log('Success:', message);
  }

  /**
   * Show error message (placeholder for toast implementation)
   */
  private showErrorMessage(message: string): void {
    // TODO: Implement toast notification service
    console.error('Error:', message);
  }

  /**
   * Mark all form fields as touched
   */
  private markFormGroupTouched(): void {
    Object.keys(this.confirmAttendanceForm.controls).forEach(key => {
      const control = this.confirmAttendanceForm.get(key);
      control?.markAsTouched();
    });
  }

  /**
   * Check if field has error and is touched
   */
  hasError(fieldName: string): boolean {
    const field = this.confirmAttendanceForm.get(fieldName);
    return field ? field.invalid && field.touched : false;
  }

  /**
   * Get error message for field
   */
  getErrorMessage(fieldName: string): string {
    const field = this.confirmAttendanceForm.get(fieldName);
    if (!field || !field.errors || !field.touched) return '';

    const errors = field.errors;

    if (errors['required']) {
      return 'Código do agendamento é obrigatório';
    }

    if (errors['pattern']) {
      return 'Código deve ter o formato AGD seguido de números';
    }

    return 'Campo inválido';
  }
}
