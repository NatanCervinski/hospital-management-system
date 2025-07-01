// Importe o CommonModule e FormsModule como você já fez
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-consulta-cadastro',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './consulta-cadastro.component.html',
  styleUrls: ['./consulta-cadastro.component.scss']
})
export class ConsultaCadastroComponent {
  // Propriedade para guardar a data e hora mínima permitida
  minDate: string;

  consulta = {
    dataHora: '',
    especialidade: '',
    medico: '',
    valor: 0,
    vagas: 1
  };

  constructor(private http: HttpClient) {
    // Inicializa a propriedade com a data e hora atuais no formato correto
    const now = new Date();
    // Ajusta para o fuso horário local
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    // Formata para 'YYYY-MM-DDTHH:mm'
    this.minDate = now.toISOString().slice(0, 16);
  }

  salvar() {
    this.http.post('/api/consultas', this.consulta).subscribe({
      next: () => alert('Consulta cadastrada com sucesso!'),
      error: () => alert('Erro ao cadastrar consulta.')
    });
  }
}
