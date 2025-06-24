import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { AuthService, LoginRequest } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  sessionMessage = '';
  showPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.initializeForm();

    this.route.queryParams.subscribe(params => {
      if (params['message']) {
        this.sessionMessage = params['message'];
        setTimeout(() => {
          this.sessionMessage = '';
        }, 5000);
      }
    });

    if (this.authService.isAuthenticated()) {
      this.redirectToDashboard();
    }
  }

  private initializeForm(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required, Validators.minLength(3)]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid && !this.isLoading) {
      this.isLoading = true;
      this.errorMessage = '';

      const loginData: LoginRequest = {
        email: this.loginForm.value.email,
        senha: this.loginForm.value.senha
      };

      this.authService.login(loginData).subscribe({
        next: (response) => {
          this.isLoading = false;
          // Use the response type directly instead of relying on getUserType()
          this.redirectToDashboardWithType(response.tipo);
        },
        error: (error) => {
          this.isLoading = false;
          this.handleLoginError(error);
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  private redirectToDashboard(): void {
    const userType = this.authService.getUserType();
    if (userType === 'FUNCIONARIO') {
      this.router.navigate(['/dashboard/funcionario']);
    } else if (userType === 'PACIENTE') {
      this.router.navigate(['/dashboard/paciente']);
    } else {
      this.router.navigate(['/dashboard']);
    }
  }

  private redirectToDashboardWithType(userType: 'PACIENTE' | 'FUNCIONARIO'): void {
    if (userType === 'FUNCIONARIO') {
      this.router.navigate(['/dashboard/funcionario']);
    } else if (userType === 'PACIENTE') {
      this.router.navigate(['/dashboard/paciente']);
    } else {
      this.router.navigate(['/dashboard']);
    }
  }

  private handleLoginError(error: any): void {
    if (error.status === 401) {
      this.errorMessage = 'Email ou senha inválidos. Verifique suas credenciais.';
    } else if (error.status === 0) {
      this.errorMessage = 'Erro de conexão. Verifique sua internet ou tente novamente.';
    } else if (error.error?.message) {
      this.errorMessage = error.error.message;
    } else {
      this.errorMessage = 'Ocorreu um erro inesperado. Tente novamente.';
    }
  }

  private markFormGroupTouched(): void {
    Object.keys(this.loginForm.controls).forEach(key => {
      this.loginForm.get(key)?.markAsTouched();
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  getFieldError(fieldName: string): string {
    const field = this.loginForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `${fieldName === 'email' ? 'Email' : 'Senha'} é obrigatório`;
      }
      if (field.errors['email']) {
        return 'Digite um email válido';
      }
      if (field.errors['minlength']) {
        return 'Senha deve ter pelo menos 3 caracteres';
      }
    }
    return '';
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.loginForm.get(fieldName);
    return !!(field?.invalid && field.touched);
  }
}
