import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ConsultaService, ConsultaDTO } from '../../../services/consulta.service';

@Component({
  selector: 'app-consulta-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './consulta-form.component.html',
  styleUrls: ['./consulta-form.component.scss']
})
export class ConsultaFormComponent implements OnInit {
  @Input() show = false;
  @Output() showChange = new EventEmitter<boolean>();
  @Output() consultaCreated = new EventEmitter<void>();

  consultaForm: FormGroup;
  especialidades: string[] = [];
  medicos: string[] = [];
  isSubmitting = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private consultaService: ConsultaService
  ) {
    this.consultaForm = this.createForm();
  }

  ngOnInit(): void {
    // --- Para Especialidades ---
    this.consultaService.getEspecialidades().subscribe({
      next: (dados) => { // 'dados' aqui é um array de objetos EspecialidadeDTO
        console.log('Objetos de especialidade recebidos:', dados);

        // A CORREÇÃO: Use .map() para extrair apenas a propriedade 'nome' de cada objeto.
        this.especialidades = dados.map(especialidade => especialidade.nome);

        console.log('Nomes das especialidades (strings) para o dropdown:', this.especialidades);
      },
      error: (err) => {
        console.error('Erro ao buscar especialidades:', err);
        // Opcional: Esvaziar o array em caso de erro
        this.especialidades = [];
      }
    });

    // --- Para Médicos (aplicando a mesma lógica) ---
    this.consultaService.getMedicos().subscribe({
      next: (dados) => { // 'dados' aqui é um array de objetos MedicoDTO
        console.log('Objetos de médico recebidos:', dados);

        // A CORREÇÃO: Use .map() para extrair apenas a propriedade 'nome' de cada objeto.
        this.medicos = dados.map(medico => medico.nome);

        console.log('Nomes dos médicos (strings) para o dropdown:', this.medicos);
      },
      error: (err) => {
        console.error('Erro ao buscar médicos:', err);
        this.medicos = [];
      }
    });
  }

  private createForm(): FormGroup {
    return this.fb.group({
      dataHora: ['', [Validators.required]],
      especialidade: ['', [Validators.required]],
      medico: ['', [Validators.required]],
      valor: ['', [Validators.required, Validators.min(0.01)]],
      vagas: ['', [Validators.required, Validators.min(1), Validators.max(50)]]
    });
  }

  /**
   * Get current date and time in ISO format for datetime-local input
   */
  getMinDateTime(): string {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    return now.toISOString().slice(0, 16);
  }

  /**
   * Handle form submission
   */
  onSubmit(): void {
    if (this.consultaForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.isSubmitting = true;
    this.error = null;

    const formValue = this.consultaForm.value;
    const consultaDto: ConsultaDTO = {
      dataHora: new Date(formValue.dataHora).toISOString(),
      especialidade: formValue.especialidade,
      medico: formValue.medico,
      valor: parseFloat(formValue.valor),
      vagas: parseInt(formValue.vagas, 10)
    };

    this.consultaService.criarConsulta(consultaDto).subscribe({
      next: () => {
        this.consultaCreated.emit();
        this.closeModal();
        this.resetForm();
      },
      error: (error) => {
        this.error = error.message || 'Erro ao criar consulta. Tente novamente.';
        this.isSubmitting = false;
      },
      complete: () => {
        this.isSubmitting = false;
      }
    });
  }

  /**
   * Close modal
   */
  closeModal(): void {
    this.show = false;
    this.showChange.emit(false);
    this.resetForm();
  }

  /**
   * Reset form to initial state
   */
  private resetForm(): void {
    this.consultaForm.reset();
    this.error = null;
    this.isSubmitting = false;
  }

  /**
   * Mark all form fields as touched to show validation errors
   */
  private markFormGroupTouched(): void {
    Object.keys(this.consultaForm.controls).forEach(key => {
      const control = this.consultaForm.get(key);
      control?.markAsTouched();
    });
  }

  /**
   * Check if field has error and is touched
   */
  hasError(fieldName: string, errorType?: string): boolean {
    const field = this.consultaForm.get(fieldName);
    if (!field) return false;

    if (errorType) {
      return field.hasError(errorType) && field.touched;
    }
    return field.invalid && field.touched;
  }

  /**
   * Get error message for field
   */
  getErrorMessage(fieldName: string): string {
    const field = this.consultaForm.get(fieldName);
    if (!field || !field.errors || !field.touched) return '';

    const errors = field.errors;

    if (errors['required']) {
      switch (fieldName) {
        case 'dataHora': return 'Data e hora são obrigatórias';
        case 'especialidade': return 'Especialidade é obrigatória';
        case 'medico': return 'Médico é obrigatório';
        case 'valor': return 'Valor é obrigatório';
        case 'vagas': return 'Número de vagas é obrigatório';
        default: return 'Campo obrigatório';
      }
    }

    if (errors['min']) {
      if (fieldName === 'valor') return 'Valor deve ser maior que zero';
      if (fieldName === 'vagas') return 'Deve ter pelo menos 1 vaga';
    }

    if (errors['max']) {
      if (fieldName === 'vagas') return 'Máximo de 50 vagas permitidas';
    }

    return 'Campo inválido';
  }
}
