import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Reacciona de forma centralizada a los errores de autenticación y autorización.
 *
 * @remarks
 * Ante un rechazo por falta de sesión cierra la sesión local; ante uno por falta de permisos navega a la pantalla de acceso
 * no autorizado. En ambos casos vuelve a propagar el error, de modo que cada llamante puede además mostrar su propio
 * mensaje.
 *
 * **No distingue de qué petición proviene el error, y eso tiene una consecuencia observable:** un intento de inicio de
 * sesión con credenciales incorrectas también produce un rechazo por falta de sesión, así que este interceptor dispara un
 * cierre de sesión completo —con su llamada al backend y su evento de auditoría— por cada contraseña mal escrita. No entra
 * en bucle porque el backend acepta esa llamada sin sesión.
 *
 * Tampoco contempla el rechazo por exceso de peticiones, cuyo cuerpo el backend devuelve como texto plano; el llamante no
 * consigue extraer el mensaje y muestra uno genérico de credenciales incorrectas, que es engañoso.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        authService.logout();
      } else if (error.status === 403) {
        router.navigate(['/unauthorized']);
      }
      return throwError(() => error);
    }),
  );
};
