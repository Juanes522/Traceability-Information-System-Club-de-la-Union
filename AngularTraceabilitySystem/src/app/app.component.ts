import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastComponent } from './shared/components/toast/toast.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  imports: [RouterOutlet, ToastComponent],
})
/**
 * Componente raíz de la aplicación.
 *
 * @remarks
 * Su plantilla se reduce a dos elementos: el punto de montaje del enrutador y la pila de avisos. Colocar los avisos aquí,
 * **fuera del área enrutada**, es lo que permite que sobrevivan a la navegación.
 *
 * No contiene lógica: el estado de sesión, el diseño y los modales bloqueantes viven en el contenedor principal.
 */
export class AppComponent {
  title = 'AngularTraceabilitySystem';
}
