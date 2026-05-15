import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthApiService } from '../../../core/services/auth-api.service';
import { AuthService } from '../../../core/services/auth.service';
import { NgIf } from '@angular/common';

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
export class ChangePasswordComponent {
  form: FormGroup;
  loading = false;
  errorMsg = '';

  constructor(
    private fb: FormBuilder,
    private authApiService: AuthApiService,
    private authService: AuthService,
    private router: Router,
  ) {
    this.form = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
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
