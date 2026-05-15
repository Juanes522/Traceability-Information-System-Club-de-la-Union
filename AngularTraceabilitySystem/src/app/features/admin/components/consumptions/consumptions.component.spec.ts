import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConsumptionsComponent } from './consumptions.component';
import { AdminService } from '../../admin.service';

describe('ConsumptionsComponent', () => {
  let component: ConsumptionsComponent;
  let fixture: ComponentFixture<ConsumptionsComponent>;

  beforeEach(async () => {
    const adminServiceSpy = jasmine.createSpyObj('AdminService', ['getConsumptionsByEnvironment']);

    await TestBed.configureTestingModule({
      imports: [ConsumptionsComponent],
      providers: [
        { provide: AdminService, useValue: adminServiceSpy },
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
