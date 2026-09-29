import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DependentsComponent } from './dependents.component';

/**
 * Prueba de humo del componente de dependientes.
 *
 * @remarks
 * **Verifica que se puede instanciar un componente cuya plantilla está vacía**, que no tiene ruta ni entrada de navegación.
 * Es el ejemplo más claro de cómo las pruebas de humo sobre elementos inertes inflan las cifras de cobertura sin cubrir nada.
 */
describe('DependentsComponent', () => {
  let component: DependentsComponent;
  let fixture: ComponentFixture<DependentsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DependentsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DependentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });
});
