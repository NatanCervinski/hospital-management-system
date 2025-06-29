import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-funcionario-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './funcionario-dashboard.component.html',
  styleUrls: ['./funcionario-dashboard.component.scss']
})
export class FuncionarioDashboardComponent {
  isLoading = false;
  isAuthenticated = true;

  userName = 'Funcionário';
  userEmail = 'funcionario@teste.com';
  user = { tipo: 'FUNCIONÁRIO' };

  constructor(public router: Router) {}

  logout() {
    alert('Logout simulado');
  }
}
