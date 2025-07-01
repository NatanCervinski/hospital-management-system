import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PacienteDetalhes, Agendamento, ConsultaDisponivel } from '../interfaces/paciente.interfaces';

@Injectable({
  providedIn: 'root'
})
export class PacienteService {
  private readonly API_BASE_URL = 'http://localhost:3000/api';

  constructor(private http: HttpClient) { }

  /**
   * Gets patient details including points balance
   * @param pacienteId Patient ID
   * @returns Observable with patient details
   */
  getPacienteDetalhes(pacienteId: string): Observable<PacienteDetalhes> {
    return this.http.get<PacienteDetalhes>(`${this.API_BASE_URL}/pacientes/${pacienteId}`);
  }

  /**
   * Gets patient's appointments
   * @param pacienteId Patient ID
   * @returns Observable with array of appointments
   */
  getAgendamentos(pacienteId: string): Observable<Agendamento[]> {
    return this.http.get<Agendamento[]>(`${this.API_BASE_URL}/pacientes/${pacienteId}/agendamentos`);
  }

  /**
   * Gets available consultations (can be filtered)
   * @param especialidade Optional specialty filter
   * @returns Observable with array of available consultations
   */
  getConsultasDisponiveis(especialidade?: string): Observable<ConsultaDisponivel[]> {
    const url = especialidade
      ? `${this.API_BASE_URL}/consultas?especialidade=${especialidade}`
      : `${this.API_BASE_URL}/consultas`;
    return this.http.get<ConsultaDisponivel[]>(url);
  }

  /**
   * Schedules a new appointment
   * @param agendamentoData Appointment data
   * @returns Observable with scheduling response
   */
  agendarConsulta(agendamentoData: any): Observable<any> {
    return this.http.post(`${this.API_BASE_URL}/agendamentos`, agendamentoData);
  }

  /**
   * Buys points for a patient
   * @param pacienteId Patient ID
   * @param valorReais Total cost in BRL (calculated as quantity * 5)
   * @returns Observable with purchase response
   */
  comprarPontos(pacienteId: string, valorReais: number): Observable<any> {
    console.log(`Buying points for patient ${pacienteId} with value R$ ${valorReais}`);
    return this.http.post(`${this.API_BASE_URL}/pacientes/${pacienteId}/comprar-pontos`, { valorReais });
  }

  /**
   * Cancels an appointment
   * @param agendamentoId Appointment ID
   * @returns Observable with cancellation response
   */
  cancelarAgendamento(agendamentoId: string): Observable<any> {
    return this.http.post(`${this.API_BASE_URL}/agendamentos/${agendamentoId}/cancelar`, {});
  }

  /**
   * Performs check-in for an appointment
   * @param agendamentoId Appointment ID
   * @returns Observable with check-in response
   */
  fazerCheckin(agendamentoId: string): Observable<any> {
    return this.http.post(`${this.API_BASE_URL}/agendamentos/${agendamentoId}/checkin`, {});
  }

  getSaldoEHistorico(pacienteId: string): Observable<any> {
    return this.http.get(`${this.API_BASE_URL}/pacientes/${pacienteId}/saldo-e-historico`);
  }
}
