import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService, User } from '../../../services/auth.service';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-paciente-dashboard',
  imports: [CommonModule],
  templateUrl: './paciente-dashboard.component.html',
  styleUrl: './paciente-dashboard.component.scss'
})
export class PacienteDashboardComponent implements OnInit, OnDestroy {
  user: User | null = null;
  isLoading = true;
  isAuthenticated = false;
  private subscription = new Subscription();

  constructor(
    private authService: AuthService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.subscription.add(
      this.authService.currentUser$.subscribe({
        next: (user) => {
          this.user = user;
          this.isAuthenticated = !!user;
          this.isLoading = false;
        }
      })
    );

    this.subscription.add(
      this.authService.verifyToken().subscribe({
        next: (isValid) => {
          if (!isValid) {
            this.router.navigate(['/login']);
          }
          this.isLoading = false;
        },
        error: () => {
          this.isLoading = false;
          this.router.navigate(['/login']);
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  get userName(): string {
    return this.user?.nome || this.user?.email || 'Paciente';
  }

  get userEmail(): string {
    return this.user?.email || '';
  }
}
