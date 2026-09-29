import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SystemUsersComponent } from './system-users.component';

/**
 * Prueba de humo del componente de usuarios del sistema.
 *
 * @remarks
 * **Prueba un marcador de posición completamente desconectado:** ese componente no tiene ruta, no tiene entrada de navegación
 * y ni siquiera figura en las importaciones de su módulo.
 */
describe('SystemUsersComponent', () => {
  let component: SystemUsersComponent;
  let fixture: ComponentFixture<SystemUsersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SystemUsersComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SystemUsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });
});
