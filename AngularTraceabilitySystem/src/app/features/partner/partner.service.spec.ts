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
    service.getProfile(1).subscribe();
    http.expectOne(`${API_BASE}/personpartner/getpartner/1`).flush({});
  });

  it('getConsumptions hace GET a la URL correcta', () => {
    service.getConsumptions(1).subscribe();
    http.expectOne(`${API_BASE}/partnerconsumption/getpartner/1`).flush([]);
  });

  it('getNotifications hace GET a la URL correcta', () => {
    service.getNotifications(1).subscribe();
    http.expectOne(`${API_BASE}/partnerconsumption/getnotifications/1`).flush([]);
  });

  it('respondValidation hace POST a la URL correcta', () => {
    service.respondValidation(5, true).subscribe();
    http.expectOne(`${API_BASE}/partnerconsumption/respondvalidation/5/true`).flush(null);
  });

  it('getAccessLog hace GET a la URL correcta', () => {
    service.getAccessLog(1).subscribe();
    http.expectOne(`${API_BASE}/personpartner/getaccesslog/1`).flush([]);
  });

  it('getDependents hace GET a la URL correcta', () => {
    service.getDependents(1).subscribe();
    http.expectOne(`${API_BASE}/personpartner/getdependents/1`).flush([]);
  });
});
