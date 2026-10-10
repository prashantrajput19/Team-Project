import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  const requiredRole = route.data?.['role'];

  if (requiredRole && authService.getUserRole() !== requiredRole) {
    return router.createUrlTree(['/error'], { queryParams: { reason: 'forbidden', need: requiredRole } });
  }

  return true;
};

export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return router.createUrlTree([authService.isAdmin() ? '/admin' : '/home']);
  }

  return true;
};