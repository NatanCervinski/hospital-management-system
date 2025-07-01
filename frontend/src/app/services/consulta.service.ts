import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, catchError, finalize, of, map, forkJoin } from 'rxjs';
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

export interface ConsultaDTO {
  dataHora: string; // ISO string for LocalDateTime
  especialidade: string;
  medico: string;
  valor: number;
  vagas: number;
}

export interface MedicoDTO {
  id: number;
  nome: string;
  // adicione outros campos se necessário, como email ou cpf
}

export interface AgendamentoResponseDTO {
  id: number;
  codigoAgendamento: string;
  pacienteId: number;
  dataAgendamento: string;
  pontosUsados: number;
  valorPago: number;
  valorTotal: number;
  descontoPontos: number;
  status: 'CRIADO' | 'CHECK_IN' | 'COMPARECEU' | 'FALTOU' | 'REALIZADO' | 'CANCELADO';
  observacoes?: string;
  dataCheckin?: string;
  dataConfirmacao?: string;
  consulta: ConsultaResponseDTO;
}

export interface EspecialidadeDTO {
  codigo: string;
  nome: string;
}

@Injectable({ providedIn: 'root' })
export class ConsultaService {
  private readonly baseUrl = 'http://localhost:3000/api/consultas';
  private readonly agendamentoUrl = 'http://localhost:3000/api/agendamentos';

  private readonly medicosBaseUrl = 'http://localhost:3000/api/func-ops';
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

  // ... seu código existente ...


  /**
   * Get all consultations including canceled and finalized ones
   * Since backend may only return available ones, we'll combine dashboard and search results
   */
  buscarTodasConsultas(): Observable<ConsultaResponseDTO[]> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    // Try to get both dashboard (48h) and available consultations to get a broader view
    return forkJoin({
      dashboard: this.getConsultasDashboard().pipe(catchError(() => of([]))),
      disponiveis: this.http.get<ConsultaResponseDTO[]>(`${this.baseUrl}/buscar`).pipe(catchError(() => of([])))
    }).pipe(
      map(({ dashboard, disponiveis }) => {
        // Combine and deduplicate by ID
        const allConsultas = [...dashboard, ...disponiveis];
        const uniqueConsultas = allConsultas.filter((consulta, index, self) =>
          index === self.findIndex(c => c.id === consulta.id)
        );
        return uniqueConsultas;
      }),
      catchError(error => {
        console.error('Error searching all consultations:', error);
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
   * Create a new consultation (R12) - Employee only
   */
  criarConsulta(consultaDto: ConsultaDTO): Observable<ConsultaResponseDTO> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.post<ConsultaResponseDTO>(this.baseUrl, consultaDto).pipe(
      catchError(error => {
        console.error('Error creating consultation:', error);
        const errorMessage = this.getErrorMessage(error);
        this.errorSubject.next(errorMessage);
        throw error;
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  /**
   * Get a specific consultation by ID
   * Since backend doesn't have individual GET endpoint, we'll search in the available consultations
   */
  buscarConsultaPorId(consultaId: number): Observable<ConsultaResponseDTO> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.buscarConsultasDisponiveis().pipe(
      map(consultas => {
        const consulta = consultas.find(c => c.id === consultaId);
        if (!consulta) {
          throw new Error('Consulta não encontrada');
        }
        return consulta;
      }),
      catchError(error => {
        console.error('Error fetching consultation:', error);
        const errorMessage = this.getErrorMessage(error);
        this.errorSubject.next(errorMessage);
        throw error;
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  /**
   * Cancel consultation (R10) - Employee only
   */
  cancelarConsulta(consultaId: number): Observable<void> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.put<void>(`${this.baseUrl}/${consultaId}/cancelar`, {}).pipe(
      catchError(error => {
        console.error('Error cancelling consultation:', error);
        const errorMessage = this.getErrorMessage(error);
        this.errorSubject.next(errorMessage);
        throw error;
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  /**
   * Finalize consultation (R11) - Employee only
   */
  realizarConsulta(consultaId: number): Observable<void> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.put<void>(`${this.baseUrl}/${consultaId}/realizar`, {}).pipe(
      catchError(error => {
        console.error('Error finalizing consultation:', error);
        const errorMessage = this.getErrorMessage(error);
        this.errorSubject.next(errorMessage);
        throw error;
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  /**
   * Confirm patient attendance (R09) - Employee only
   */
  confirmarComparecimento(codigo: string): Observable<void> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.put<void>(`${this.baseUrl}/agendamento/confirmar`, {}, {
      params: { codigo }
    }).pipe(
      catchError(error => {
        console.error('Error confirming attendance:', error);
        const errorMessage = this.getErrorMessage(error);
        this.errorSubject.next(errorMessage);
        throw error;
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  /**
   * Get bookings for a specific consultation
   */
  buscarAgendamentosPorConsulta(consultaId: number): Observable<AgendamentoResponseDTO[]> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.get<AgendamentoResponseDTO[]>(`${this.agendamentoUrl}/consulta/${consultaId}`).pipe(
      catchError(error => {
        console.error('Error fetching consultation bookings:', error);
        const errorMessage = this.getErrorMessage(error);
        this.errorSubject.next(errorMessage);
        return of([]);
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  /**
   * Get available specialties for dropdown
   * Now fetches from backend instead of hardcoded list
   */
  getEspecialidades(): Observable<EspecialidadeDTO[]> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.get<EspecialidadeDTO[]>(`${this.baseUrl}/especialidades`).pipe(
      catchError(error => {
        console.error('Error fetching specialties:', error);
        const errorMessage = this.getErrorMessage(error);
        this.errorSubject.next(errorMessage);
        return of([]);
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  /**
   * Get active doctors for dropdowns
   */
  getMedicos(): Observable<MedicoDTO[]> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.http.get<MedicoDTO[]>(`${this.medicosBaseUrl}/medicos`).pipe(
      catchError(error => {
        console.error('Error fetching active doctors:', error);
        const errorMessage = this.getErrorMessage(error);
        this.errorSubject.next(errorMessage);
        return of([]); // Retorna um array vazio em caso de erro.
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }


  /**
   * Format currency value for display
   */
  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
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
