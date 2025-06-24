import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.getToken();

  let authReq = req;
  if (token && !req.url.includes('/login') && !req.url.includes('/verify')) {
    authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
  }

  return next(authReq).pipe(
    catchError(error => {
      // if (error.status === 401 || error.status === 403) {
      if (error.status === 401) {
        console.warn('Authentication failed, logging out user');
        authService.logout();
        router.navigate(['/login'], {
          queryParams: {
            message: 'Sua sessão expirou. Faça login novamente.'
          }
        });
      }
      return throwError(() => error);
    })
  );
};
