import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, catchError, of, map } from 'rxjs';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  token: string;
  id: string;
  email: string;
  nome: string;
  tipo: 'PACIENTE' | 'FUNCIONARIO';
}

export interface User {
  id: string;
  email: string;
  nome: string;
  tipo: 'PACIENTE' | 'FUNCIONARIO';
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_BASE_URL = 'http://localhost:3000/api/auth';
  private readonly TOKEN_KEY = 'hospital_token';
  private readonly USER_KEY = 'hospital_user';

  private currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) { }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post<LoginResponse>(`${this.API_BASE_URL}/login`, credentials, { headers })
      .pipe(
        tap(response => {
          console.log('Login successful:', response);
          this.setToken(response.token);
          var user: User = {
            id: response.id,
            email: response.email,
            nome: response.nome,
            tipo: response.tipo
          };
          this.setUser(user);
          this.currentUserSubject.next(user);
        })
      );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  setUser(user: User): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  private getUserFromStorage(): User | null {
    const userStr = localStorage.getItem(this.USER_KEY);
    if (!userStr) return null;

    try {
      return JSON.parse(userStr);
    } catch (error) {
      console.warn('Failed to parse user data from localStorage, clearing corrupted data');
      localStorage.removeItem(this.USER_KEY);
      return null;
    }
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      return payload.exp > currentTime;
    } catch {
      return false;
    }
  }

  verifyToken(): Observable<boolean> {
    const token = this.getToken();
    if (!token) {
      return of(false);
    }

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.get(`${this.API_BASE_URL}/verify`, { headers })
      .pipe(
        map(() => true),
        catchError(() => {
          this.logout();
          return of(false);
        })
      );
  }

  async verifyTokenAsync(): Promise<boolean> {
    return new Promise((resolve) => {
      this.verifyToken().subscribe({
        next: (isValid) => resolve(isValid),
        error: () => resolve(false)
      });
    });
  }

  initializeAuth(): Observable<boolean> {
    return this.verifyToken().pipe(
      tap(isValid => {
        if (!isValid) {
          this.currentUserSubject.next(null);
        }
      })
    );
  }

  getUserType(): 'PACIENTE' | 'FUNCIONARIO' | null {
    const user = this.getCurrentUser();
    return user ? user.tipo : null;
  }
}
