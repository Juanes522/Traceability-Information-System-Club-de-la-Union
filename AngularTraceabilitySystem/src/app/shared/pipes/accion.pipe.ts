import { Pipe, PipeTransform } from '@angular/core';

/**
 * Formatea un número de acción rellenándolo con ceros a la izquierda.
 *
 * @remarks
 * Es el único filtro de presentación de la aplicación. Da a los números de acción una anchura uniforme, que es como los
 * identifica el club.
 *
 * Nótese que el número de acción **no es único** en el modelo: varias personas pueden compartir una misma acción.
 */
@Pipe({ name: 'accion', standalone: true })
export class AccionPipe implements PipeTransform {
  transform(value: number | null | undefined): string {
    return value == null ? '' : String(value).padStart(6, '0');
  }
}
