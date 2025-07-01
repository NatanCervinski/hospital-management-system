import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { ConsultaService, ConsultaResponseDTO } from '../../../services/consulta.service';
import { ConsultaFormComponent } from '../form/consulta-form.component';

@Component({
  selector: 'app-consulta-list',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, ConsultaFormComponent],
  templateUrl: './consulta-list.component.html',
  styleUrls: ['./consulta-list.component.scss']
})
export class ConsultaListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  allConsultas: ConsultaResponseDTO[] = [];
  filteredConsultas: ConsultaResponseDTO[] = [];
  loading = true;
  error: string | null = null;
  showCreateModal = false;
  
  filterForm: FormGroup;
  availableEspecialidades: string[] = [];
  availableMedicos: string[] = [];

  constructor(
    private consultaService: ConsultaService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.filterForm = this.createFilterForm();
  }

  ngOnInit(): void {
    // Subscribe to loading state from service
    this.consultaService.loading$
      .pipe(takeUntil(this.destroy$))
      .subscribe(loading => {
        this.loading = loading;
      });

    // Subscribe to error state from service  
    this.consultaService.error$
      .pipe(takeUntil(this.destroy$))
      .subscribe(error => {
        this.error = error;
      });

    // Load all consultations including canceled ones
    this.loadConsultas();

    // Load specialties and doctors
    this.loadEspecialidades();
    this.loadMedicos();

    // Set up filter form subscriptions
    this.setupFilterSubscriptions();
  }

  private createFilterForm(): FormGroup {
    return this.fb.group({
      status: ['all'],
      especialidade: ['all'],
      medico: ['all'],
      searchText: ['']
    });
  }

  private setupFilterSubscriptions(): void {
    // Subscribe to filter changes with debounce for search text
    this.filterForm.valueChanges
      .pipe(
        takeUntil(this.destroy$),
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe(() => {
        this.applyFilters();
      });
  }

  private loadConsultas(): void {
    // Try to get all consultations including canceled ones
    this.consultaService.buscarTodasConsultas()
      .pipe(takeUntil(this.destroy$))
      .subscribe(consultas => {
        this.allConsultas = consultas;
        this.filteredConsultas = [...consultas];
        this.extractFilterOptions();
        this.applyFilters();
      });
  }

  private loadEspecialidades(): void {
    this.consultaService.getEspecialidades()
      .pipe(takeUntil(this.destroy$))
      .subscribe(especialidades => {
        this.availableEspecialidades = especialidades.map(e => e.nome).sort();
      });
  }

  private loadMedicos(): void {
    this.consultaService.getMedicos()
      .pipe(takeUntil(this.destroy$))
      .subscribe(medicos => {
        this.availableMedicos = medicos.map(m => m.nome).sort();
      });
  }

  private extractFilterOptions(): void {
    // No longer needed as specialties and doctors are fetched from backend
  }

  private applyFilters(): void {
    const filters = this.filterForm.value;
    
    this.filteredConsultas = this.allConsultas.filter(consulta => {
      // Status filter
      if (filters.status !== 'all' && consulta.status !== filters.status) {
        return false;
      }

      // Specialty filter
      if (filters.especialidade !== 'all' && consulta.especialidade !== filters.especialidade) {
        return false;
      }

      // Doctor filter
      if (filters.medico !== 'all' && consulta.medico !== filters.medico) {
        return false;
      }

      // Search text filter (searches in codigo, especialidade, medico)
      if (filters.searchText && filters.searchText.trim()) {
        const searchText = filters.searchText.toLowerCase().trim();
        const searchFields = [
          consulta.codigo,
          consulta.especialidade,
          consulta.medico
        ].join(' ').toLowerCase();
        
        if (!searchFields.includes(searchText)) {
          return false;
        }
      }

      return true;
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
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
   * Refresh consultation list
   */
  refresh(): void {
    this.consultaService.clearError();
    this.loadConsultas();
  }

  /**
   * Clear all filters
   */
  clearFilters(): void {
    this.filterForm.reset({
      status: 'all',
      especialidade: 'all',
      medico: 'all',
      searchText: ''
    });
  }

  /**
   * Get filtered consultation count
   */
  get filteredCount(): number {
    return this.filteredConsultas.length;
  }

  /**
   * Get total consultation count
   */
  get totalCount(): number {
    return this.allConsultas.length;
  }

  /**
   * Manage consultation - navigate to detail page
   */
  gerenciarConsulta(consultaId: number): void {
    this.router.navigate(['/consultas', consultaId]);
  }

  /**
   * Create new consultation - open modal
   */
  cadastrarNovaConsulta(): void {
    this.showCreateModal = true;
  }

  /**
   * Handle consultation created event
   */
  onConsultaCreated(): void {
    this.showCreateModal = false;
    this.refresh(); // Refresh the list to show the new consultation
    this.showSuccessMessage('Consulta criada com sucesso!');
  }

  /**
   * Get consultas for display (filtered)
   */
  get consultas(): ConsultaResponseDTO[] {
    return this.filteredConsultas;
  }

  /**
   * Navigate back to dashboard
   */
  voltarAoDashboard(): void {
    this.router.navigate(['/admin/dashboard']);
  }

  /**
   * Show success message (placeholder for toast implementation)
   */
  private showSuccessMessage(message: string): void {
    // TODO: Implement toast notification service
    console.log('Success:', message);
  }
}
