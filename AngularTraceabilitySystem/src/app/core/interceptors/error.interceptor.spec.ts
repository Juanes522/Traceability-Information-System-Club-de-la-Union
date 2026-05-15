import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { errorInterceptor } from './error.interceptor';
import { AuthService } from '../services/auth.service';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', ['logout']);
    router      = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authService },
        { provide: Router,      useValue: router },
      ],
    });

    http     = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('401 llama a AuthService.logout()', () => {
    http.get('/api/test').subscribe({ error: () => {} });
    httpMock.expectOne('/api/test').flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    expect(authService.logout).toHaveBeenCalled();
  });

  it('403 navega a /unauthorized', () => {
    http.get('/api/test').subscribe({ error: () => {} });
    httpMock.expectOne('/api/test').flush('Forbidden', { status: 403, statusText: 'Forbidden' });
    expect(router.navigate).toHaveBeenCalledWith(['/unauthorized']);
  });

  it('500 propaga el error sin redirigir', () => {
    let caught = false;
    http.get('/api/test').subscribe({ error: () => (caught = true) });
    httpMock.expectOne('/api/test').flush('Error', { status: 500, statusText: 'Server Error' });
    expect(caught).toBeTrue();
    expect(authService.logout).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
