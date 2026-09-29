import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

/**
 * Redirige a la pantalla de cambio de contraseña cuando la sesión lo exige.
 *
 * @remarks
 * **No está conectada a ninguna ruta:** es código muerto. Una búsqueda en el proyecto no encuentra referencias fuera de este
 * archivo y de su propia prueba.
 *
 * El cambio forzado de contraseña se resuelve con un modal bloqueante en el contenedor principal, y no redirigiendo. Esa
 * decisión fue deliberada —la pantalla a la que esta guarda redirige es además inalcanzable por estar tras la guarda
 * inversa—, pero la guarda quedó en el código, con prueba incluida.
 */
export const passwordChangeGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  return authService.needsPasswordChange$.pipe(
    take(1),
    map((needs) => !needs || router.createUrlTree(['/auth/change-password'])),
  );
};
