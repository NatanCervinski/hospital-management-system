import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import {
  PatientRegistrationRequest,
  PatientRegistrationResponse,
  EmailCheckResponse,
  CpfCheckResponse,
  PatientProfileRequest
} from '../interfaces/patient.interface';
import { UserRegistrationRequest, UserRegistrationResponse } from '../interfaces/user.interface';

@Injectable({
  providedIn: 'root'
})
export class PatientRegistrationService {
  private readonly AUTH_API_URL = 'http://localhost:3000/api/auth';
  private readonly PATIENT_API_URL = 'http://localhost:3000/api/pacientes';
  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient) { }

  /**
   * PASSO 1: Registra o Usuário no ms-autenticacao
   * @param userData Dados para registro do usuário
   * @returns Observable com a resposta do ms-autenticacao
   */
  registerUser(userData: UserRegistrationRequest): Observable<UserRegistrationResponse> {
    this.loadingSubject.next(true);
    return this.http.post<UserRegistrationResponse>(
      `${this.AUTH_API_URL}/register/paciente`, // Endpoint correto
      userData
    ).pipe(catchError(this.handleError));
  }
  createPatientProfile(profileData: PatientProfileRequest): Observable<PatientRegistrationResponse> {
    // Note que a URL base mudou para o endpoint de pacientes
    return this.http.post<PatientRegistrationResponse>(
      `${this.PATIENT_API_URL}/cadastro`, // Endpoint do ms-paciente
      profileData
    ).pipe(
      map(response => {
        this.loadingSubject.next(false); // Finaliza o loading apenas aqui
        return response;
      }),
      catchError(this.handleError)
    );
  }
  /**
   * Registers a new patient
   * @param patientData Patient registration data
   * @returns Observable with registration response
   */
  registerPatient(patientData: PatientRegistrationRequest): Observable<PatientRegistrationResponse> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    this.loadingSubject.next(true);


    return this.http.post<PatientRegistrationResponse>(
      `${this.AUTH_API_URL}/register/paciente`,
      patientData,
      { headers }
    ).pipe(
      map(response => {
        this.loadingSubject.next(false);
        return response;
      }),
      catchError(error => {
        this.loadingSubject.next(false);
        throw error;
      })
    );
  }

  /**
   * Checks if email is already registered
   * @param email Email to check
   * @returns Observable with email check response
   */
  checkEmailAvailability(email: string): Observable<EmailCheckResponse> {
    if (!email || !this.isValidEmail(email)) {
      return of({ exists: false, message: 'Email inválido' });
    }

    const params = new HttpParams().set('email', email);

    return this.http.get<EmailCheckResponse>(`${this.AUTH_API_URL}/check-email`, { params })
      .pipe(
        catchError(error => {
          console.error('Email check error:', error);
          // Return a default response if the API call fails
          return of({ exists: false, message: 'Erro ao verificar email' });
        })
      );
  }

  /**
   * Checks if CPF is already registered
   * @param cpf CPF to check
   * @returns Observable with CPF check response
   */
  checkCpfAvailability(cpf: string): Observable<CpfCheckResponse> {
    if (!cpf) {
      return of({ exists: false, message: 'CPF inválido' });
    }

    // Clean CPF for API call
    const cleanCpf = cpf.replace(/[^\d]/g, '');
    const params = new HttpParams().set('cpf', cleanCpf);

    return this.http.get<CpfCheckResponse>(`${this.AUTH_API_URL}/check-cpf`, { params })
      .pipe(
        catchError(error => {
          console.error('CPF check error:', error);
          // Return a default response if the API call fails
          return of({ exists: false, message: 'Erro ao verificar CPF' });
        })
      );
  }

  /**
   * Validates email format
   * @param email Email string
   * @returns boolean indicating if email is valid
   */
  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  /**
   * Validates required fields for registration
   * @param patientData Patient data to validate
   * @returns object with validation results
   */
  validatePatientData(patientData: PatientRegistrationRequest): { isValid: boolean; errors: string[] } {
    const errors: string[] = [];

    if (!patientData.nome?.trim()) {
      errors.push('Nome é obrigatório');
    }

    if (!patientData.cpf?.trim()) {
      errors.push('CPF é obrigatório');
    }

    if (!patientData.email?.trim()) {
      errors.push('Email é obrigatório');
    } else if (!this.isValidEmail(patientData.email)) {
      errors.push('Email deve ter um formato válido');
    }

    if (!patientData.dataNascimento?.trim()) {
      errors.push('Data de nascimento é obrigatória');
    }

    if (!patientData.telefone?.trim()) {
      errors.push('Telefone é obrigatório');
    }

    if (!patientData.cep?.trim()) {
      errors.push('CEP é obrigatório');
    }

    if (!patientData.logradouro?.trim()) {
      errors.push('Logradouro é obrigatório');
    }

    if (!patientData.numero?.trim()) {
      errors.push('Número é obrigatório');
    }

    if (!patientData.bairro?.trim()) {
      errors.push('Bairro é obrigatório');
    }

    if (!patientData.cidade?.trim()) {
      errors.push('Cidade é obrigatória');
    }

    if (!patientData.estado?.trim()) {
      errors.push('Estado é obrigatório');
    }

    return {
      isValid: errors.length === 0,
      errors
    };
  }

  /**
   * Formats patient data for API submission
   * @param formData Form data object
   * @returns Formatted patient registration request
   */
  formatPatientDataForApi(formData: any): PatientRegistrationRequest {
    return {
      nome: formData.nome?.trim() || '',
      cpf: formData.cpf?.replace(/[^\d]/g, '') || '',
      email: formData.email?.trim().toLowerCase() || '',
      dataNascimento: this.convertDateToApiFormat(formData.dataNascimento) || '',
      telefone: formData.telefone?.replace(/[^\d]/g, '') || '',
      cep: formData.cep?.replace(/[^\d]/g, '') || '',
      logradouro: formData.logradouro?.trim() || '',
      numero: formData.numero?.trim() || '',
      complemento: formData.complemento?.trim() || '',
      bairro: formData.bairro?.trim() || '',
      cidade: formData.cidade?.trim() || '',
      estado: formData.estado?.trim() || ''
    };
  }

  /**
   * Converts date from dd/mm/yyyy to yyyy-mm-dd format for API
   * @param dateString date in dd/mm/yyyy format
   * @returns date in yyyy-mm-dd format
   */
  private convertDateToApiFormat(dateString: string): string {
    if (!dateString || dateString.length !== 10) return '';

    const [day, month, year] = dateString.split('/');
    return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
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

    if (error.status === 500) {
      return 'Sistema temporariamente indisponível. Tente novamente em alguns minutos';
    }

    if (error.status === 0) {
      return 'Falha na conexão. Verifique sua internet e tente novamente';
    }

    return error.error?.message || 'Erro inesperado. Tente novamente';
  }

  private handleError = (error: any) => {
    this.loadingSubject.next(false);
    return throwError(error);
  }
  public formatPatientProfileForApi(formData: any, usuarioId: number): PatientProfileRequest {
    return {
      usuarioId: usuarioId,
      nome: formData.nome,
      cpf: formData.cpf,
      email: formData.email,
      dataNascimento: formData.dataNascimento,
      telefone: formData.telefone,
      cep: formData.cep,
      logradouro: formData.logradouro,
      numero: formData.numero,
      complemento: formData.complemento,
      bairro: formData.bairro,
      cidade: formData.cidade,
      estado: formData.estado,
    };
  }
  public formatUserDataForApi(formData: any): UserRegistrationRequest {
    return {
      nome: formData.nome,
      cpf: formData.cpf,
      email: formData.email,
      cep: formData.cep
    };
  }
}
