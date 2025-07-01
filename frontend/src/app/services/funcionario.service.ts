import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import {
  FuncionarioResponseDTO,
  FuncionarioCadastroDTO,
  FuncionarioUpdateDTO,
  CustomPaginatedResponse
} from '../interfaces/funcionario.interfaces';

@Injectable({
  providedIn: 'root'
})
export class FuncionarioService {
  private readonly API_BASE_URL = 'http://localhost:3000/api/funcionarios';
  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient) { }

  /**
   * Creates a new employee
   * @param funcionario Employee data to create
   * @returns Observable with created employee data
   */
  createFuncionario(funcionario: FuncionarioCadastroDTO): Observable<FuncionarioResponseDTO> {
    this.loadingSubject.next(true);

    return this.http.post<FuncionarioResponseDTO>(this.API_BASE_URL, funcionario)
      .pipe(
        tap(() => this.loadingSubject.next(false)),
        tap({
          error: () => this.loadingSubject.next(false)
        })
      );
  }

  /**
   * Gets list of all employees
   * @returns Observable with array of employees
   */
  getFuncionarios(): Observable<CustomPaginatedResponse<FuncionarioResponseDTO>> {
    // O tipo de retorno agora diz ao TypeScript que esperamos um objeto de paginação
    return this.http.get<CustomPaginatedResponse<FuncionarioResponseDTO>>(this.API_BASE_URL);
  }

  /**
   * Gets single employee by ID
   * @param id Employee ID
   * @returns Observable with employee data
   */
  getFuncionarioById(id: number): Observable<FuncionarioResponseDTO> {
    this.loadingSubject.next(true);

    return this.http.get<FuncionarioResponseDTO>(`${this.API_BASE_URL}/${id}`)
      .pipe(
        tap(() => this.loadingSubject.next(false)),
        tap({
          error: () => this.loadingSubject.next(false)
        })
      );
  }

  /**
   * Updates an existing employee
   * @param id Employee ID
   * @param funcionario Updated employee data
   * @returns Observable with updated employee data
   */
  updateFuncionario(id: number, funcionario: FuncionarioUpdateDTO): Observable<FuncionarioResponseDTO> {
    this.loadingSubject.next(true);

    return this.http.put<FuncionarioResponseDTO>(`${this.API_BASE_URL}/${id}`, funcionario)
      .pipe(
        tap(() => this.loadingSubject.next(false)),
        tap({
          error: () => this.loadingSubject.next(false)
        })
      );
  }

  /**
   * Deactivates an employee (soft delete)
   * @param id Employee ID
   * @returns Observable with void response
   */
  deactivateFuncionario(id: number): Observable<void> {
    this.loadingSubject.next(true);

    return this.http.delete<void>(`${this.API_BASE_URL}/${id}`)
      .pipe(
        tap(() => this.loadingSubject.next(false)),
        tap({
          error: () => this.loadingSubject.next(false)
        })
      );
  }

  /**
   * Toggles employee active/inactive status
   * @param id Employee ID
   * @returns Observable with updated employee data
   */
  toggleFuncionarioStatus(id: number): Observable<FuncionarioResponseDTO> {
    this.loadingSubject.next(true);

    return this.http.patch<FuncionarioResponseDTO>(`${this.API_BASE_URL}/${id}/toggle-status`, {})
      .pipe(
        tap(() => this.loadingSubject.next(false)),
        tap({
          error: () => this.loadingSubject.next(false)
        })
      );
  }

  /**
   * Gets list of active doctors for consultation form dropdown
   * @returns Observable with array of active doctors
   */
  getMedicosAtivos(): Observable<FuncionarioResponseDTO[]> {
    this.loadingSubject.next(true);

    return this.http.get<{medicos: FuncionarioResponseDTO[], total: number}>(`${this.API_BASE_URL}/medicos`)
      .pipe(
        map(response => response.medicos),
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
      return error.error?.message || 'Email ou CPF já cadastrado';
    }

    if (error.status === 400) {
      return error.error?.message || 'Dados inválidos. Verifique os campos e tente novamente';
    }

    if (error.status === 404) {
      return 'Funcionário não encontrado';
    }

    if (error.status === 500) {
      return 'Sistema temporariamente indisponível. Tente novamente em alguns minutos';
    }

    if (error.status === 0) {
      return 'Falha na conexão. Verifique sua internet e tente novamente';
    }

    return error.error?.message || 'Erro inesperado. Tente novamente';
  }
}
