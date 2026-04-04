import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from './auth.service';
import { TokenService } from './token.service';
import { AuthApiService } from './auth-api.service';
import { UserSession } from '../../shared/models';

const PARTNER_SESSION: UserSession = { token: 'tok', role: 'ROLE_PARTNER', needsPasswordChange: false };
const FORCE_SESSION:   UserSession = { token: 'tok', role: 'ROLE_PARTNER', needsPasswordChange: true  };

describe('AuthService', () => {
  let service: AuthService;
  let tokenService: jasmine.SpyObj<TokenService>;
  let authApi: jasmine.SpyObj<AuthApiService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    tokenService = jasmine.createSpyObj('TokenService', ['guardarSesion', 'obtenerSesion', 'limpiar', 'existeSesion']);
    tokenService.obtenerSesion.and.returnValue(null);
    authApi      = jasmine.createSpyObj('AuthApiService', ['login', 'changePassword']);
    router       = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: TokenService,   useValue: tokenService },
        { provide: AuthApiService, useValue: authApi },
        { provide: Router,         useValue: router },
      ],
    });
    service = TestBed.inject(AuthService);
  });

  it('init rehidrata sesión desde TokenService', () => {
    tokenService.obtenerSesion.and.returnValue(PARTNER_SESSION);
    service.init();
    let result: UserSession | null = null as UserSession | null;
    service.currentUser$.subscribe((u: UserSession | null) => (result = u));
    expect(result).toEqual(PARTNER_SESSION);
  });

  it('init no emite si no hay sesión guardada', () => {
    tokenService.obtenerSesion.and.returnValue(null);
    service.init();
    let result: UserSession | null = undefined as any;
    service.currentUser$.subscribe((u: UserSession | null) => (result = u));
    expect(result).toBeNull();
  });

  it('isAuthenticated$ emite false inicialmente', (done) => {
    service.isAuthenticated$.subscribe((v: boolean) => { expect(v).toBeFalse(); done(); });
  });

  it('login guarda sesión y navega al dashboard del rol', fakeAsync(() => {
    authApi.login.and.returnValue(of(PARTNER_SESSION));
    service.login({ email: 'a@b.com', password: '123' }).subscribe();
    tick();
    expect(tokenService.guardarSesion).toHaveBeenCalledWith(PARTNER_SESSION);
    expect(router.navigate).toHaveBeenCalledWith(['/app/partner/dashboard']);
  }));

  it('login con needsPasswordChange=true navega a change-password', fakeAsync(() => {
    authApi.login.and.returnValue(of(FORCE_SESSION));
    service.login({ email: 'a@b.com', password: '123' }).subscribe();
    tick();
    expect(router.navigate).toHaveBeenCalledWith(['/auth/change-password']);
  }));

  it('logout limpia la sesión y navega a login', () => {
    service.logout();
    expect(tokenService.limpiar).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/auth/login']);
  });

  it('clearNeedsPasswordChange actualiza el estado', fakeAsync(() => {
    authApi.login.and.returnValue(of(FORCE_SESSION));
    service.login({ email: 'a@b.com', password: '123' }).subscribe();
    tick();
    service.clearNeedsPasswordChange();
    let value: boolean = true;
    service.needsPasswordChange$.subscribe((v: boolean) => (value = v));
    expect(value).toBeFalse();
  }));

  it('currentRole devuelve null cuando no hay sesión', () => {
    expect(service.currentRole).toBeNull();
  });

  it('currentRole devuelve el rol de la sesión activa', fakeAsync(() => {
    authApi.login.and.returnValue(of(PARTNER_SESSION));
    service.login({ email: 'a@b.com', password: '123' }).subscribe();
    tick();
    expect(service.currentRole).toBe('ROLE_PARTNER');
  }));
});
