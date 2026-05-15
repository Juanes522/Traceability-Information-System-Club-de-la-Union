import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConsumptionListComponent } from './consumption-list.component';
import { ManagerService } from '../../manager.service';

describe('ConsumptionListComponent', () => {
  let component: ConsumptionListComponent;
  let fixture: ComponentFixture<ConsumptionListComponent>;

  beforeEach(async () => {
    const managerServiceSpy = jasmine.createSpyObj('ManagerService', [
      'getConsumptionsByEnvironment',
    ]);

    await TestBed.configureTestingModule({
      imports: [ConsumptionListComponent],
      providers: [
        { provide: ManagerService, useValue: managerServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ConsumptionListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });
});
