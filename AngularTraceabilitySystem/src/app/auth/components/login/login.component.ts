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
