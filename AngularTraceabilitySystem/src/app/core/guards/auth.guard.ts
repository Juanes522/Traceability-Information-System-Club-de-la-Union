import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

/**
 * Impide el acceso a la aplicación sin sesión y redirige al inicio de sesión.
 *
 * @remarks
 * Protege toda la rama autenticada de la aplicación.
 *
 * Toma un único valor del flujo de sesión y se resuelve de forma sincrónica. Eso es correcto aquí porque la sesión se
 * rehidrata desde el almacenamiento durante la inicialización de la aplicación, **antes** de que se resuelva la primera
 * ruta; sin esa garantía, la guarda podría evaluarse con la sesión todavía vacía y expulsar a un usuario válido.
 *
 * Devuelve un árbol de URL en lugar de un booleano, de modo que la redirección forma parte del propio resultado.
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  return authService.isAuthenticated$.pipe(
    take(1),
    map((isAuth) => isAuth || router.createUrlTree(['/auth/login'])),
  );
};
