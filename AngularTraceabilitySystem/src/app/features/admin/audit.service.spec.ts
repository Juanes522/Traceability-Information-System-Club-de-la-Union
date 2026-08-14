import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuditService } from './audit.service';
import { API_BASE } from '../../core/config/api.config';

describe('AuditService', () => {
  let service: AuditService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AuditService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuditService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('construye los query params solo con los filtros presentes', () => {
    service.search({ username: '123', eventType: 'LOGIN_FAILED', page: 0, size: 20 }).subscribe();
    const req = httpMock.expectOne(
      `${API_BASE}/audit?username=123&eventType=LOGIN_FAILED&page=0&size=20`,
    );
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0, number: 0, size: 20 });
  });
});
