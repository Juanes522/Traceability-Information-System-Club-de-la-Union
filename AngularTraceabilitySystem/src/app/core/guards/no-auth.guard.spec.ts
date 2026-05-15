import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { firstValueFrom } from 'rxjs';
import { noAuthGuard } from './no-auth.guard';
import { AuthService } from '../services/auth.service';

describe('noAuthGuard', () => {
  let isAuth$: BehaviorSubject<boolean>;
  let role$: BehaviorSubject<string | null>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    isAuth$ = new BehaviorSubject<boolean>(false);
    role$   = new BehaviorSubject<string | null>(null);
    router  = jasmine.createSpyObj('Router', ['createUrlTree']);
    router.createUrlTree.and.returnValue({} as UrlTree);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: { isAuthenticated$: isAuth$, role$: role$ } },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('permite acceso si NO está autenticado', async () => {
    const result = await firstValueFrom(
      TestBed.runInInjectionContext(() => noAuthGuard({} as any, {} as any)) as any
    );
    expect(result).toBeTrue();
  });

  it('redirige al dashboard si ya está autenticado como PARTNER', async () => {
    isAuth$.next(true);
    role$.next('ROLE_PARTNER');
    await firstValueFrom(
      TestBed.runInInjectionContext(() => noAuthGuard({} as any, {} as any)) as any
    );
    expect(router.createUrlTree).toHaveBeenCalledWith(['/app/partner/dashboard']);
  });

  it('redirige a /auth/login si el rol es null al estar autenticado', async () => {
    isAuth$.next(true);
    role$.next(null);
    await firstValueFrom(
      TestBed.runInInjectionContext(() => noAuthGuard({} as any, {} as any)) as any
    );
    expect(router.createUrlTree).toHaveBeenCalledWith(['/auth/login']);
  });
});
