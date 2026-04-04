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
    http.expectOne(`${API_BASE}/personpartner/getallpartners`).flush([]);
  });

  it('createPartner hace POST a la URL correcta', () => {
    service.createPartner({ name: 'Test' } as any).subscribe();
    http.expectOne(`${API_BASE}/personpartner/createpartner`).flush({});
  });

  it('updatePartner hace PUT a la URL correcta', () => {
    service.updatePartner(1, { name: 'Updated' } as any).subscribe();
    http.expectOne(`${API_BASE}/personpartner/updatepartner/1`).flush({});
  });

  it('deletePartner hace DELETE a la URL correcta', () => {
    service.deletePartner(2).subscribe();
    http.expectOne(`${API_BASE}/personpartner/deletepartner/2`).flush(null);
  });

  it('getAllConsumptions hace GET a la URL correcta', () => {
    service.getAllConsumptions().subscribe();
    http.expectOne(`${API_BASE}/partnerconsumption/getallconsumptions`).flush([]);
  });
});
