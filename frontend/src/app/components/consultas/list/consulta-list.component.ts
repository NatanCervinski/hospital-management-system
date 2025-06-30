import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { ConsultaService, ConsultaResponseDTO } from '../../../services/consulta.service';

@Component({
  selector: 'app-consulta-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './consulta-list.component.html',
  styleUrls: ['./consulta-list.component.scss']
})
export class ConsultaListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  consultas: ConsultaResponseDTO[] = [];
  loading = true;
  error: string | null = null;

  constructor(private consultaService: ConsultaService) {}

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

    // Load dashboard consultations using the new method
    this.consultaService.getConsultasDashboard()
      .pipe(takeUntil(this.destroy$))
      .subscribe(consultas => {
        this.consultas = consultas;
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
    this.consultaService.getConsultasDashboard()
      .pipe(takeUntil(this.destroy$))
      .subscribe(consultas => {
        this.consultas = consultas;
      });
  }
}
