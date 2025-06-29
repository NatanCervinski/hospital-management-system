import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { FuncionarioService } from '../../services/funcionario.service';
import { CpfValidatorService } from '../../services/cpf-validator.service';
import { ViacepService } from '../../services/viacep.service';
import { 
  FuncionarioResponseDTO, 
  FuncionarioCadastroDTO, 
  FuncionarioUpdateDTO 
} from '../../interfaces/funcionario.interfaces';

@Component({
  selector: 'app-funcionario-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './funcionario-modal.component.html',
  styleUrls: ['./funcionario-modal.component.scss']
})
export class FuncionarioModalComponent {
  @Output() funcionarioSaved = new EventEmitter<void>();

  isOpen = false;
  isEditMode = false;
  loading = false;
  error: string | null = null;
  currentFuncionarioId: number | null = null;

  funcionarioForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private funcionarioService: FuncionarioService,
    private cpfValidator: CpfValidatorService,
    private viacepService: ViacepService
  ) {
    this.funcionarioForm = this.createForm();
  }

  /**
   * Creates the reactive form
   */
  private createForm(): FormGroup {
    return this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      cpf: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
      telefone: ['', [Validators.required, Validators.maxLength(15)]]
    });
  }

  /**
   * Opens the modal for creating a new employee
   */
  openModal(funcionario?: FuncionarioResponseDTO) {
    this.isOpen = true;
    this.error = null;
    this.currentFuncionarioId = null;
    
    if (funcionario) {
      // Edit mode
      this.isEditMode = true;
      this.currentFuncionarioId = funcionario.id;
      this.funcionarioForm.patchValue({
        nome: funcionario.nome,
        email: funcionario.email,
        telefone: funcionario.telefone
      });
      // Remove CPF field for editing
      this.funcionarioForm.get('cpf')?.clearValidators();
      this.funcionarioForm.get('cpf')?.updateValueAndValidity();
    } else {
      // Create mode
      this.isEditMode = false;
      this.funcionarioForm.reset();
      // Add CPF validators for creation
      this.funcionarioForm.get('cpf')?.setValidators([Validators.required]);
      this.funcionarioForm.get('cpf')?.updateValueAndValidity();
    }
  }

  /**
   * Closes the modal
   */
  closeModal() {
    this.isOpen = false;
    this.funcionarioForm.reset();
    this.error = null;
    this.loading = false;
  }

  /**
   * Handles form submission
   */
  onSubmit() {
    if (this.funcionarioForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.loading = true;
    this.error = null;

    if (this.isEditMode && this.currentFuncionarioId) {
      this.updateFuncionario();
    } else {
      this.createFuncionario();
    }
  }

  /**
   * Creates a new employee
   */
  private createFuncionario() {
    const formValue = this.funcionarioForm.value;
    
    // Validate CPF
    if (!this.cpfValidator.validateCpf(formValue.cpf)) {
      this.error = 'CPF inválido. Verifique os dígitos informados.';
      this.loading = false;
      return;
    }

    const funcionarioData: FuncionarioCadastroDTO = {
      nome: formValue.nome.trim(),
      cpf: this.cpfValidator.cleanCpf(formValue.cpf),
      email: formValue.email.trim().toLowerCase(),
      telefone: formValue.telefone.replace(/[^\d]/g, '')
    };

    this.funcionarioService.createFuncionario(funcionarioData).subscribe({
      next: () => {
        this.loading = false;
        this.funcionarioSaved.emit();
        this.closeModal();
      },
      error: (error) => {
        this.loading = false;
        this.error = this.funcionarioService.getErrorMessage(error);
        console.error('Error creating funcionario:', error);
      }
    });
  }

  /**
   * Updates an existing employee
   */
  private updateFuncionario() {
    if (!this.currentFuncionarioId) return;

    const formValue = this.funcionarioForm.value;
    const funcionarioData: FuncionarioUpdateDTO = {
      nome: formValue.nome.trim(),
      email: formValue.email.trim().toLowerCase(),
      telefone: formValue.telefone.replace(/[^\d]/g, '')
    };

    this.funcionarioService.updateFuncionario(this.currentFuncionarioId, funcionarioData).subscribe({
      next: () => {
        this.loading = false;
        this.funcionarioSaved.emit();
        this.closeModal();
      },
      error: (error) => {
        this.loading = false;
        this.error = this.funcionarioService.getErrorMessage(error);
        console.error('Error updating funcionario:', error);
      }
    });
  }

  /**
   * Marks all form fields as touched to show validation errors
   */
  private markFormGroupTouched() {
    Object.keys(this.funcionarioForm.controls).forEach(key => {
      const control = this.funcionarioForm.get(key);
      control?.markAsTouched();
    });
  }

  /**
   * Checks if a field has validation errors and was touched
   */
  hasFieldError(fieldName: string): boolean {
    const field = this.funcionarioForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  /**
   * Gets the error message for a specific field
   */
  getFieldErrorMessage(fieldName: string): string {
    const field = this.funcionarioForm.get(fieldName);
    if (!field || !field.errors) return '';

    const errors = field.errors;
    
    if (errors['required']) return `${this.getFieldLabel(fieldName)} é obrigatório`;
    if (errors['email']) return 'Email deve ter um formato válido';
    if (errors['minlength']) return `${this.getFieldLabel(fieldName)} deve ter pelo menos ${errors['minlength'].requiredLength} caracteres`;
    if (errors['maxlength']) return `${this.getFieldLabel(fieldName)} não pode ter mais de ${errors['maxlength'].requiredLength} caracteres`;
    
    return 'Campo inválido';
  }

  /**
   * Gets the label for a field
   */
  private getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      'nome': 'Nome',
      'cpf': 'CPF',
      'email': 'Email',
      'telefone': 'Telefone'
    };
    return labels[fieldName] || fieldName;
  }

  /**
   * Handles CPF input formatting
   */
  onCpfInput(event: any) {
    const value = event.target.value;
    const formattedValue = this.cpfValidator.formatCpf(value);
    this.funcionarioForm.get('cpf')?.setValue(formattedValue, { emitEvent: false });
  }

  /**
   * Handles phone input formatting
   */
  onTelefoneInput(event: any) {
    const value = event.target.value;
    const formattedValue = this.viacepService.formatTelefone(value);
    this.funcionarioForm.get('telefone')?.setValue(formattedValue, { emitEvent: false });
  }
}