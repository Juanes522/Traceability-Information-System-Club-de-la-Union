import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ManagerService } from './manager.service';
import { API_BASE } from '../../core/config/api.config';

describe('ManagerService', () => {
  let service: ManagerService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ManagerService],
    });
    service = TestBed.inject(ManagerService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('getAllPartners hace GET a la URL correcta', () => {
    service.getAllPartners().subscribe();
    http.expectOne(`${API_BASE}/personpartner/getall`).flush([]);
  });

  it('searchByIdentification hace GET a la URL correcta', () => {
    service.searchByIdentification('12345678').subscribe();
    http.expectOne(`${API_BASE}/personpartner/getbyidentification/12345678`).flush({});
  });

  it('getConsumptionsByIdentification hace GET a la URL paginada', () => {
    service.getConsumptionsByIdentification('123', { from: '2026-08-01T00:00', to: '2026-08-08T00:00', page: 0, size: 10 }).subscribe();
    const req = http.expectOne(
      `${API_BASE}/personpartner/getconsumptionsidentification/123?page=0&size=10&from=2026-08-01T00:00&to=2026-08-08T00:00`
    );
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0, number: 0, size: 10 });
  });

  it('getConsumptionsByEnvironment hace GET a la URL paginada', () => {
    service.getConsumptionsByEnvironment('Restaurante', { from: '2026-08-01T00:00', to: '2026-08-08T00:00', page: 1, size: 10 }).subscribe();
    http.expectOne(`${API_BASE}/partnerconsumption/by-environment/Restaurante?page=1&size=10&from=2026-08-01T00:00&to=2026-08-08T00:00`)
      .flush({ content: [], totalElements: 0, number: 1, size: 10 });
  });
});
