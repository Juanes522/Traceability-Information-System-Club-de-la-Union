import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthApiService } from '../../../core/services/auth-api.service';
import { AuthService } from '../../../core/services/auth.service';
import { NgIf } from '@angular/common';
import { strongPasswordValidator, PASSWORD_REQUIREMENTS_TEXT } from '../../../core/validators/password.validator';

const ROLE_ROUTES: Record<string, string> = {
  ROLE_PARTNER: '/app/partner/dashboard',
  ROLE_MANAGER: '/app/manager/dashboard',
  ROLE_ADMIN: '/app/admin/dashboard',
};

@Component({
    selector: 'app-change-password',
    templateUrl: './change-password.component.html',
    imports: [ReactiveFormsModule, NgIf],
})
/**
 * Pantalla de cambio de contraseña. **Inalcanzable en la práctica.**
 *
 * @remarks
 * Su ruta está tras la guarda que expulsa a los usuarios con sesión, de modo que el único público al que serviría —quien está
 * autenticado y debe rotar su contraseña— nunca puede llegar a ella.
 *
 * Esa función la cumple realmente un modal bloqueante del contenedor principal, que reimplementa el mismo formulario.
 *
 * Se documenta en lugar de ignorarse porque el componente está enrutado y probado, de modo que parece vivo al leer el
 * proyecto.
 */
export class ChangePasswordComponent {
  form: FormGroup;
  loading = false;
  errorMsg = '';
  readonly passwordHint = PASSWORD_REQUIREMENTS_TEXT;

  constructor(
    private fb: FormBuilder,
    private authApiService: AuthApiService,
    private authService: AuthService,
    private router: Router,
  ) {
    this.form = this.fb.group({
      newPassword: ['', [Validators.required, strongPasswordValidator()]],
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.errorMsg = '';
    this.authApiService.changePassword({ newPassword: this.form.value.newPassword }).subscribe({
      next: () => {
        this.loading = false;
        this.authService.clearNeedsPasswordChange();
        const route = this.authService.currentRole
          ? (ROLE_ROUTES[this.authService.currentRole] ?? '/auth/login')
          : '/auth/login';
        this.router.navigate([route]);
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message ?? 'No se pudo actualizar la contraseña';
      },
    });
  }
}
