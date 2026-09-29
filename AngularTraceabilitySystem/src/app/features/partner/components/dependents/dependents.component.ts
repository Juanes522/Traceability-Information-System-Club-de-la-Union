import { Component } from '@angular/core';

@Component({
  selector: 'app-partner-dependents',
  template: '',
  imports: [],
})
/**
 * Pantalla de dependientes del socio. **Vacía y no enrutada.**
 *
 * @remarks
 * La clase declara una plantilla vacía en línea. Junto a ella conviven un archivo de plantilla y una hoja de estilos
 * **completamente escritos**, que referencian propiedades y métodos que esta clase no tiene; al no declararse sus rutas, esos
 * dos archivos son huérfanos inalcanzables.
 *
 * La causa de fondo está en el backend: **no modela ninguna jerarquía de titular y dependientes**, de modo que la pantalla
 * nunca pudo completarse. El campo de parentesco del modelo del cliente, que solo esa plantilla huérfana usa, tampoco tiene
 * respaldo en el servidor.
 *
 * El componente no tiene ruta ni entrada de navegación, pero sí una prueba que verifica que se puede instanciar.
 */
export class DependentsComponent {}
