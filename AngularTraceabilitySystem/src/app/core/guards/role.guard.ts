import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { map, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const required = route.data['role'] as string;
  return authService.role$.pipe(
    take(1),
    map((role) => (role !== null && role === required) || router.createUrlTree(['/unauthorized'])),
  );
};
