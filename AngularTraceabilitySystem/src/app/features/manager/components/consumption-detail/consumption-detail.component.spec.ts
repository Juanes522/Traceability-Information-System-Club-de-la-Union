import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConsumptionDetailComponent } from './consumption-detail.component';

/**
 * Prueba de humo del detalle de consumo.
 *
 * @remarks
 * **Prueba un marcador de posición:** ese componente está enrutado con un identificador que nunca lee y anuncia que el módulo
 * está en construcción.
 */
describe('ConsumptionDetailComponent', () => {
  let component: ConsumptionDetailComponent;
  let fixture: ComponentFixture<ConsumptionDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
    imports: [ConsumptionDetailComponent],
}).compileComponents();

    fixture = TestBed.createComponent(ConsumptionDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });
});
