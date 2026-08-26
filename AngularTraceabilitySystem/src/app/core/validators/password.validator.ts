import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{12,100}$/;

export const PASSWORD_REQUIREMENTS_TEXT =
  'Mínimo 12 caracteres, con al menos una minúscula, una mayúscula y un dígito.';

export function strongPasswordValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (!value) {
      return null;
    }
    return PASSWORD_PATTERN.test(value) ? null : { strongPassword: true };
  };
}
