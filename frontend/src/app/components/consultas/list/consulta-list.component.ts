import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ConsultaService, Consulta } from '../../../services/consulta.service';

@Component({
  selector: 'app-consulta-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './consulta-list.component.html',
  styleUrls: ['./consulta-list.component.scss']
})
export class ConsultaListComponent implements OnInit {
  consultas: Consulta[] = [];
  loading = true;

  constructor(private consultaService: ConsultaService) {}

  ngOnInit(): void {
    this.consultaService.listarConsultas48h().subscribe({
      next: (res: Consulta[]) => {
        this.consultas = res;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
