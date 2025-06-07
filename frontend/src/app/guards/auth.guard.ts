import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map, catchError, of } from 'rxjs';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.getToken()) {
    router.navigate(['/login']);
    return false;
  }

  return authService.verifyToken().pipe(
    map(isValid => {
      if (isValid) {
        return true;
      } else {
        router.navigate(['/login']);
        return false;
      }
    }),
    catchError(() => {
      router.navigate(['/login']);
      return of(false);
    })
  );
};

export const loginGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    const userType = authService.getUserType();
    if (userType === 'FUNCIONARIO') {
      router.navigate(['/dashboard/funcionario']);
    } else if (userType === 'PACIENTE') {
      router.navigate(['/dashboard/paciente']);
    } else {
      router.navigate(['/dashboard']);
    }
    return false;
  } else {
    return true;
  }
};
