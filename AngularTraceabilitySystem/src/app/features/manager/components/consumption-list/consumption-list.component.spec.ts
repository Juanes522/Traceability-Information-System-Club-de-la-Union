import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ConsumptionListComponent } from './consumption-list.component';
import { ManagerService } from '../../manager.service';

describe('ConsumptionListComponent', () => {
  let component: ConsumptionListComponent;
  let fixture: ComponentFixture<ConsumptionListComponent>;
  let managerServiceSpy: jasmine.SpyObj<ManagerService>;

  beforeEach(async () => {
    managerServiceSpy = jasmine.createSpyObj('ManagerService', [
      'getConsumptionsByEnvironment',
    ]);
    managerServiceSpy.getConsumptionsByEnvironment.and.returnValue(of({ content: [], totalElements: 0, number: 0, size: 10 }));

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

  it('un preset con ambiente definido reconsulta en página 0', () => {
    component.environment = 'Bar';
    managerServiceSpy.getConsumptionsByEnvironment.calls.reset();
    component.lastMonth();
    expect(component.currentPage).toBe(1);
    const args = managerServiceSpy.getConsumptionsByEnvironment.calls.mostRecent().args;
    expect(args[1].page).toBe(0);
  });

  it('goToPage reconsulta el backend con la página', () => {
    component.environment = 'Bar';
    component.totalElements = 45;
    managerServiceSpy.getConsumptionsByEnvironment.and.returnValue(of({ content: [], totalElements: 45, number: 0, size: 10 }));
    component.search();
    managerServiceSpy.getConsumptionsByEnvironment.calls.reset();
    component.goToPage(3);
    const args = managerServiceSpy.getConsumptionsByEnvironment.calls.mostRecent().args;
    expect(args[1].page).toBe(2);
  });

  it('rechaza rango mayor a 3 meses sin consultar', () => {
    component.environment = 'Bar';
    component.from = '2026-01-01T00:00';
    component.to = '2026-06-01T00:00';
    managerServiceSpy.getConsumptionsByEnvironment.calls.reset();
    component.search();
    expect(component.rangeError).toBeTrue();
    expect(managerServiceSpy.getConsumptionsByEnvironment).not.toHaveBeenCalled();
  });
});
