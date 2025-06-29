import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-consulta-cadastro',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './consulta-cadastro.component.html',
  styleUrls: ['./consulta-cadastro.component.scss']
})
export class ConsultaCadastroComponent {
  consulta = {
    dataHora: '',
    especialidade: '',
    medico: '',
    valor: 0,
    vagas: 1
  };

  constructor(private http: HttpClient) {}

  salvar() {
    this.http.post('/api/consultas', this.consulta).subscribe({
      next: () => alert('Consulta cadastrada com sucesso!'),
      error: () => alert('Erro ao cadastrar consulta.')
    });
  }
}
