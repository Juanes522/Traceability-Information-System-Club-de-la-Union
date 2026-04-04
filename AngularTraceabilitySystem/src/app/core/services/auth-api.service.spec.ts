import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthApiService } from './auth-api.service';
import { API_BASE } from '../config/api.config';
import { UserSession, ChangePasswordRequest } from '../../shared/models';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthApiService],
    });
    service = TestBed.inject(AuthApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('login hace POST a /auth/login y retorna UserSession', (done) => {
    const mockSession: UserSession = { token: 'tok', role: 'ROLE_PARTNER', needsPasswordChange: false };

    service.login({ email: 'a@b.com', password: '123' }).subscribe((session) => {
      expect(session).toEqual(mockSession);
      done();
    });

    const req = httpMock.expectOne(`${API_BASE}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'a@b.com', password: '123' });
    req.flush(mockSession);
  });

  it('changePassword hace POST a /auth/change-password', (done) => {
    const payload: ChangePasswordRequest = { newPassword: 'new123' };

    service.changePassword(payload).subscribe(() => done());

    const req = httpMock.expectOne(`${API_BASE}/auth/change-password`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(null);
  });
});
