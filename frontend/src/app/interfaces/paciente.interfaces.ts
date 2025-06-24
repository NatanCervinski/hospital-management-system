export interface PacienteDetalhes {
  id: string;
  nome: string;
  email: string;
  pontos: number;
}

export interface Agendamento {
  codigo: string;
  especialidade: string;
  nomeMedico: string;
  dataHora: string; // ISO 8601 format
  status: 'CRIADO' | 'CHECK-IN' | 'REALIZADO' | 'CANCELADO' | 'FALTOU' | 'COMPARECEU';
}

export interface ConsultaDisponivel {
  id: string;
  especialidade: string;
  medico: string;
  dataHora: string;
  valor: number;
  vagas: number;
}