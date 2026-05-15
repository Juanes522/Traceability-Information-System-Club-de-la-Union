import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterConsumptionComponent } from './register-consumption.component';
import { ManagerService } from '../../manager.service';
import { ToastService } from '../../../../core/services/toast.service';

describe('RegisterConsumptionComponent', () => {
  let component: RegisterConsumptionComponent;
  let fixture: ComponentFixture<RegisterConsumptionComponent>;

  beforeEach(async () => {
    const managerServiceSpy = jasmine.createSpyObj('ManagerService', [
      'searchByIdentification', 'registerConsumption',
    ]);
    const toastServiceSpy = jasmine.createSpyObj('ToastService', ['success', 'error']);

    await TestBed.configureTestingModule({
      imports: [RegisterConsumptionComponent],
      providers: [
        { provide: ManagerService, useValue: managerServiceSpy },
        { provide: ToastService, useValue: toastServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterConsumptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });
});
