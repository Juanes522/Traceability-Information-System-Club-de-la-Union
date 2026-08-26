import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../../core/services/auth-api.service';
import { NgIf, NgClass } from '@angular/common';
import { strongPasswordValidator, PASSWORD_REQUIREMENTS_TEXT } from '../../../core/validators/password.validator';

@Component({
    selector: 'app-reset-password',
    templateUrl: './reset-password.component.html',
    styleUrls: ['./reset-password.component.scss'],
    imports: [
        NgIf,
        RouterLink,
        ReactiveFormsModule,
        NgClass,
    ],
})
export class ResetPasswordComponent implements OnInit {
  form: FormGroup;
  loading = false;
  done = false;
  error = '';
  showPassword = false;
  readonly passwordHint = PASSWORD_REQUIREMENTS_TEXT;
  private token = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authApi: AuthApiService
  ) {
    this.form = this.fb.group({
      newPassword: ['', [Validators.required, strongPasswordValidator()]],
    });
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) {
      this.error = 'El enlace de recuperación es inválido o ha expirado.';
    }
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  submit(): void {
    if (this.form.invalid || !this.token) return;
    this.loading = true;
    this.error = '';
    this.authApi.resetPassword({ token: this.token, newPassword: this.form.value.newPassword }).subscribe({
      next: () => {
        this.loading = false;
        this.done = true;
      },
      error: () => {
        this.loading = false;
        this.error = 'El enlace de recuperación es inválido o ha expirado. Solicita uno nuevo.';
      },
    });
  }
}
