import { ComponentFixture, TestBed, fakeAsync, tick, discardPeriodicTasks } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { AuditComponent } from './audit.component';
import { AuditService } from '../../audit.service';

describe('AuditComponent', () => {
  let component: AuditComponent;
  let fixture: ComponentFixture<AuditComponent>;
  let auditService: jasmine.SpyObj<AuditService>;

  beforeEach(async () => {
    auditService = jasmine.createSpyObj('AuditService', ['search']);
    auditService.search.and.returnValue(
      of({ content: [], totalElements: 0, number: 0, size: 20 }),
    );

    await TestBed.configureTestingModule({
      imports: [AuditComponent, FormsModule],
      providers: [{ provide: AuditService, useValue: auditService }],
    }).compileComponents();

    fixture = TestBed.createComponent(AuditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('carga eventos al inicializar', () => {
    expect(auditService.search).toHaveBeenCalled();
  });

  it('usa un tamaño de página de 10 filas', () => {
    expect(component.filters.size).toBe(10);
  });

  it('buscar() reenvía los filtros y resetea a la primera página', () => {
    component.filters.username = '123';
    component.search();
    const args = auditService.search.calls.mostRecent().args[0];
    expect(args.username).toBe('123');
    expect(args.page).toBe(0);
  });

  it('convierte from/to a ISO-8601 con zona antes de enviar', () => {
    component.filters.from = '2026-08-12T14:30';
    component.search();
    const args = auditService.search.calls.mostRecent().args[0];
    expect(args.from).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/);
  });

  it('marca estado de error cuando la consulta falla', () => {
    auditService.search.and.returnValue(throwError(() => new Error('down')));
    component.load();
    expect(component.error).toBeTrue();
    expect(component.events.length).toBe(0);
  });

  it('arranca con la ventana de última semana (from/to definidos)', () => {
    expect(component.filters.from).toBeTruthy();
    expect(component.filters.to).toBeTruthy();
  });

  it('un preset resetea a la primera página y define la ventana', () => {
    component.filters.page = 3;
    component.lastMonth();
    expect(component.filters.page).toBe(0);
    expect(component.filters.from).toBeTruthy();
  });

  it('goToPage cambia de página y vuelve a consultar el backend', () => {
    component.totalElements = 60;
    auditService.search.calls.reset();
    component.goToPage(2);
    expect(component.filters.page).toBe(1);
    expect(auditService.search).toHaveBeenCalled();
  });

  it('rechaza rango personalizado mayor a 3 meses sin consultar', () => {
    auditService.search.calls.reset();
    component.filters.from = '2026-01-01T00:00';
    component.filters.to = '2026-06-01T00:00';
    component.load();
    expect(component.rangeError).toBeTrue();
    expect(auditService.search).not.toHaveBeenCalled();
  });

  it('onTick refresca cuando la vista está en vivo y en la primera página', () => {
    component.lastWeek();
    component.autoRefresh = true;
    auditService.search.calls.reset();
    component.onTick();
    expect(auditService.search).toHaveBeenCalled();
  });

  it('onTick no refresca cuando no está en la primera página', () => {
    component.lastWeek();
    component.filters.page = 2;
    auditService.search.calls.reset();
    component.onTick();
    expect(auditService.search).not.toHaveBeenCalled();
  });

  it('onTick no refresca cuando el auto-refresco está apagado', () => {
    component.lastWeek();
    component.autoRefresh = false;
    auditService.search.calls.reset();
    component.onTick();
    expect(auditService.search).not.toHaveBeenCalled();
  });

  it('una búsqueda manual pausa el auto-refresco', () => {
    component.search();
    auditService.search.calls.reset();
    component.onTick();
    expect(auditService.search).not.toHaveBeenCalled();
  });

  it('registra la marca de última actualización tras cargar', () => {
    component.lastWeek();
    expect(component.lastUpdated).toBeTruthy();
  });

  it('auto-refresca periódicamente mientras está en vivo y se detiene al destruir', fakeAsync(() => {
    component.ngOnDestroy();
    component.ngOnInit();
    auditService.search.calls.reset();
    tick(component.REFRESH_MS);
    expect(auditService.search).toHaveBeenCalledTimes(1);
    component.ngOnDestroy();
    auditService.search.calls.reset();
    tick(component.REFRESH_MS);
    expect(auditService.search).not.toHaveBeenCalled();
    discardPeriodicTasks();
  }));
});
