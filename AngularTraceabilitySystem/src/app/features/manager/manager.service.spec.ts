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

  it('registerConsumption hace POST a la URL correcta', () => {
    service.registerConsumption({ partnerId: 1, amount: 50, description: 'test' }).subscribe();
    http.expectOne(`${API_BASE}/partnerconsumption/registerconsumption`).flush({});
  });

  it('sendValidationNotification hace POST a la URL correcta', () => {
    service.sendValidationNotification(3).subscribe();
    http.expectOne(`${API_BASE}/partnerconsumption/sendvalidationnotification/3`).flush(null);
  });

  it('getConsumptions hace GET a la URL correcta', () => {
    service.getConsumptions(1).subscribe();
    http.expectOne(`${API_BASE}/partnerconsumption/getpartner/1`).flush([]);
  });

  it('getPartner hace GET a la URL correcta', () => {
    service.getPartner(2).subscribe();
    http.expectOne(`${API_BASE}/personpartner/getpartner/2`).flush({});
  });

  it('getConsumptionById hace GET a la URL correcta', () => {
    service.getConsumptionById(10).subscribe();
    http.expectOne(`${API_BASE}/partnerconsumption/getconsumptionbyid/10`).flush({});
  });
});
