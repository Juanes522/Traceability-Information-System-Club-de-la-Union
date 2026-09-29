import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-system-users',
  templateUrl: './system-users.component.html',
  imports: [],
})
/**
 * Gestión de usuarios del sistema. **Marcador de posición completamente desconectado.**
 *
 * @remarks
 * Su plantilla anuncia que el módulo está en construcción. No tiene ruta, no tiene entrada de navegación y **ni siquiera
 * figura en las importaciones de su módulo**: es el elemento más desconectado del proyecto, y aun así tiene una prueba.
 *
 * La interfaz de usuario del sistema que lo acompaña en el modelo compartido tampoco se usa en ningún otro sitio.
 *
 * Nótese que el sistema no tiene en realidad una entidad de usuario separada: los gestores y administradores son filas de la
 * tabla de socios distinguidas por su rol, de modo que esta pantalla tendría que gestionar ese mismo recurso.
 */
export class SystemUsersComponent {}
