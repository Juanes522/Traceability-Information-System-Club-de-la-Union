import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { PartnersComponent } from './partners.component';
import { AdminService } from '../../admin.service';

describe('PartnersComponent', () => {
  let component: PartnersComponent;
  let fixture: ComponentFixture<PartnersComponent>;

  beforeEach(async () => {
    const adminServiceSpy = jasmine.createSpyObj('AdminService', [
      'getAllPartners', 'searchByIdentification', 'searchByShareNumber',
      'searchByFirstName', 'searchBySecondName', 'getConsumptionsByIdentification',
    ]);
    adminServiceSpy.getAllPartners.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [PartnersComponent],
      providers: [
        { provide: AdminService, useValue: adminServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PartnersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });
});
