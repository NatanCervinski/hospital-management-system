// agendar-consulta-modal.component.ts
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ConsultaService, ConsultaResponseDTO, EspecialidadeDTO, MedicoDTO } from '../../../services/consulta.service';
import { AuthService } from '../../../services/auth.service';
import { PacienteService } from '../../../services/paciente.service';

@Component({
  selector: 'app-agendar-consulta-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './agendar-consulta-modal.component.html',
  styleUrl: './agendar-consulta-modal.component.scss'
})
export class AgendarConsultaModalComponent implements OnInit {
  @Output() agendamentoSuccess = new EventEmitter<void>();

  agendarForm: FormGroup;
  especialidades: EspecialidadeDTO[] = [];
  medicos: MedicoDTO[] = [];
  consultasDisponiveis: ConsultaResponseDTO[] = [];
  consultaSelecionada: ConsultaResponseDTO | null = null;

  isLoading = false;
  errorMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private consultaService: ConsultaService,
    private pacienteService: PacienteService,
    private authService: AuthService
  ) {
    this.agendarForm = this.fb.group({
      especialidade: ['', Validators.required],
      medico: [''],
      consultaId: ['', Validators.required],
      pontosUsados: [0, [Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.consultaService.getEspecialidades().subscribe(data => this.especialidades = data);
  }

  buscarConsultas(): void {
    const { especialidade } = this.agendarForm.value;
    if (!especialidade) return;

    this.consultaService.buscarPorEspecialidade(especialidade).subscribe(data => {
      this.consultasDisponiveis = data;
      this.agendarForm.patchValue({ consultaId: '' });
    });
  }

  onSubmit(): void {
    if (!this.agendarForm.valid) return;

    const user = this.authService.getCurrentUser();
    if (!user) {
      this.errorMessage = 'Usuário não autenticado';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    const { consultaId, pontosUsados } = this.agendarForm.value;




    const agendamentoData = {
      consultaId,
      pontosUsados: Number(pontosUsados)
    };

    this.pacienteService.agendarConsulta(agendamentoData).subscribe({
      next: () => {
        this.isLoading = false;
        this.agendamentoSuccess.emit();
        this.closeModal();
        alert('Consulta agendada com sucesso!');
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err?.error?.message || 'Erro ao agendar consulta';
      }
    });
  }

  closeModal(): void {
    const modalElement = document.getElementById('agendarConsultaModal');
    if (modalElement) {
      const modal = (window as any).bootstrap?.Modal?.getInstance(modalElement);
      modal?.hide();
    }
  }

  formatDateTime(dateTime: string): string {
    return this.consultaService.formatDateTime(dateTime);
  }

  formatCurrency(value: number): string {
    return this.consultaService.formatCurrency(value);
  }
}
