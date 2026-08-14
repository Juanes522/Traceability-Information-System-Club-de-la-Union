import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ConsumptionsComponent } from './consumptions.component';
import { AdminService } from '../../admin.service';

describe('ConsumptionsComponent', () => {
  let component: ConsumptionsComponent;
  let fixture: ComponentFixture<ConsumptionsComponent>;
  let adminServiceSpy: jasmine.SpyObj<AdminService>;

  beforeEach(async () => {
    adminServiceSpy = jasmine.createSpyObj('AdminService', ['getConsumptionsByEnvironment']);
    adminServiceSpy.getConsumptionsByEnvironment.and.returnValue(of({ content: [], totalElements: 0, number: 0, size: 10 }));

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

  it('un preset con ambiente definido reconsulta en página 0', () => {
    component.environment = 'Bar';
    adminServiceSpy.getConsumptionsByEnvironment.calls.reset();
    component.lastMonth();
    expect(component.currentPage).toBe(1);
    expect(adminServiceSpy.getConsumptionsByEnvironment).toHaveBeenCalled();
    const args = adminServiceSpy.getConsumptionsByEnvironment.calls.mostRecent().args;
    expect(args[1].page).toBe(0);
  });

  it('goToPage reconsulta el backend con la página', () => {
    component.environment = 'Bar';
    component.totalElements = 45;
    adminServiceSpy.getConsumptionsByEnvironment.and.returnValue(of({ content: [], totalElements: 45, number: 0, size: 10 }));
    component.search();
    adminServiceSpy.getConsumptionsByEnvironment.calls.reset();
    component.goToPage(3);
    const args = adminServiceSpy.getConsumptionsByEnvironment.calls.mostRecent().args;
    expect(args[1].page).toBe(2);
  });

  it('rechaza rango mayor a 3 meses sin consultar', () => {
    component.environment = 'Bar';
    component.from = '2026-01-01T00:00';
    component.to = '2026-06-01T00:00';
    adminServiceSpy.getConsumptionsByEnvironment.calls.reset();
    component.search();
    expect(component.rangeError).toBeTrue();
    expect(adminServiceSpy.getConsumptionsByEnvironment).not.toHaveBeenCalled();
  });
});
