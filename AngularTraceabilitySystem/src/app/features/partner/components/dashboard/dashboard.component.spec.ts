import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DashboardComponent } from './dashboard.component';

/**
 * Prueba de humo del tablero del socio: verifica que se crea.
 *
 * @remarks
 * Su alcance es limitado por construcción: el componente es un envoltorio de una línea, y toda la lógica vive en el panel de
 * métricas compartido, que **no tiene pruebas**.
 */
describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent, HttpClientTestingModule],
    }).compileComponents();
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    const http = TestBed.inject(HttpTestingController);
    http.match(() => true).forEach(r => r.flush({ summary: {}, byEnvironment: [], trend: [] }));
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });
});
