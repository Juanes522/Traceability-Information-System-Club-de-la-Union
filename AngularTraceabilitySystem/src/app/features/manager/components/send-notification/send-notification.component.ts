import { Component } from '@angular/core';

@Component({
    selector: 'app-manager-send-notification',
    templateUrl: './send-notification.component.html',
})
/**
 * Envío manual de notificaciones. **Marcador de posición sin funcionalidad.**
 *
 * @remarks
 * Está enrutado pero su plantilla anuncia que el módulo está en construcción, y **el backend no expone ningún endpoint** para
 * enviar notificaciones a demanda: las únicas que existen se generan automáticamente al registrar un consumo.
 *
 * Completar esta pantalla exigiría por tanto trabajo en el servidor, no solo en el cliente.
 */
export class SendNotificationComponent {}
