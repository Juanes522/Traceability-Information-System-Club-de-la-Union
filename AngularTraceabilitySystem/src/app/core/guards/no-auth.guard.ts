import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { Observable, combineLatest } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

const ROLE_ROUTES: Record<string, string> = {
  ROLE_PARTNER: '/app/partner/dashboard',
  ROLE_MANAGER: '/app/manager/dashboard',
  ROLE_ADMIN:   '/app/admin/dashboard',
};

@Injectable()
export class NoAuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): Observable<boolean | UrlTree> {
    return combineLatest([this.authService.isAuthenticated$, this.authService.role$]).pipe(
      take(1),
      map(([isAuth, role]) => {
        if (!isAuth) return true;
        const route = role && ROLE_ROUTES[role] ? ROLE_ROUTES[role] : '/auth/login';
        return this.router.createUrlTree([route]);
      }),
    );
  }
}
