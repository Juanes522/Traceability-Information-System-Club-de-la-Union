import { Injectable } from '@angular/core';
import { UserSession } from '../../shared/models';

const SESSION_KEY = 'auth_session';

@Injectable()
export class TokenService {
  guardarSesion(session: UserSession): void {
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
  }

  obtenerSesion(): UserSession | null {
    const raw = sessionStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as UserSession;
    } catch {
      return null;
    }
  }

  obtenerToken(): string | null {
    return this.obtenerSesion()?.token ?? null;
  }

  limpiar(): void {
    sessionStorage.removeItem(SESSION_KEY);
  }

  existeSesion(): boolean {
    return this.obtenerSesion() !== null;
  }
}
