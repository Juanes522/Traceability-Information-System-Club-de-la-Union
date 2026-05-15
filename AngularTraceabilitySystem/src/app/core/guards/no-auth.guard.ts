import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { combineLatest } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

const ROLE_ROUTES: Record<string, string> = {
  ROLE_PARTNER: '/app/partner/dashboard',
  ROLE_MANAGER: '/app/manager/dashboard',
  ROLE_ADMIN:   '/app/admin/dashboard',
};

export const noAuthGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  return combineLatest([authService.isAuthenticated$, authService.role$]).pipe(
    take(1),
    map(([isAuth, role]) => {
      if (!isAuth) return true;
      const route = role && ROLE_ROUTES[role] ? ROLE_ROUTES[role] : '/auth/login';
      return router.createUrlTree([route]);
    }),
  );
};
