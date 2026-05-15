import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ConsumptionsComponent } from './consumptions.component';
import { PartnerService } from '../../partner.service';

describe('ConsumptionsComponent', () => {
  let component: ConsumptionsComponent;
  let fixture: ComponentFixture<ConsumptionsComponent>;

  beforeEach(async () => {
    const partnerServiceSpy = jasmine.createSpyObj('PartnerService', ['getConsumptions']);
    partnerServiceSpy.getConsumptions.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [ConsumptionsComponent],
      providers: [
        { provide: PartnerService, useValue: partnerServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ConsumptionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });
});
