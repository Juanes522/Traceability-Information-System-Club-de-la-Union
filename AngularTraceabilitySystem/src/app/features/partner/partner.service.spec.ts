import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PartnerService } from './partner.service';
import { API_BASE } from '../../core/config/api.config';

describe('PartnerService', () => {
  let service: PartnerService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PartnerService],
    });
    service = TestBed.inject(PartnerService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('getProfile hace GET a la URL correcta', () => {
    service.getProfile().subscribe();
    http.expectOne(`${API_BASE}/personpartner/me`).flush({});
  });

  it('getConsumptions hace GET paginado con ventana de tiempo', () => {
    service.getConsumptions({ from: '2026-08-01T00:00', to: '2026-08-08T00:00', page: 0, size: 10 }).subscribe();
    const req = http.expectOne(
      `${API_BASE}/personpartner/getconsumptions/me?page=0&size=10&from=2026-08-01T00:00&to=2026-08-08T00:00`
    );
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0, number: 0, size: 10 });
  });

  it('getConsumptions omite from/to cuando no se pasan', () => {
    service.getConsumptions({ page: 1, size: 10 }).subscribe();
    const req = http.expectOne(`${API_BASE}/personpartner/getconsumptions/me?page=1&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0, number: 1, size: 10 });
  });
});
