import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenService } from '../services/token.service';

/**
 * Añade el token de sesión a las peticiones salientes que lo requieren.
 *
 * @remarks
 * Excluye las tres operaciones que por naturaleza son anónimas: inicio de sesión, solicitud de recuperación y
 * restablecimiento de contraseña.
 *
 * Nótese que **sí** añade el token al cierre de sesión y al cambio de contraseña, aunque el backend declare públicas esas
 * rutas: el cierre de sesión necesita el token para poder revocarlo, y el cambio de contraseña para identificar al usuario.
 *
 * La exclusión se decide buscando una subcadena en la dirección completa, no analizando la ruta. Una dirección que
 * contuviera una de esas cadenas en otra posición —por ejemplo en un parámetro de consulta— se trataría erróneamente como
 * anónima.
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const token = tokenService.obtenerToken();
  const isPublicEndpoint = req.url.includes('/auth/login')
    || req.url.includes('/auth/forgot-password')
    || req.url.includes('/auth/reset-password');

  if (token && !isPublicEndpoint) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    });
  }

  return next(req);
};
