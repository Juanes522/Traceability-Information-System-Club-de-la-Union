import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { PartnerDetailComponent } from './partner-detail.component';
import { ManagerService } from '../../manager.service';
import { PartnerProfile } from '../../../../shared/models';

describe('Manager PartnerDetailComponent', () => {
  let component: PartnerDetailComponent;
  let fixture: ComponentFixture<PartnerDetailComponent>;
  let managerServiceSpy: jasmine.SpyObj<ManagerService>;

  const partner = {
    identification: '123',
    firstName: 'Ana',
    secondName: '',
    lastName: 'Lopez',
    email: [],
    phone: null,
    cellPhone: null,
    shareNumber: 1001,
    partnerState: true,
    ingressDate: '',
    birthDate: '',
  } as unknown as PartnerProfile;

  beforeEach(async () => {
    managerServiceSpy = jasmine.createSpyObj('ManagerService', ['getConsumptionsByIdentification']);
    managerServiceSpy.getConsumptionsByIdentification.and.returnValue(of({ content: [], totalElements: 0, number: 0, size: 10 }));

    await TestBed.configureTestingModule({
      imports: [PartnerDetailComponent],
      providers: [{ provide: ManagerService, useValue: managerServiceSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(PartnerDetailComponent);
    component = fixture.componentInstance;
    component.partner = partner;
    fixture.detectChanges();
  });

  it('se crea', () => {
    expect(component).toBeTruthy();
  });

  it('arranca en última semana y consulta con page 0', () => {
    expect(component.from).toBeTruthy();
    expect(component.to).toBeTruthy();
    const args = managerServiceSpy.getConsumptionsByIdentification.calls.mostRecent().args;
    expect(args[0]).toBe('123');
    expect(args[1].page).toBe(0);
  });

  it('goToPage reconsulta con la página (0-based) correcta', () => {
    managerServiceSpy.getConsumptionsByIdentification.and.returnValue(of({ content: [], totalElements: 45, number: 0, size: 10 }));
    component.loadConsumptions();
    managerServiceSpy.getConsumptionsByIdentification.calls.reset();
    component.goToPage(3);
    const args = managerServiceSpy.getConsumptionsByIdentification.calls.mostRecent().args;
    expect(args[1].page).toBe(2);
  });

  it('editar la fecha manualmente vuelve a la página 1', () => {
    managerServiceSpy.getConsumptionsByIdentification.and.returnValue(of({ content: [], totalElements: 45, number: 0, size: 10 }));
    component.loadConsumptions();
    component.goToPage(3);
    expect(component.currentPage).toBe(3);
    managerServiceSpy.getConsumptionsByIdentification.calls.reset();
    component.onWindowEdit();
    expect(component.currentPage).toBe(1);
    const args = managerServiceSpy.getConsumptionsByIdentification.calls.mostRecent().args;
    expect(args[1].page).toBe(0);
  });

  it('marca consError cuando la consulta falla', () => {
    managerServiceSpy.getConsumptionsByIdentification.and.returnValue(throwError(() => new Error('x')));
    component.loadConsumptions();
    expect(component.consError).toBeTrue();
  });

  it('rechaza rango mayor a 3 meses sin consultar', () => {
    component.from = '2026-01-01T00:00';
    component.to = '2026-06-01T00:00';
    managerServiceSpy.getConsumptionsByIdentification.calls.reset();
    component.loadConsumptions();
    expect(component.rangeError).toBeTrue();
    expect(managerServiceSpy.getConsumptionsByIdentification).not.toHaveBeenCalled();
  });
});
