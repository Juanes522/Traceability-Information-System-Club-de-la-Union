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

  it('getConsumptions hace GET a la URL correcta', () => {
    service.getConsumptions().subscribe();
    http.expectOne(`${API_BASE}/personpartner/getconsumptions/me`).flush([]);
  });
});
