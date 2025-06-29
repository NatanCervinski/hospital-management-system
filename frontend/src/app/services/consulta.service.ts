import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface Consulta {
  codigo: string;
  dataHora: string;
  especialidade: string;
  medico: string;
  valor: number;
  vagas: number;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class ConsultaService {
  private readonly baseUrl = '/api/consultas';

  constructor(private http: HttpClient) {}

  listarConsultas48h(): Observable<Consulta[]> {
    return this.http.get<Consulta[]>(this.baseUrl + '/proximas-48h');
  }
}
