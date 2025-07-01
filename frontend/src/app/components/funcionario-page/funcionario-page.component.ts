import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FuncionarioService } from '../../services/funcionario.service';
import { FuncionarioResponseDTO } from '../../interfaces/funcionario.interfaces';
import { FuncionarioModalComponent } from '../funcionario-modal/funcionario-modal.component';

@Component({
  selector: 'app-funcionario-page',
  standalone: true,
  imports: [CommonModule, FuncionarioModalComponent],
  templateUrl: './funcionario-page.component.html',
  styleUrls: ['./funcionario-page.component.scss']
})
export class FuncionarioPageComponent implements OnInit {
  @ViewChild('funcionarioModal') funcionarioModal!: FuncionarioModalComponent;

  funcionarios: FuncionarioResponseDTO[] = [];
  loading = false;
  error: string | null = null;

  // Medical specialties mapping
  private especialidades = {
    'CARD': 'Cardiologia',
    'DERM': 'Dermatologia', 
    'ENDO': 'Endocrinologia',
    'GAST': 'Gastroenterologia',
    'GINE': 'Ginecologia',
    'NEUR': 'Neurologia',
    'OFTA': 'Oftalmologia',
    'ORTO': 'Ortopedia',
    'OTOR': 'Otorrinolaringologia',
    'PED': 'Pediatria',
    'PNEU': 'Pneumologia',
    'PSIQ': 'Psiquiatria',
    'URO': 'Urologia'
  };

  constructor(private funcionarioService: FuncionarioService) { }

  ngOnInit() {
    this.loadFuncionarios();
  }

  /**
   * Loads the list of employees from the API
   */
  loadFuncionarios() {
    this.loading = true;
    this.error = null;

    this.funcionarioService.getFuncionarios().subscribe({
      next: (respostaApi) => {
        console.log('Funcionarios carregados:', respostaApi);
        this.funcionarios = respostaApi.funcionarios;

        this.loading = false;
      },
      error: (error) => {
        // ...
      }
    });
  }

  /**
   * Opens the modal to create a new employee
   */
  openCreateModal() {
    this.funcionarioModal.openModal();
  }

  /**
   * Opens the modal to edit an existing employee
   */
  openEditModal(funcionario: FuncionarioResponseDTO) {
    this.funcionarioModal.openModal(funcionario);
  }

  /**
   * Deactivates an employee after confirmation
   */
  deactivateFuncionario(funcionario: FuncionarioResponseDTO) {
    const confirmed = window.confirm(
      `Tem certeza que deseja inativar o funcionário "${funcionario.nome}"?`
    );

    if (confirmed) {
      this.funcionarioService.deactivateFuncionario(funcionario.id).subscribe({
        next: () => {
          // Refresh the list after successful deactivation
          this.loadFuncionarios();
        },
        error: (error) => {
          this.error = this.funcionarioService.getErrorMessage(error);
          console.error('Error deactivating funcionario:', error);
        }
      });
    }
  }

  /**
   * Handles successful creation or update from the modal
   */
  onFuncionarioSaved() {
    this.loadFuncionarios();
  }

  /**
   * Gets the appropriate CSS class for status badge
   */
  getStatusClass(status: boolean): string {
    return status ? 'status-active' : 'status-inactive';
  }

  /**
   * Gets user-friendly status text
   */
  getStatusText(status: boolean): string {
    return status ? 'Ativo' : 'Inativo';
  }

  /**
   * Gets the specialty name from code
   */
  getEspecialidadeNome(codigo: string): string {
    return this.especialidades[codigo as keyof typeof this.especialidades] || codigo;
  }
}
