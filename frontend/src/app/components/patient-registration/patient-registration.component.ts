import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, switchMap, takeUntil, tap } from 'rxjs';

import { PatientRegistrationService } from '../../services/patient-registration.service';
import { ViacepService } from '../../services/viacep.service';
import { CpfValidatorService } from '../../services/cpf-validator.service';
import { 
  PatientRegistrationRequest, 
  FormValidationState, 
  ViaCepResponse 
} from '../../interfaces/patient.interface';

@Component({
  selector: 'app-patient-registration',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './patient-registration.component.html',
  styleUrls: ['./patient-registration.component.scss']
})
export class PatientRegistrationComponent implements OnInit, OnDestroy {
  registrationForm!: FormGroup;
  validationState: FormValidationState = {};
  loading = false;
  cepLoading = false;
  errorMessage = '';
  successMessage = '';
  redirectCountdown = 0;

  private destroy$ = new Subject<void>();
  private emailCheck$ = new Subject<string>();
  private cpfCheck$ = new Subject<string>();
  private cepLookup$ = new Subject<string>();

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private patientService: PatientRegistrationService,
    private viacepService: ViacepService,
    private cpfValidator: CpfValidatorService
  ) {
    this.initializeForm();
    this.setupAsyncValidation();
  }

  ngOnInit(): void {
    this.setupFormMasks();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeForm(): void {
    this.registrationForm = this.fb.group({
      // Personal Information
      nome: ['', [Validators.required, Validators.minLength(2)]],
      cpf: ['', [Validators.required, this.cpfValidatorFn.bind(this)]],
      email: ['', [Validators.required, Validators.email]],
      dataNascimento: ['', [Validators.required, this.dateValidator.bind(this)]],
      telefone: ['', [Validators.required, Validators.minLength(14)]],
      
      // Address Information
      cep: ['', [Validators.required, Validators.minLength(9)]],
      logradouro: ['', Validators.required],
      numero: ['', Validators.required],
      complemento: [''],
      bairro: ['', Validators.required],
      cidade: ['', Validators.required],
      estado: ['', Validators.required]
    });

    // Initialize validation state
    Object.keys(this.registrationForm.controls).forEach(key => {
      this.validationState[key] = { valid: false, pending: false };
    });
  }

  private setupFormMasks(): void {
    // CPF Mask
    this.registrationForm.get('cpf')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => {
        if (value !== null) {
          const formatted = this.cpfValidator.formatCpf(value);
          if (formatted !== value) {
            this.registrationForm.get('cpf')?.setValue(formatted, { emitEvent: false });
          }
          
          // Trigger async validation when CPF is complete
          if (this.cpfValidator.isCpfComplete(value)) {
            this.cpfCheck$.next(value);
          }
        }
      });

    // CEP Mask and ViaCEP lookup
    this.registrationForm.get('cep')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => {
        if (value !== null) {
          const formatted = this.viacepService.formatCep(value);
          if (formatted !== value) {
            this.registrationForm.get('cep')?.setValue(formatted, { emitEvent: false });
          }
          
          // Trigger ViaCEP lookup when CEP is complete
          if (this.viacepService.isCepComplete(value)) {
            this.cepLookup$.next(value);
          }
        }
      });

    // Phone Mask
    this.registrationForm.get('telefone')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => {
        if (value !== null) {
          const formatted = this.viacepService.formatTelefone(value);
          if (formatted !== value) {
            this.registrationForm.get('telefone')?.setValue(formatted, { emitEvent: false });
          }
        }
      });

    // Date Mask
    this.registrationForm.get('dataNascimento')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => {
        if (value !== null) {
          const formatted = this.viacepService.formatDate(value);
          if (formatted !== value) {
            this.registrationForm.get('dataNascimento')?.setValue(formatted, { emitEvent: false });
          }
        }
      });

    // Email async validation
    this.registrationForm.get('email')?.valueChanges
      .pipe(
        debounceTime(500),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(value => {
        if (value && this.registrationForm.get('email')?.valid) {
          this.emailCheck$.next(value);
        }
      });
  }

  private setupAsyncValidation(): void {
    // Email availability check
    this.emailCheck$
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => {
          this.validationState['email'] = { valid: false, pending: true };
        }),
        switchMap(email => this.patientService.checkEmailAvailability(email)),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (response) => {
          this.validationState['email'] = {
            valid: !response.exists,
            pending: false,
            error: response.exists ? 'Email já cadastrado' : undefined
          };
        },
        error: () => {
          this.validationState['email'] = { valid: false, pending: false, error: 'Erro ao verificar email' };
        }
      });

    // CPF availability check
    this.cpfCheck$
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => {
          this.validationState['cpf'] = { valid: false, pending: true };
        }),
        switchMap(cpf => this.patientService.checkCpfAvailability(cpf)),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (response) => {
          const isValidCpf = this.cpfValidator.validateCpf(this.registrationForm.get('cpf')?.value);
          this.validationState['cpf'] = {
            valid: !response.exists && isValidCpf,
            pending: false,
            error: response.exists ? 'CPF já cadastrado' : !isValidCpf ? 'CPF inválido' : undefined
          };
        },
        error: () => {
          this.validationState['cpf'] = { valid: false, pending: false, error: 'Erro ao verificar CPF' };
        }
      });

    // ViaCEP address lookup
    this.cepLookup$
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => {
          this.cepLoading = true;
        }),
        switchMap(cep => this.viacepService.getAddressByCep(cep)),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (response: ViaCepResponse | null) => {
          this.cepLoading = false;
          if (response) {
            this.fillAddressFields(response);
            this.validationState['cep'] = { valid: true, pending: false };
          } else {
            this.validationState['cep'] = { valid: false, pending: false, error: 'CEP não encontrado' };
          }
        },
        error: () => {
          this.cepLoading = false;
          this.validationState['cep'] = { valid: false, pending: false, error: 'Erro ao buscar CEP' };
        }
      });
  }

  private fillAddressFields(address: ViaCepResponse): void {
    this.registrationForm.patchValue({
      logradouro: address.logradouro,
      bairro: address.bairro,
      cidade: address.localidade,
      estado: address.uf
    });
  }

  private cpfValidatorFn(control: AbstractControl): { [key: string]: any } | null {
    if (!control.value) return null;
    
    const isValid = this.cpfValidator.validateCpf(control.value);
    return isValid ? null : { invalidCpf: true };
  }

  private dateValidator(control: AbstractControl): { [key: string]: any } | null {
    if (!control.value) return null;
    
    const isValid = this.viacepService.isValidDate(control.value);
    return isValid ? null : { invalidDate: true };
  }

  getFieldError(fieldName: string): string | null {
    const control = this.registrationForm.get(fieldName);
    const validationState = this.validationState[fieldName];

    if (validationState?.error) {
      return validationState.error;
    }

    if (control?.errors && control.touched) {
      if (control.errors['required']) return `${this.getFieldLabel(fieldName)} é obrigatório`;
      if (control.errors['email']) return 'Email deve ter formato válido';
      if (control.errors['invalidCpf']) return 'CPF inválido';
      if (control.errors['invalidDate']) return 'Data inválida';
      if (control.errors['minlength']) return `${this.getFieldLabel(fieldName)} muito curto`;
    }

    return null;
  }

  private getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      nome: 'Nome',
      cpf: 'CPF',
      email: 'Email',
      dataNascimento: 'Data de nascimento',
      telefone: 'Telefone',
      cep: 'CEP',
      logradouro: 'Logradouro',
      numero: 'Número',
      complemento: 'Complemento',
      bairro: 'Bairro',
      cidade: 'Cidade',
      estado: 'Estado'
    };
    return labels[fieldName] || fieldName;
  }

  isFieldValid(fieldName: string): boolean {
    const control = this.registrationForm.get(fieldName);
    const validationState = this.validationState[fieldName];
    
    return !!(control?.valid && !validationState?.error && !validationState?.pending);
  }

  isFieldInvalid(fieldName: string): boolean {
    const control = this.registrationForm.get(fieldName);
    const validationState = this.validationState[fieldName];
    
    return !!(control?.invalid && control?.touched) || !!validationState?.error;
  }

  isFieldPending(fieldName: string): boolean {
    return !!this.validationState[fieldName]?.pending;
  }

  async onSubmit(): Promise<void> {
    if (this.registrationForm.invalid || this.loading) {
      this.markAllFieldsAsTouched();
      return;
    }

    // Check for async validation errors
    const hasAsyncErrors = Object.values(this.validationState).some(state => state.error || state.pending);
    if (hasAsyncErrors) {
      this.errorMessage = 'Corrija os erros no formulário antes de continuar';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    try {
      const formData = this.registrationForm.value;
      const patientData: PatientRegistrationRequest = this.patientService.formatPatientDataForApi(formData);
      
      const response = await this.patientService.registerPatient(patientData).toPromise();
      
      this.successMessage = 'Cadastro realizado com sucesso! Verifique seu email para receber suas credenciais de acesso. Você será redirecionado para o login...';
      this.startRedirectCountdown();
      
    } catch (error: any) {
      this.errorMessage = this.patientService.getErrorMessage(error);
    } finally {
      this.loading = false;
    }
  }

  private markAllFieldsAsTouched(): void {
    Object.keys(this.registrationForm.controls).forEach(key => {
      this.registrationForm.get(key)?.markAsTouched();
    });
  }

  private startRedirectCountdown(): void {
    this.redirectCountdown = 5;
    const interval = setInterval(() => {
      this.redirectCountdown--;
      if (this.redirectCountdown <= 0) {
        clearInterval(interval);
        this.router.navigate(['/login']);
      }
    }, 1000);
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}