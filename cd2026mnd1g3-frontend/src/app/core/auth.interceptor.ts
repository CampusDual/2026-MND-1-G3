import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const isLoginRequest = request.method === 'GET' && /\/api\/users(?:\?|$)/.test(request.url);
  const authHeader = auth.getAuthHeader();
  if (!isLoginRequest && authHeader) {
    request = request.clone({ setHeaders: { Authorization: authHeader } });
  }

  return next(request).pipe(catchError((error: unknown) => {
    if (error instanceof HttpErrorResponse && error.status === 401 && !isLoginRequest) {
      auth.logout();
      void router.navigate(['/login']);
    }
    return throwError(() => error);
  }));
};
