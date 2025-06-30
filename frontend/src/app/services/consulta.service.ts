import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, catchError, finalize, of, map } from 'rxjs';
export interface ConsultaResponseDTO {
  id: number;
  codigo: string;
  dataHora: string; // LocalDateTime from backend comes as ISO string
  especialidade: string;
  medico: string;
  valor: number;
  vagas: number;
  vagasOcupadas: number;
  vagasDisponiveis: number;
  status: 'DISPONIVEL' | 'CANCELADA' | 'REALIZADA';
  dataCriacao: string;
  taxaOcupacao: number;
}

@Injectable({ providedIn: 'root' })
export class ConsultaService {
  private readonly baseUrl = 'http://localhost:3000/api/consultas';

  // Loading state management
  private readonly loadingSubject = new BehaviorSubject<boolean>(false);
  public readonly loading$ = this.loadingSubject.asObservable();

  // Error state management
  private readonly errorSubject = new BehaviorSubject<string | null>(null);
  public readonly error$ = this.errorSubject.asObservable();

  constructor(private http: HttpClient) { }

  /**
   * Get consultations for employee dashboard - next 48 hours (R08)
   * This matches the backend endpoint GET /consultas/dashboard
   */
  getConsultasDashboard(): Observable<ConsultaResponseDTO[]> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    // A MUDANÇA PRINCIPAL ESTÁ AQUI
    return this.http.get(`${this.baseUrl}/dashboard`, { responseType: 'text' }).pipe(
      map(responseText => {
        console.log('Dashboard response:', responseText);
        return responseText ? JSON.parse(responseText) : [];
      }),
      catchError(error => {
        // O catchError continua funcionando para erros reais de servidor (como 4xx ou 5xx).
        console.error('Error fetching dashboard consultations:', error);
        const errorMessage = this.getErrorMessage(error);
        this.errorSubject.next(errorMessage);
        return of([]); // Retorna um array vazio em caso de erro.
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }
  /**
   * Search available consultations
   */
  buscarConsultasDisponiveis(): Observable<ConsultaResponseDTO[]> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.get<ConsultaResponseDTO[]>(`${this.baseUrl}/buscar`).pipe(
      catchError(error => {
        console.error('Error searching consultations:', error);
        const errorMessage = this.getErrorMessage(error);
        this.errorSubject.next(errorMessage);
        return of([]);
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  /**
   * Search consultations by specialty
   */
  buscarPorEspecialidade(especialidade: string): Observable<ConsultaResponseDTO[]> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.get<ConsultaResponseDTO[]>(`${this.baseUrl}/buscar/especialidade/${especialidade}`).pipe(
      catchError(error => {
        console.error('Error searching consultations by specialty:', error);
        const errorMessage = this.getErrorMessage(error);
        this.errorSubject.next(errorMessage);
        return of([]);
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  /**
   * Search consultations by doctor name
   */
  buscarPorMedico(medico: string): Observable<ConsultaResponseDTO[]> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.get<ConsultaResponseDTO[]>(`${this.baseUrl}/buscar/medico`, {
      params: { medico }
    }).pipe(
      catchError(error => {
        console.error('Error searching consultations by doctor:', error);
        const errorMessage = this.getErrorMessage(error);
        this.errorSubject.next(errorMessage);
        return of([]);
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  /**
   * Clear any existing error state
   */
  clearError(): void {
    this.errorSubject.next(null);
  }

  /**
   * Format date/time from backend ISO string to readable Brazilian format
   */
  formatDateTime(dateTimeString: string): string {
    try {
      const date = new Date(dateTimeString);
      return date.toLocaleString('pt-BR', {
        day: '2-digit',
        month: 'long',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      console.error('Error formatting date:', error);
      return dateTimeString;
    }
  }

  /**
   * Format occupancy rate as percentage
   */
  formatTaxaOcupacao(taxaOcupacao: number): string {
    return `${(taxaOcupacao * 100).toFixed(0)}%`;
  }

  /**
   * Get user-friendly error message from HTTP error
   */
  private getErrorMessage(error: any): string {
    if (error.status === 0) {
      return 'Erro de conexão. Verifique sua internet e tente novamente.';
    }

    if (error.status === 401) {
      return 'Sessão expirada. Faça login novamente.';
    }

    if (error.status === 403) {
      return 'Você não tem permissão para acessar este recurso.';
    }

    if (error.status === 404) {
      return 'Recurso não encontrado.';
    }

    if (error.status >= 500) {
      return 'Erro interno do servidor. Tente novamente em alguns minutos.';
    }

    return error.error?.message || error.message || 'Erro desconhecido ocorreu.';
  }
}
