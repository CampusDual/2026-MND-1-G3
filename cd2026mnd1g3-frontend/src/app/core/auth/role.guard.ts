import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { Role } from '../models/domain.models';

export const roleGuard = (allowedRoles: readonly Role[]): CanActivateFn => () => {
  const authService: AuthService = inject(AuthService);
  const router: Router = inject(Router);
  return authService.hasAnyRole(allowedRoles)
    ? true
    : router.createUrlTree(['/portal']);
};
