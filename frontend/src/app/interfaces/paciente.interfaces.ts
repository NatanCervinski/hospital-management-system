export interface PacienteDetalhes {
  id: string;
  nome: string;
  email: string;
  saldoPontos: number;
}

export interface Agendamento {
  id: number;
  codigoAgendamento: string;
  pacienteId: number;
  dataAgendamento: string;
  pontosUsados: number;
  valorPago: number;
  valorTotal: number;
  descontoPontos: number;
  status: string;
  observacoes?: string;
  dataCheckin?: string;
  dataConfirmacao?: string;
  consulta?: {
    id: number;
    codigo: string;
    dataHora: string;
    especialidade: string;
    medico: string;
    valor: number;
    vagas: number;
    vagasOcupadas: number;
    status: string;
  };
}


export interface ConsultaDisponivel {
  id: string;
  especialidade: string;
  medico: string;
  dataHora: string;
  valor: number;
  vagas: number;
}
