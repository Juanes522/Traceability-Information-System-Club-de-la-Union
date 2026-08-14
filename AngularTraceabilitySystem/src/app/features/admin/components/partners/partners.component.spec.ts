import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { PartnersComponent } from './partners.component';
import { AdminService } from '../../admin.service';
import { PartnerProfile } from '../../../../shared/models';

describe('PartnersComponent', () => {
  let component: PartnersComponent;
  let fixture: ComponentFixture<PartnersComponent>;
  let adminServiceSpy: jasmine.SpyObj<AdminService>;

  beforeEach(async () => {
    adminServiceSpy = jasmine.createSpyObj('AdminService', [
      'getAllPartners', 'searchByIdentification', 'searchByShareNumber',
      'searchByFirstName', 'searchBySecondName', 'getConsumptionsByIdentification',
    ]);
    adminServiceSpy.getAllPartners.and.returnValue(of({ content: [], totalElements: 0, number: 0, size: 10 }));
    adminServiceSpy.getConsumptionsByIdentification.and.returnValue(of({ content: [], totalElements: 0, number: 0, size: 10 }));

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

  it('loadAll muestra error y limpia resultados si la consulta falla', () => {
    adminServiceSpy.getAllPartners.and.returnValue(throwError(() => new Error('down')));
    component.loadAll();
    expect(component.error).toBe('Error al cargar socios.');
    expect(component.results.length).toBe(0);
  });

  it('resultCount usa totalElements en modo todos', () => {
    adminServiceSpy.getAllPartners.and.returnValue(of({ content: [], totalElements: 45, number: 0, size: 10 }));
    component.loadAll();
    expect(component.resultCount).toBe(45);
  });

  it('loadAll usa paginación server-side y guarda totalElements', () => {
    adminServiceSpy.getAllPartners.and.returnValue(of({ content: [{} as PartnerProfile], totalElements: 45, number: 0, size: 10 }));
    component.loadAll();
    expect(component.mode).toBe('all');
    expect(component.totalElements).toBe(45);
    expect(adminServiceSpy.getAllPartners).toHaveBeenCalledWith(0, 10);
  });

  it('goToPage en modo "todos" reconsulta el backend con la página', () => {
    adminServiceSpy.getAllPartners.and.returnValue(of({ content: [], totalElements: 45, number: 0, size: 10 }));
    component.loadAll();
    adminServiceSpy.getAllPartners.calls.reset();
    component.goToPage(3);
    expect(adminServiceSpy.getAllPartners).toHaveBeenCalledWith(2, 10);
  });

  it('en modo búsqueda la paginación es client-side (no reconsulta)', () => {
    adminServiceSpy.searchByFirstName.and.returnValue(of(Array.from({ length: 25 }, () => ({} as PartnerProfile))));
    component.searchField = 'firstName';
    component.searchValue = 'Carlos';
    component.search();
    adminServiceSpy.getAllPartners.calls.reset();
    component.goToPage(2);
    expect(adminServiceSpy.getAllPartners).not.toHaveBeenCalled();
    expect(component.mode).toBe('search');
  });

  it('selectPartner arranca consumos en última semana con page 0', () => {
    adminServiceSpy.getConsumptionsByIdentification.calls.reset();
    component.selectPartner({ identification: '123' } as PartnerProfile);
    expect(component.consFrom).toBeTruthy();
    expect(component.consTo).toBeTruthy();
    const args = adminServiceSpy.getConsumptionsByIdentification.calls.mostRecent().args;
    expect(args[0]).toBe('123');
    expect(args[1].page).toBe(0);
  });

  it('consGoToPage reconsulta con page 0-based correcto', () => {
    component.selectedPartner = { identification: '123' } as PartnerProfile;
    adminServiceSpy.getConsumptionsByIdentification.and.returnValue(of({ content: [], totalElements: 45, number: 0, size: 10 }));
    component.loadConsumptions();
    adminServiceSpy.getConsumptionsByIdentification.calls.reset();
    component.consGoToPage(3);
    const args = adminServiceSpy.getConsumptionsByIdentification.calls.mostRecent().args;
    expect(args[1].page).toBe(2);
  });

  it('editar la fecha manualmente vuelve a la página 1', () => {
    component.selectedPartner = { identification: '123' } as PartnerProfile;
    adminServiceSpy.getConsumptionsByIdentification.and.returnValue(of({ content: [], totalElements: 45, number: 0, size: 10 }));
    component.loadConsumptions();
    component.consGoToPage(3);
    expect(component.consCurrentPage).toBe(3);
    adminServiceSpy.getConsumptionsByIdentification.calls.reset();
    component.onConsWindowEdit();
    expect(component.consCurrentPage).toBe(1);
    const args = adminServiceSpy.getConsumptionsByIdentification.calls.mostRecent().args;
    expect(args[1].page).toBe(0);
  });

  it('marca consError cuando la consulta de consumos falla', () => {
    component.selectedPartner = { identification: '123' } as PartnerProfile;
    adminServiceSpy.getConsumptionsByIdentification.and.returnValue(throwError(() => new Error('x')));
    component.loadConsumptions();
    expect(component.consError).toBeTrue();
  });

  it('rechaza rango de consumos mayor a 3 meses sin consultar', () => {
    component.selectedPartner = { identification: '123' } as PartnerProfile;
    component.consFrom = '2026-01-01T00:00';
    component.consTo = '2026-06-01T00:00';
    adminServiceSpy.getConsumptionsByIdentification.calls.reset();
    component.loadConsumptions();
    expect(component.consRangeError).toBeTrue();
    expect(adminServiceSpy.getConsumptionsByIdentification).not.toHaveBeenCalled();
  });
});
