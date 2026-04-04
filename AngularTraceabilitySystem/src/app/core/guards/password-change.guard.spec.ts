import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { firstValueFrom } from 'rxjs';
import { PasswordChangeGuard } from './password-change.guard';
import { AuthService } from '../services/auth.service';

describe('PasswordChangeGuard', () => {
  let guard: PasswordChangeGuard;
  let needsChange$: BehaviorSubject<boolean>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    needsChange$ = new BehaviorSubject<boolean>(false);
    router       = jasmine.createSpyObj('Router', ['createUrlTree']);
    router.createUrlTree.and.returnValue({} as UrlTree);

    TestBed.configureTestingModule({
      providers: [
        PasswordChangeGuard,
        { provide: AuthService, useValue: { needsPasswordChange$: needsChange$ } },
        { provide: Router, useValue: router },
      ],
    });
    guard = TestBed.inject(PasswordChangeGuard);
  });

  it('permite acceso si needsPasswordChange es false', async () => {
    const r = await firstValueFrom(guard.canActivate());
    expect(r).toBeTrue();
  });

  it('redirige a /auth/change-password si needsPasswordChange es true', async () => {
    needsChange$.next(true);
    const r = await firstValueFrom(guard.canActivate());
    expect(router.createUrlTree).toHaveBeenCalledWith(['/auth/change-password']);
  });
});
