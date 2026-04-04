import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { firstValueFrom } from 'rxjs';
import { AuthGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let isAuth$: BehaviorSubject<boolean>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    isAuth$ = new BehaviorSubject<boolean>(false);
    router  = jasmine.createSpyObj('Router', ['createUrlTree']);
    router.createUrlTree.and.returnValue({} as UrlTree);

    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: AuthService, useValue: { isAuthenticated$: isAuth$ } },
        { provide: Router, useValue: router },
      ],
    });
    guard = TestBed.inject(AuthGuard);
  });

  it('permite acceso si el usuario está autenticado', async () => {
    isAuth$.next(true);
    const result = await firstValueFrom(guard.canActivate());
    expect(result).toBeTrue();
  });

  it('redirige a /auth/login si no está autenticado', async () => {
    const result = await firstValueFrom(guard.canActivate());
    expect(router.createUrlTree).toHaveBeenCalledWith(['/auth/login']);
  });
});
