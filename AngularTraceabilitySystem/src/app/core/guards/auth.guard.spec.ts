import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { firstValueFrom } from 'rxjs';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
  let isAuth$: BehaviorSubject<boolean>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    isAuth$ = new BehaviorSubject<boolean>(false);
    router  = jasmine.createSpyObj('Router', ['createUrlTree']);
    router.createUrlTree.and.returnValue({} as UrlTree);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: { isAuthenticated$: isAuth$ } },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('permite acceso si el usuario está autenticado', async () => {
    isAuth$.next(true);
    const result = await firstValueFrom(
      TestBed.runInInjectionContext(() => authGuard({} as any, {} as any)) as any
    );
    expect(result).toBeTrue();
  });

  it('redirige a /auth/login si no está autenticado', async () => {
    await firstValueFrom(
      TestBed.runInInjectionContext(() => authGuard({} as any, {} as any)) as any
    );
    expect(router.createUrlTree).toHaveBeenCalledWith(['/auth/login']);
  });
});
