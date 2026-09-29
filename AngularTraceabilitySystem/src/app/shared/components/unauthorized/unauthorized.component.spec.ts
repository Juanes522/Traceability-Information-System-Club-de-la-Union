import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { UnauthorizedComponent } from './unauthorized.component';

/**
 * Verifica la pantalla de acceso no autorizado.
 *
 * @remarks
 * Fija que su botón navega al inicio de sesión, lo que **documenta la incoherencia** entre el texto del botón —que invita a
 * volver al inicio— y su destino real.
 */
describe('UnauthorizedComponent', () => {
  let component: UnauthorizedComponent;
  let fixture: ComponentFixture<UnauthorizedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
    imports: [RouterTestingModule, UnauthorizedComponent],
}).compileComponents();

    fixture = TestBed.createComponent(UnauthorizedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });

  it('navega a /auth/login al llamar goBack()', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    component.goBack();
    expect(router.navigate).toHaveBeenCalledWith(['/auth/login']);
  });
});
