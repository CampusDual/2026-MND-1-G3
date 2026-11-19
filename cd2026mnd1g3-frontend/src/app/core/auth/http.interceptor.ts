import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authService: AuthService = inject(AuthService);
  const router: Router = inject(Router);
  const authHeader: string = authService.getAuthHeader();
  const authenticatedRequest = authHeader
    ? request.clone({ setHeaders: { Authorization: authHeader } })
    : request;

  return next(authenticatedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        authService.logout();
        void router.navigate(['/login']);
      } else if (error.status === 403) {
        void router.navigate(['/portal']);
      }
      return throwError(() => error);
    })
  );
};
