import { TestBed } from '@angular/core/testing';
import { TokenService } from './token.service';
import { UserSession } from '../../shared/models';

const MOCK_SESSION: UserSession = {
  token: 'test-jwt-token',
  role: 'ROLE_PARTNER',
  needsPasswordChange: false,
};

describe('TokenService', () => {
  let service: TokenService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [TokenService] });
    service = TestBed.inject(TokenService);
    sessionStorage.clear();
  });

  afterEach(() => sessionStorage.clear());

  it('guardarSesion / obtenerSesion hace roundtrip correcto', () => {
    service.guardarSesion(MOCK_SESSION);
    expect(service.obtenerSesion()).toEqual(MOCK_SESSION);
  });

  it('obtenerToken devuelve el token de la sesión guardada', () => {
    service.guardarSesion(MOCK_SESSION);
    expect(service.obtenerToken()).toBe('test-jwt-token');
  });

  it('obtenerToken devuelve null si no hay sesión', () => {
    expect(service.obtenerToken()).toBeNull();
  });

  it('limpiar elimina la sesión de sessionStorage', () => {
    service.guardarSesion(MOCK_SESSION);
    service.limpiar();
    expect(service.obtenerSesion()).toBeNull();
  });

  it('existeSesion devuelve false si no hay sesión', () => {
    expect(service.existeSesion()).toBeFalse();
  });

  it('existeSesion devuelve true si hay sesión guardada', () => {
    service.guardarSesion(MOCK_SESSION);
    expect(service.existeSesion()).toBeTrue();
  });

  it('obtenerSesion devuelve null si sessionStorage tiene JSON inválido', () => {
    sessionStorage.setItem('auth_session', 'not-valid-json');
    expect(service.obtenerSesion()).toBeNull();
  });
});
