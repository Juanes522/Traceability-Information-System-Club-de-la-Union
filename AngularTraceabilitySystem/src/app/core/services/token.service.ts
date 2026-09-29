import { Injectable } from '@angular/core';
import { UserSession } from '../../shared/models';

const SESSION_KEY = 'auth_session';

/**
 * Persiste la sesión del usuario en el almacenamiento del navegador.
 *
 * @remarks
 * Es la única puerta de acceso al almacenamiento: ningún otro código lee ni escribe la clave de sesión directamente.
 *
 * Usa el almacenamiento **de pestaña**, no el persistente. La consecuencia es deliberada y conviene conocerla: la sesión no
 * se comparte entre pestañas y desaparece al cerrar la actual, de modo que abrir el sistema en una pestaña nueva obliga a
 * iniciar sesión otra vez.
 *
 * La lectura tolera contenido corrupto devolviendo una sesión vacía en lugar de propagar el error, para que un
 * almacenamiento manipulado no impida arrancar la aplicación.
 *
 * Nótese que el token se guarda tal cual y es legible por cualquier script de la página.
 */
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
