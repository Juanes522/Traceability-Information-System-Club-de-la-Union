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

  it('getAllPartners hace GET a la URL correcta', () => {
    service.getAllPartners().subscribe();
    http.expectOne(`${API_BASE}/personpartner/getall`).flush([]);
  });

  it('searchByIdentification hace GET a la URL correcta', () => {
    service.searchByIdentification('12345678').subscribe();
    http.expectOne(`${API_BASE}/personpartner/getbyidentification/12345678`).flush({});
  });

  it('searchByShareNumber hace GET a la URL correcta', () => {
    service.searchByShareNumber('42').subscribe();
    http.expectOne(`${API_BASE}/personpartner/getbysharenumber/42`).flush([]);
  });

  it('getConsumptionsByEnvironment hace GET a la URL correcta', () => {
    service.getConsumptionsByEnvironment('Restaurante').subscribe();
    http.expectOne(`${API_BASE}/partnerconsumption/by-environment/Restaurante`).flush([]);
  });

  it('registerConsumption hace POST a la URL correcta', () => {
    const req: any = { partnerId: 1, enviroment: 'Bar', account: 5, table: '3', waiterName: 'Juan',
                       isPartner: 'S', consumptionValue: 50000, iva: 4750, service: 5000, tip: 0, consumptionOpening: '2026-05-12' };
    service.registerConsumption(req).subscribe();
    http.expectOne(`${API_BASE}/partnerconsumption/registerconsumption`).flush({});
  });
});
