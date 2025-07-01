import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface FuncionarioOpsDTO {
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  especialidade?: string;
  crm?: string;
}

export interface FuncionarioOpsResponseDTO {
  id: number;
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  especialidade?: string;
  crm?: string;
  status: string;
  dataCadastro: string;
  dataInativacao?: string;
}

@Injectable({
  providedIn: 'root'
})
export class FuncionarioOpsService {
  private readonly API_BASE_URL = 'http://localhost:3000/api/func-ops';
  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient) { }

  /**
   * Creates operational employee record in ms-consulta
   * This is the second step of the dual-record pattern
   * @param funcionario Employee operational data
   * @returns Observable with created operational employee data
   */
  createOperationalEmployee(funcionario: FuncionarioOpsDTO): Observable<FuncionarioOpsResponseDTO> {
    this.loadingSubject.next(true);

    return this.http.post<FuncionarioOpsResponseDTO>(this.API_BASE_URL, funcionario)
      .pipe(
        tap(() => this.loadingSubject.next(false)),
        tap({
          error: () => this.loadingSubject.next(false)
        })
      );
  }

  /**
   * Gets error message from HTTP error response
   * @param error HTTP error response
   * @returns User-friendly error message
   */
  getErrorMessage(error: any): string {
    if (error.status === 409) {
      return error.error?.message || 'CPF ou Email já cadastrado no sistema de consultas';
    }

    if (error.status === 400) {
      return error.error?.message || 'Dados inválidos para o registro operacional. Verifique os campos';
    }

    if (error.status === 404) {
      return 'Serviço de consultas não encontrado';
    }

    if (error.status === 500) {
      return 'Sistema de consultas temporariamente indisponível. Tente novamente em alguns minutos';
    }

    if (error.status === 0) {
      return 'Falha na conexão com o sistema de consultas. Verifique sua internet';
    }

    return error.error?.message || 'Erro inesperado no sistema de consultas. Tente novamente';
  }
}
