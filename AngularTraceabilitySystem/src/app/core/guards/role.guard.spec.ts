import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, UrlTree } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { firstValueFrom } from 'rxjs';
import { roleGuard } from './role.guard';
import { AuthService } from '../services/auth.service';

describe('roleGuard', () => {
  let role$: BehaviorSubject<string | null>;
  let router: jasmine.SpyObj<Router>;

  const mockRoute = (role: string): ActivatedRouteSnapshot =>
    ({ data: { role } } as unknown as ActivatedRouteSnapshot);

  beforeEach(() => {
    role$  = new BehaviorSubject<string | null>(null);
    router = jasmine.createSpyObj('Router', ['createUrlTree']);
    router.createUrlTree.and.returnValue({} as UrlTree);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: { role$: role$ } },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('permite acceso si el rol coincide', async () => {
    role$.next('ROLE_PARTNER');
    const r = await firstValueFrom(
      TestBed.runInInjectionContext(() => roleGuard(mockRoute('ROLE_PARTNER'), {} as any)) as any
    );
    expect(r).toBeTrue();
  });

  it('redirige a /unauthorized si el rol no coincide', async () => {
    role$.next('ROLE_ADMIN');
    await firstValueFrom(
      TestBed.runInInjectionContext(() => roleGuard(mockRoute('ROLE_PARTNER'), {} as any)) as any
    );
    expect(router.createUrlTree).toHaveBeenCalledWith(['/unauthorized']);
  });

  it('redirige a /unauthorized si el rol es null', async () => {
    role$.next(null);
    await firstValueFrom(
      TestBed.runInInjectionContext(() => roleGuard(mockRoute('ROLE_PARTNER'), {} as any)) as any
    );
    expect(router.createUrlTree).toHaveBeenCalledWith(['/unauthorized']);
  });
});
