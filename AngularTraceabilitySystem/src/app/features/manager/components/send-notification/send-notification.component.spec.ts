import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SendNotificationComponent } from './send-notification.component';

/**
 * Prueba de humo del envío de notificaciones.
 *
 * @remarks
 * **Prueba un marcador de posición** para el que además **no existe endpoint en el backend**: completar esa pantalla exigiría
 * trabajo en el servidor.
 */
describe('SendNotificationComponent', () => {
  let component: SendNotificationComponent;
  let fixture: ComponentFixture<SendNotificationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
    imports: [SendNotificationComponent],
}).compileComponents();

    fixture = TestBed.createComponent(SendNotificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });
});
