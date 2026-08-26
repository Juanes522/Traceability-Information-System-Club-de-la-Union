import { Injectable, NgZone } from '@angular/core';
import { AuthService } from './auth.service';
import { ToastService } from './toast.service';

const IDLE_TIMEOUT_MS = 15 * 60 * 1000;
const ACTIVITY_EVENTS = ['mousemove', 'keydown', 'click', 'scroll', 'touchstart'];

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
