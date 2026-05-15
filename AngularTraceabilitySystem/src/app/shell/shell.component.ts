import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil, filter, take } from 'rxjs/operators';
import { AuthService } from '../core/services/auth.service';
import { AuthApiService } from '../core/services/auth-api.service';
import { ToastService } from '../core/services/toast.service';
import { PushNotificationService } from '../core/services/push-notification.service';
import { SidebarComponent } from '../shared/components/sidebar/sidebar.component';
import { RouterOutlet } from '@angular/router';
import { NgIf } from '@angular/common';

@Component({
    selector: 'app-shell',
    templateUrl: './shell.component.html',
    styleUrls: ['./shell.component.scss'],
    imports: [
        SidebarComponent,
        RouterOutlet,
        NgIf,
        ReactiveFormsModule,
    ],
})
export class ShellComponent implements OnInit, OnDestroy {
  sidebarOpen = false;
  showPasswordModal = false;

  pwForm: FormGroup;
  pwLoading = false;

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private authApiService: AuthApiService,
    private fb: FormBuilder,
    private toast: ToastService,
    private pushService: PushNotificationService,
  ) {
    this.pwForm = this.fb.group({
      newPassword:     ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
    });
  }

  ngOnInit(): void {
    this.authService.needsPasswordChange$
      .pipe(takeUntil(this.destroy$))
      .subscribe(needs => { this.showPasswordModal = needs; });

    this.authService.role$
      .pipe(
        filter(role => role === 'ROLE_PARTNER'),
        take(1),
        takeUntil(this.destroy$),
      )
      .subscribe(() => {
        this.pushService.subscribeToPartnerNotifications();
      });
  }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  toggleSidebar(): void { this.sidebarOpen = !this.sidebarOpen; }
  closeSidebar(): void  { this.sidebarOpen = false; }

  get passwordMismatch(): boolean {
    const { newPassword, confirmPassword } = this.pwForm.value;
    return confirmPassword && newPassword !== confirmPassword;
  }

  submitPassword(): void {
    if (this.pwForm.invalid || this.passwordMismatch) return;
    this.pwLoading = true;
    this.authApiService.changePassword({ newPassword: this.pwForm.value.newPassword }).subscribe({
      next: () => {
        this.pwLoading = false;
        this.authService.clearNeedsPasswordChange();
        this.pwForm.reset();
        this.toast.success('Contraseña actualizada correctamente.');
      },
      error: (err) => {
        this.pwLoading = false;
        const msg = err?.error?.message ?? 'No se pudo actualizar la contraseña. Inténtalo de nuevo.';
        this.toast.error(msg);
      },
    });
  }
}
