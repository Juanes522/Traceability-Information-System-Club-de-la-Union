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

/**
 * Impide que un usuario con sesión activa vuelva a las pantallas de autenticación.
 *
 * @remarks
 * Si ya hay sesión, redirige al tablero que corresponde a su rol en lugar de mostrar el formulario de inicio de sesión.
 *
 * **Efecto colateral que conviene conocer:** protege *todas* las rutas de autenticación, incluida la de cambio de contraseña.
 * Como esa pantalla está pensada precisamente para usuarios autenticados, esta guarda la vuelve inalcanzable. El cambio
 * forzado de contraseña se resuelve en la práctica mediante un modal bloqueante del contenedor principal.
 *
 * La tabla de rutas por rol que usa está **duplicada** en otros dos archivos.
 */
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
