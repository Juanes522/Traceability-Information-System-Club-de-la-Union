import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { NgIf, NgClass } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss'],
    imports: [
        ReactiveFormsModule,
        NgIf,
        NgClass,
        RouterLink,
    ],
})
/**
 * Formulario de inicio de sesión.
 *
 * @remarks
 * Delega en el servicio de sesión, que además de guardar el estado **navega por su cuenta** al tablero del rol. Por eso este
 * componente no decide el destino tras autenticarse.
 *
 * **Limitación conocida en los mensajes de error:** lee el motivo del fallo de un campo estructurado de la respuesta, pero el
 * backend responde a estas operaciones con texto plano. El resultado es que los mensajes del servidor se descartan y se
 * muestra siempre uno genérico de credenciales incorrectas; en particular, un bloqueo temporal por exceso de intentos se
 * anuncia como contraseña equivocada, lo que induce al usuario a insistir y prolongar el bloqueo.
 */
export class LoginComponent {
  form: FormGroup;
  loading = false;
  showPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private toast: ToastService,
  ) {
    this.form = this.fb.group({
      identification: ['', Validators.required],
      password: ['', Validators.required],
    });
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.authService.login(this.form.value).subscribe({
      next: () => { this.loading = false; },
      error: (err) => {
        this.loading = false;
        this.toast.error(err?.error?.message ?? 'Credenciales incorrectas');
      },
    });
  }
}
