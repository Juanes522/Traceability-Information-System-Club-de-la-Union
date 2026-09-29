import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{12,100}$/;

export const PASSWORD_REQUIREMENTS_TEXT =
  'Mínimo 12 caracteres, con al menos una minúscula, una mayúscula y un dígito.';

/**
 * Valida en el cliente la política de robustez de contraseñas.
 *
 * @remarks
 * Su propósito es dar retroalimentación inmediata en el formulario; la validación que realmente cuenta es la del backend.
 *
 * **La regla está duplicada:** el backend define una equivalente por su cuenta, y las dos no derivan de una fuente común.
 * Pueden divergir en silencio, y entonces el formulario aceptaría contraseñas que el servidor rechaza, o al revés.
 *
 * Como el resto de las validaciones de formulario, deja pasar el valor vacío para que la obligatoriedad la imponga la
 * validación correspondiente.
 */
export function strongPasswordValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (!value) {
      return null;
    }
    return PASSWORD_PATTERN.test(value) ? null : { strongPassword: true };
  };
}
