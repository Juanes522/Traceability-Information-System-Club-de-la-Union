import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AdminService } from './admin.service';
import { API_BASE } from '../../core/config/api.config';

describe('AdminService', () => {
  let service: AdminService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AdminService],
    });
    service = TestBed.inject(AdminService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('getAllPartners hace GET a la URL paginada', () => {
    service.getAllPartners(2, 10).subscribe();
    http.expectOne(`${API_BASE}/personpartner/getallpaged?page=2&size=10`)
      .flush({ content: [], totalElements: 0, number: 2, size: 10 });
  });

  it('searchByIdentification hace GET a la URL correcta', () => {
    service.searchByIdentification('12345678').subscribe();
    http.expectOne(`${API_BASE}/personpartner/getbyidentification/12345678`).flush({});
  });

  it('searchByShareNumber hace GET a la URL correcta', () => {
    service.searchByShareNumber('42').subscribe();
    http.expectOne(`${API_BASE}/personpartner/getbysharenumber/42`).flush([]);
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

  it('registerConsumption hace POST a la URL correcta', () => {
    const req: any = { partnerId: 1, enviroment: 'Bar', account: 5, table: '3', waiterName: 'Juan',
                       isPartner: 'S', consumptionValue: 50000, iva: 4750, service: 5000, tip: 0, consumptionOpening: '2026-05-12' };
    service.registerConsumption(req).subscribe();
    http.expectOne(`${API_BASE}/partnerconsumption/registerconsumption`).flush({});
  });
});
