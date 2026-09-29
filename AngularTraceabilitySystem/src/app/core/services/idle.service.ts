import { Injectable, NgZone } from '@angular/core';
import { AuthService } from './auth.service';
import { ToastService } from './toast.service';

const IDLE_TIMEOUT_MS = 15 * 60 * 1000;
const ACTIVITY_EVENTS = ['mousemove', 'keydown', 'click', 'scroll', 'touchstart'];

/**
 * Cierra la sesión automáticamente tras un periodo de inactividad.
 *
 * @remarks
 * Es una medida de seguridad para equipos compartidos: vigila varios tipos de interacción del usuario y reinicia un
 * temporizador con cada una.
 *
 * Los oyentes se registran **fuera del ciclo de detección de cambios** de la aplicación, algo necesario porque el
 * seguimiento del puntero dispararía comprobaciones continuas y degradaría el rendimiento de toda la interfaz. La vuelta al
 * ciclo se hace de forma explícita solo cuando el temporizador vence.
 *
 * Lo activa y lo detiene el contenedor principal, de modo que la vigilancia **no se aplica en las pantallas de
 * autenticación**, donde no tendría sentido.
 */
@Injectable({ providedIn: 'root' })
export class IdleService {
  private timer: ReturnType<typeof setTimeout> | null = null;
  private readonly handler = () => this.reset();

  constructor(private authService: AuthService, private toast: ToastService, private zone: NgZone) {}

  start(): void {
    this.zone.runOutsideAngular(() => {
      ACTIVITY_EVENTS.forEach(e => document.addEventListener(e, this.handler, true));
    });
    this.reset();
  }

  stop(): void {
    ACTIVITY_EVENTS.forEach(e => document.removeEventListener(e, this.handler, true));
    if (this.timer) { clearTimeout(this.timer); this.timer = null; }
  }

  private reset(): void {
    if (this.timer) { clearTimeout(this.timer); }
    this.timer = setTimeout(() => this.onTimeout(), IDLE_TIMEOUT_MS);
  }

  private onTimeout(): void {
    this.stop();
    this.zone.run(() => {
      this.toast.success('Sesión cerrada por inactividad.');
      this.authService.logout('inactividad');
    });
  }
}
