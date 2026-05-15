import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

export const passwordChangeGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  return authService.needsPasswordChange$.pipe(
    take(1),
    map((needs) => !needs || router.createUrlTree(['/auth/change-password'])),
  );
};
