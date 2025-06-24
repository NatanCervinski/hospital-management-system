import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CpfValidatorService {

  constructor() { }

  /**
   * Validates Brazilian CPF using the official algorithm
   * @param cpf CPF string with or without formatting
   * @returns boolean indicating if CPF is valid
   */
  validateCpf(cpf: string): boolean {
    if (!cpf) return false;

    // Remove formatting characters
    const cleanCpf = cpf.replace(/[^\d]/g, '');

    // Check if CPF has 11 digits
    if (cleanCpf.length !== 11) return false;

    // Check if all digits are the same (invalid CPF)
    if (/^(\d)\1{10}$/.test(cleanCpf)) return false;

    // Validate first check digit
    let sum = 0;
    for (let i = 0; i < 9; i++) {
      sum += parseInt(cleanCpf.charAt(i)) * (10 - i);
    }
    let remainder = (sum * 10) % 11;
    if (remainder === 10 || remainder === 11) remainder = 0;
    if (remainder !== parseInt(cleanCpf.charAt(9))) return false;

    // Validate second check digit
    sum = 0;
    for (let i = 0; i < 10; i++) {
      sum += parseInt(cleanCpf.charAt(i)) * (11 - i);
    }
    remainder = (sum * 10) % 11;
    if (remainder === 10 || remainder === 11) remainder = 0;
    if (remainder !== parseInt(cleanCpf.charAt(10))) return false;

    return true;
  }

  /**
   * Formats CPF string with mask 000.000.000-00
   * @param cpf CPF string
   * @returns formatted CPF string
   */
  formatCpf(cpf: string): string {
    if (!cpf) return '';
    
    const cleanCpf = cpf.replace(/[^\d]/g, '');
    
    if (cleanCpf.length <= 3) return cleanCpf;
    if (cleanCpf.length <= 6) return `${cleanCpf.slice(0, 3)}.${cleanCpf.slice(3)}`;
    if (cleanCpf.length <= 9) return `${cleanCpf.slice(0, 3)}.${cleanCpf.slice(3, 6)}.${cleanCpf.slice(6)}`;
    
    return `${cleanCpf.slice(0, 3)}.${cleanCpf.slice(3, 6)}.${cleanCpf.slice(6, 9)}-${cleanCpf.slice(9, 11)}`;
  }

  /**
   * Removes CPF formatting
   * @param cpf formatted CPF string
   * @returns clean CPF string with only digits
   */
  cleanCpf(cpf: string): string {
    return cpf ? cpf.replace(/[^\d]/g, '') : '';
  }

  /**
   * Validates if CPF is complete (11 digits)
   * @param cpf CPF string
   * @returns boolean indicating if CPF is complete
   */
  isCpfComplete(cpf: string): boolean {
    const cleanCpf = this.cleanCpf(cpf);
    return cleanCpf.length === 11;
  }
}