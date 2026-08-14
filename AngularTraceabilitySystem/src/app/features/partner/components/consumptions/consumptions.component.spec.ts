import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ConsumptionsComponent } from './consumptions.component';
import { PartnerService } from '../../partner.service';

describe('Partner ConsumptionsComponent', () => {
  let component: ConsumptionsComponent;
  let fixture: ComponentFixture<ConsumptionsComponent>;
  let partnerServiceSpy: jasmine.SpyObj<PartnerService>;

  beforeEach(async () => {
    partnerServiceSpy = jasmine.createSpyObj('PartnerService', ['getConsumptions']);
    partnerServiceSpy.getConsumptions.and.returnValue(of({ content: [], totalElements: 0, number: 0, size: 10 }));

    await TestBed.configureTestingModule({
      imports: [ConsumptionsComponent],
      providers: [{ provide: PartnerService, useValue: partnerServiceSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(ConsumptionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('se crea', () => {
    expect(component).toBeTruthy();
  });

  it('arranca con la ventana de última semana', () => {
    expect(component.from).toBeTruthy();
    expect(component.to).toBeTruthy();
  });

  it('goToPage reconsulta con la página (0-based) correcta', () => {
    partnerServiceSpy.getConsumptions.and.returnValue(of({ content: [], totalElements: 45, number: 0, size: 10 }));
    component.load();
    partnerServiceSpy.getConsumptions.calls.reset();
    component.goToPage(3);
    const args = partnerServiceSpy.getConsumptions.calls.mostRecent().args[0];
    expect(args.page).toBe(2);
  });

  it('editar la fecha manualmente vuelve a la página 1', () => {
    partnerServiceSpy.getConsumptions.and.returnValue(of({ content: [], totalElements: 45, number: 0, size: 10 }));
    component.load();
    component.goToPage(3);
    expect(component.currentPage).toBe(3);
    partnerServiceSpy.getConsumptions.calls.reset();
    component.onWindowEdit();
    expect(component.currentPage).toBe(1);
    const args = partnerServiceSpy.getConsumptions.calls.mostRecent().args[0];
    expect(args.page).toBe(0);
  });

  it('rechaza rango mayor a 3 meses sin consultar', () => {
    component.from = '2026-01-01T00:00';
    component.to = '2026-06-01T00:00';
    partnerServiceSpy.getConsumptions.calls.reset();
    component.load();
    expect(component.rangeError).toBeTrue();
    expect(partnerServiceSpy.getConsumptions).not.toHaveBeenCalled();
  });
});
