import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

@Injectable()
export class PasswordChangeGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): Observable<boolean | UrlTree> {
    return this.authService.needsPasswordChange$.pipe(
      take(1),
      map((needs) => !needs || this.router.createUrlTree(['/auth/change-password'])),
    );
  }
}
