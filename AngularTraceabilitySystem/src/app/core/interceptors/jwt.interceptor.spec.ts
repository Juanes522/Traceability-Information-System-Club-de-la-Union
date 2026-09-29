import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';
import { jwtInterceptor } from './jwt.interceptor';
import { TokenService } from '../services/token.service';

/**
 * Verifica a qué peticiones se añade el token de sesión.
 *
 * @remarks
 * Sus casos más valiosos son los negativos y los aparentemente contradictorios: **no** se añade al inicio de sesión, y **sí**
 * al cierre de sesión y al cambio de contraseña, aunque el backend declare públicas esas rutas. Esa asimetría es deliberada y
 * quedaría sin documentar de no ser por estas comprobaciones.
 */
describe('jwtInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let tokenService: jasmine.SpyObj<TokenService>;

  beforeEach(() => {
    tokenService = jasmine.createSpyObj('TokenService', ['obtenerToken']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting(),
        { provide: TokenService, useValue: tokenService },
      ],
    });

    http     = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('adjunta Authorization header cuando existe token', () => {
    tokenService.obtenerToken.and.returnValue('my-jwt');
    http.get('/api/profile/me').subscribe();
    const req = httpMock.expectOne('/api/profile/me');
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-jwt');
    req.flush({});
  });

  it('NO adjunta Authorization header a /auth/login', () => {
    tokenService.obtenerToken.and.returnValue('my-jwt');
    http.post('/auth/login', {}).subscribe();
    const req = httpMock.expectOne('/auth/login');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('NO adjunta Authorization header si no hay token', () => {
    tokenService.obtenerToken.and.returnValue(null);
    http.get('/api/profile/me').subscribe();
    const req = httpMock.expectOne('/api/profile/me');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('adjunta Authorization header a /auth/change-password', () => {
    tokenService.obtenerToken.and.returnValue('my-jwt');
    http.post('/auth/change-password', {}).subscribe();
    const req = httpMock.expectOne('/auth/change-password');
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-jwt');
    req.flush(null);
  });

  it('adjunta Authorization header a /auth/logout', () => {
    tokenService.obtenerToken.and.returnValue('my-jwt');
    http.post('/auth/logout', null).subscribe();
    const req = httpMock.expectOne('/auth/logout');
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-jwt');
    req.flush(null);
  });
});
