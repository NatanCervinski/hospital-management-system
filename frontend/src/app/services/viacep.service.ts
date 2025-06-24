import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, BehaviorSubject } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { ViaCepResponse } from '../interfaces/patient.interface';

@Injectable({
  providedIn: 'root'
})
export class ViacepService {
  private readonly VIACEP_BASE_URL = 'https://viacep.com.br/ws';
  private readonly cache = new Map<string, ViaCepResponse>();
  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient) { }

  /**
   * Looks up address information by CEP
   * @param cep CEP string with or without formatting
   * @returns Observable with address information or error
   */
  getAddressByCep(cep: string): Observable<ViaCepResponse | null> {
    const cleanCep = this.cleanCep(cep);
    
    if (!this.isValidCep(cleanCep)) {
      return of(null);
    }

    // Check cache first
    if (this.cache.has(cleanCep)) {
      return of(this.cache.get(cleanCep)!);
    }

    this.loadingSubject.next(true);

    return this.http.get<ViaCepResponse>(`${this.VIACEP_BASE_URL}/${cleanCep}/json/`)
      .pipe(
        map(response => {
          // ViaCEP returns { erro: true } for invalid CEPs
          if (response.erro) {
            return null;
          }
          return response;
        }),
        tap(response => {
          // Cache successful responses
          if (response) {
            this.cache.set(cleanCep, response);
          }
          this.loadingSubject.next(false);
        }),
        catchError(error => {
          console.error('ViaCEP API error:', error);
          this.loadingSubject.next(false);
          return of(null);
        })
      );
  }

  /**
   * Formats CEP string with mask 00000-000
   * @param cep CEP string
   * @returns formatted CEP string
   */
  formatCep(cep: string): string {
    if (!cep) return '';
    
    const cleanCep = cep.replace(/[^\d]/g, '');
    
    if (cleanCep.length <= 5) return cleanCep;
    
    return `${cleanCep.slice(0, 5)}-${cleanCep.slice(5, 8)}`;
  }

  /**
   * Removes CEP formatting
   * @param cep formatted CEP string
   * @returns clean CEP string with only digits
   */
  cleanCep(cep: string): string {
    return cep ? cep.replace(/[^\d]/g, '') : '';
  }

  /**
   * Validates CEP format (8 digits)
   * @param cep CEP string
   * @returns boolean indicating if CEP is valid
   */
  isValidCep(cep: string): boolean {
    const cleanCep = this.cleanCep(cep);
    return /^\d{8}$/.test(cleanCep);
  }

  /**
   * Checks if CEP is complete (8 digits)
   * @param cep CEP string
   * @returns boolean indicating if CEP is complete
   */
  isCepComplete(cep: string): boolean {
    const cleanCep = this.cleanCep(cep);
    return cleanCep.length === 8;
  }

  /**
   * Formats phone number with mask (00) 00000-0000
   * @param telefone phone string
   * @returns formatted phone string
   */
  formatTelefone(telefone: string): string {
    if (!telefone) return '';
    
    const cleanTelefone = telefone.replace(/[^\d]/g, '');
    
    if (cleanTelefone.length <= 2) return `(${cleanTelefone}`;
    if (cleanTelefone.length <= 7) return `(${cleanTelefone.slice(0, 2)}) ${cleanTelefone.slice(2)}`;
    
    return `(${cleanTelefone.slice(0, 2)}) ${cleanTelefone.slice(2, 7)}-${cleanTelefone.slice(7, 11)}`;
  }

  /**
   * Formats date with mask dd/mm/yyyy
   * @param date date string
   * @returns formatted date string
   */
  formatDate(date: string): string {
    if (!date) return '';
    
    const cleanDate = date.replace(/[^\d]/g, '');
    
    if (cleanDate.length <= 2) return cleanDate;
    if (cleanDate.length <= 4) return `${cleanDate.slice(0, 2)}/${cleanDate.slice(2)}`;
    
    return `${cleanDate.slice(0, 2)}/${cleanDate.slice(2, 4)}/${cleanDate.slice(4, 8)}`;
  }

  /**
   * Converts date from dd/mm/yyyy to yyyy-mm-dd format for API
   * @param dateString date in dd/mm/yyyy format
   * @returns date in yyyy-mm-dd format
   */
  convertDateToApiFormat(dateString: string): string {
    if (!dateString || dateString.length !== 10) return '';
    
    const [day, month, year] = dateString.split('/');
    return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
  }

  /**
   * Validates Brazilian date format and checks if it's a valid date
   * @param dateString date in dd/mm/yyyy format
   * @returns boolean indicating if date is valid
   */
  isValidDate(dateString: string): boolean {
    if (!dateString || dateString.length !== 10) return false;
    
    const [day, month, year] = dateString.split('/').map(Number);
    
    if (!day || !month || !year) return false;
    if (day < 1 || day > 31) return false;
    if (month < 1 || month > 12) return false;
    if (year < 1900 || year > new Date().getFullYear()) return false;
    
    // Create date object and verify it's valid
    const date = new Date(year, month - 1, day);
    return date.getFullYear() === year && 
           date.getMonth() === month - 1 && 
           date.getDate() === day;
  }

  /**
   * Clears the ViaCEP cache
   */
  clearCache(): void {
    this.cache.clear();
  }
}