import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { map, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

/**
 * Restringe una rama de la aplicación al rol declarado en los datos de la ruta.
 *
 * @remarks
 * Compara el rol de la sesión con el que la ruta exige y, si no coinciden, redirige a la pantalla de acceso no autorizado.
 *
 * Es control de **navegación**, no de seguridad: un usuario que evite la interfaz sigue enfrentándose a la autorización del
 * backend, que es la frontera real.
 *
 * Lee el rol requerido con notación de índice porque la configuración de TypeScript del proyecto prohíbe el acceso por
 * propiedad sobre firmas de índice.
 */
export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const required = route.data['role'] as string;
  return authService.role$.pipe(
    take(1),
    map((role) => (role !== null && role === required) || router.createUrlTree(['/unauthorized'])),
  );
};
