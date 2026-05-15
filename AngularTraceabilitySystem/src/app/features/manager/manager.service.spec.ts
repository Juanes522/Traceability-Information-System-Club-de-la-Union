import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ManagerService, ConsumptionCreateRequest } from './manager.service';
import { API_BASE } from '../../core/config/api.config';

const MOCK_REQ: ConsumptionCreateRequest = {
  partnerId: 1,
  enviroment: 'Restaurante',
  account: 5,
  table: '3',
  waiterName: 'Carlos',
  isPartner: 'S',
  consumptionValue: 50000,
  iva: 4750,
  service: 5000,
  tip: 0,
  consumptionOpening: '2026-05-12',
};

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

  it('registerConsumption hace POST a la URL correcta', () => {
    service.registerConsumption(MOCK_REQ).subscribe();
    http.expectOne(`${API_BASE}/partnerconsumption/registerconsumption`).flush({});
  });

  it('getAllPartners hace GET a la URL correcta', () => {
    service.getAllPartners().subscribe();
    http.expectOne(`${API_BASE}/personpartner/getall`).flush([]);
  });

  it('searchByIdentification hace GET a la URL correcta', () => {
    service.searchByIdentification('12345678').subscribe();
    http.expectOne(`${API_BASE}/personpartner/getbyidentification/12345678`).flush({});
  });

  it('getConsumptionsByIdentification hace GET a la URL correcta', () => {
    service.getConsumptionsByIdentification('12345678').subscribe();
    http.expectOne(`${API_BASE}/personpartner/getconsumptionsidentification/12345678`).flush([]);
  });

  it('getConsumptionsByEnvironment hace GET a la URL correcta', () => {
    service.getConsumptionsByEnvironment('Bar').subscribe();
    http.expectOne(`${API_BASE}/partnerconsumption/by-environment/Bar`).flush([]);
  });
});
