/**
 * Punto de arranque de la aplicación.
 *
 * @remarks
 * Arranca con la API de componentes independientes y no con un módulo raíz. El módulo raíz sigue existiendo en el proyecto,
 * pero está vacío y no participa: es un resto de la forma anterior de arrancar.
 */
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

bootstrapApplication(AppComponent, appConfig)
  .catch(err => console.error(err));
