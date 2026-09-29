import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
    selector: 'app-unauthorized',
    templateUrl: './unauthorized.component.html',
})
/**
 * Pantalla que se muestra al intentar entrar en una sección ajena al rol de la sesión.
 *
 * @remarks
 * Es el destino de la guarda de rol.
 *
 * **Incoherencia conocida:** su botón invita a volver al inicio pero navega a la pantalla de inicio de sesión. Para un
 * usuario con sesión activa eso funciona por casualidad, porque la guarda inversa lo devuelve a su propio tablero; el
 * resultado correcto se alcanza tras dos redirecciones.
 */
export class UnauthorizedComponent {
  constructor(private router: Router) {}

  goBack(): void {
    this.router.navigate(['/auth/login']);
  }
}
