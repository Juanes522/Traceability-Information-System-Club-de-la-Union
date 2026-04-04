import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { TokenService } from './token.service';
import { AuthApiService } from './auth-api.service';
import { UserSession } from '../../shared/models';

const ROLE_ROUTES: Record<string, string> = {
  ROLE_PARTNER: '/app/partner/dashboard',
  ROLE_MANAGER: '/app/manager/dashboard',
  ROLE_ADMIN:   '/app/admin/dashboard',
};

@Injectable()
export class AuthService {
  private subject = new BehaviorSubject<UserSession | null>(null);

  currentUser$         = this.subject.asObservable();
  isAuthenticated$     = this.currentUser$.pipe(map((u) => u !== null));
  role$                = this.currentUser$.pipe(map((u) => u?.role ?? null));
  needsPasswordChange$ = this.currentUser$.pipe(map((u) => u?.needsPasswordChange ?? false));

  constructor(
    private tokenService: TokenService,
    private authApi: AuthApiService,
    private router: Router,
  ) {}

  /** Called from APP_INITIALIZER on app startup */
  init(): void {
    const session = this.tokenService.obtenerSesion();
    if (session) {
      this.subject.next(session);
    }
  }

  login(credentials: { email: string; password: string }): Observable<void> {
    return this.authApi.login(credentials).pipe(
      tap((session) => {
        this.tokenService.guardarSesion(session);
        this.subject.next(session);
        this.router.navigate([ROLE_ROUTES[session.role]]);
      }),
      map(() => void 0),
    );
  }

  logout(): void {
    this.tokenService.limpiar();
    this.subject.next(null);
    this.router.navigate(['/auth/login']);
  }

  clearNeedsPasswordChange(): void {
    const current = this.subject.value;
    if (current) {
      const updated: UserSession = { ...current, needsPasswordChange: false };
      this.tokenService.guardarSesion(updated);
      this.subject.next(updated);
    }
  }

  /** Snapshot of current role — for use in components without subscription */
  get currentRole(): string | null {
    return this.subject.value?.role ?? null;
  }
}
