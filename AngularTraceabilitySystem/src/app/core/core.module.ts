import { NgModule, ModuleWithProviders, Optional, SkipSelf, APP_INITIALIZER } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

// Services
import { TokenService } from './services/token.service';
import { AuthApiService } from './services/auth-api.service';
import { AuthService } from './services/auth.service';


export function initAuth(authService: AuthService): () => void {
  return () => authService.init();
}

@NgModule({
  imports: [CommonModule, HttpClientModule],
})
/**
 * Módulo de infraestructura. **Código muerto.**
 *
 * @remarks
 * Duplica el registro de los servicios de sesión y la inicialización que hoy viven en la configuración de arranque, en la
 * forma antigua basada en módulos, con la salvaguarda clásica contra la doble importación.
 *
 * No lo importa nadie, pero **sí tiene pruebas**, de modo que el conjunto de pruebas verifica código que no se ejecuta e
 * infla las cifras de cobertura. Importa además un módulo de cliente HTTP ya obsoleto.
 */
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error('CoreModule already loaded. Import only in AppModule.');
    }
  }

  static forRoot(): ModuleWithProviders<CoreModule> {
    return {
      ngModule: CoreModule,
      providers: [
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
  }
}
