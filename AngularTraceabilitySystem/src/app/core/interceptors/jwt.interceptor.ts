import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenService } from '../services/token.service';

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
