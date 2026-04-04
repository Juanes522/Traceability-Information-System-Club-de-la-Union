import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, UrlTree } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { firstValueFrom } from 'rxjs';
import { RoleGuard } from './role.guard';
import { AuthService } from '../services/auth.service';

describe('RoleGuard', () => {
  let guard: RoleGuard;
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
        RoleGuard,
        { provide: AuthService, useValue: { role$: role$ } },
        { provide: Router, useValue: router },
      ],
    });
    guard = TestBed.inject(RoleGuard);
  });

  it('permite acceso si el rol coincide', async () => {
    role$.next('ROLE_PARTNER');
    const r = await firstValueFrom(guard.canActivate(mockRoute('ROLE_PARTNER')));
    expect(r).toBeTrue();
  });

  it('redirige a /unauthorized si el rol no coincide', async () => {
    role$.next('ROLE_ADMIN');
    await firstValueFrom(guard.canActivate(mockRoute('ROLE_PARTNER')));
    expect(router.createUrlTree).toHaveBeenCalledWith(['/unauthorized']);
  });

  it('redirige a /unauthorized si el rol es null', async () => {
    role$.next(null);
    const result = await firstValueFrom(guard.canActivate(mockRoute('ROLE_PARTNER')));
    expect(router.createUrlTree).toHaveBeenCalledWith(['/unauthorized']);
  });
});
