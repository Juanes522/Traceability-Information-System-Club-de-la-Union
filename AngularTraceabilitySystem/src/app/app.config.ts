import { ApplicationConfig, APP_INITIALIZER, isDevMode, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideServiceWorker } from '@angular/service-worker';
import { routes } from './app.routes';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { AuthService } from './core/services/auth.service';
import { TokenService } from './core/services/token.service';
import { AuthApiService } from './core/services/auth-api.service';

function initAuth(authService: AuthService): () => void {
  return () => authService.init();
}

/**
 * Configuración de arranque de la aplicación: enrutamiento, cliente HTTP, trabajador de servicio e inicialización.
 *
 * @remarks
 * **El orden de los interceptores importa:** el que añade el token se declara primero y el de errores después, de modo que
 * este último envuelve al primero y ve los fallos de las peticiones ya autenticadas.
 *
 * Registra explícitamente los tres servicios de sesión porque ninguno se declara de ámbito raíz por su cuenta; ese registro
 * es por tanto necesario, no decorativo.
 *
 * La inicialización que registra es la pieza que sostiene todas las guardas: rehidrata la sesión desde el almacenamiento
 * **antes de que se resuelva la primera ruta**, y sin ella una recarga de página expulsaría al usuario.
 *
 * El trabajador de servicio se registra solo en compilaciones de producción.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([jwtInterceptor, errorInterceptor])),
    provideServiceWorker('ngsw-worker.js', {
      enabled: !isDevMode(),
      registrationStrategy: 'registerWhenStable:30000',
    }),
    TokenService,
    AuthApiService,
    AuthService,
    {
      provide: APP_INITIALIZER,
      useFactory: initAuth,
      deps: [AuthService],
      multi: true,
    },
  ],
};
