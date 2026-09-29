import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { strongPasswordValidator, PASSWORD_REQUIREMENTS_TEXT } from '../core/validators/password.validator';
import { Subject } from 'rxjs';
import { takeUntil, filter, take } from 'rxjs/operators';
import { AuthService } from '../core/services/auth.service';
import { AuthApiService } from '../core/services/auth-api.service';
import { ToastService } from '../core/services/toast.service';
import { PushNotificationService } from '../core/services/push-notification.service';
import { IdleService } from '../core/services/idle.service';
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
        FormsModule,
    ],
})
/**
 * Contenedor principal de la aplicación autenticada.
 *
 * @remarks
 * Es el componente con más responsabilidades del frontend, y cumple cuatro papeles a la vez:
 *
 * 1. **Diseño**, con barra lateral fija en pantallas anchas y cajón desplegable en las estrechas.
 * 2. **Modal de cambio forzado de contraseña**, que es el mecanismo real por el que se impone ese cambio: la pantalla
 *    dedicada que existe en el proyecto es inalcanzable por estar tras la guarda que expulsa a los usuarios con sesión.
 * 3. **Modal de aceptación del consentimiento**, con precedencia explícita por debajo del anterior.
 * 4. **Efectos por rol:** activa la suscripción a notificaciones solo para socios, y arranca la vigilancia de inactividad,
 *    que por eso no se aplica en las pantallas de autenticación.
 *
 * **Consideración importante sobre los modales:** son hermanos del punto de montaje del enrutador y no lo reemplazan, de modo
 * que la pantalla subyacente **se renderiza y lanza sus peticiones** por detrás del bloqueo.
 *
 * Nótese que la política de consentimiento se solicita en cada montaje del contenedor, incluso cuando el usuario ya la
 * aceptó, y esa llamada no tiene tratamiento de error.
 */
export class ShellComponent implements OnInit, OnDestroy {
  sidebarOpen = false;
  showPasswordModal = false;
  showConsentModal = false;
  consentChecked = false;
  consentLoading = false;
  consentTitle = '';
  consentText = '';

  get consentBlocks(): { type: 'heading' | 'bullet' | 'para'; text: string }[] {
    return this.consentText
      .split('\n')
      .map(l => l.trim())
      .filter(l => l.length > 0)
      .map(l => {
        if (l.startsWith('-')) {
          return { type: 'bullet' as const, text: l.replace(/^-\s*/, '') };
        }
        if (!l.endsWith('.') && !l.endsWith(':')) {
          return { type: 'heading' as const, text: l };
        }
        return { type: 'para' as const, text: l };
      });
  }

  pwForm: FormGroup;
  readonly passwordHint = PASSWORD_REQUIREMENTS_TEXT;
  pwLoading = false;

  private needsPw = false;
  private needsCons = false;

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private authApiService: AuthApiService,
    private fb: FormBuilder,
    private toast: ToastService,
    private pushService: PushNotificationService,
    private idleService: IdleService,
  ) {
    this.pwForm = this.fb.group({
      newPassword:     ['', [Validators.required, strongPasswordValidator()]],
      confirmPassword: ['', [Validators.required]],
    });
  }

  ngOnInit(): void {
    this.idleService.start();

    this.authService.needsPasswordChange$
      .pipe(takeUntil(this.destroy$))
      .subscribe(needs => { this.needsPw = needs; this.updateModals(); });

    this.authService.needsConsent$
      .pipe(takeUntil(this.destroy$))
      .subscribe(needs => { this.needsCons = needs; this.updateModals(); });

    this.authService.role$
      .pipe(
        filter(role => role === 'ROLE_PARTNER'),
        take(1),
        takeUntil(this.destroy$),
      )
      .subscribe(() => {
        this.pushService.subscribeToPartnerNotifications();
      });

    this.authApiService.getConsentPolicy().subscribe(p => {
      this.consentTitle = p.title;
      this.consentText = p.text;
    });
  }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); this.idleService.stop(); }

  private updateModals(): void {
    this.showPasswordModal = this.needsPw;
    this.showConsentModal = !this.needsPw && this.needsCons;
  }

  submitConsent(): void {
    if (!this.consentChecked || this.consentLoading) return;
    this.consentLoading = true;
    this.authApiService.acceptConsent().subscribe({
      next: () => {
        this.consentLoading = false;
        this.authService.clearNeedsConsent();
        this.toast.success('Consentimiento registrado.');
      },
      error: () => {
        this.consentLoading = false;
        this.toast.error('No se pudo registrar el consentimiento. Inténtalo de nuevo.');
      },
    });
  }

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
