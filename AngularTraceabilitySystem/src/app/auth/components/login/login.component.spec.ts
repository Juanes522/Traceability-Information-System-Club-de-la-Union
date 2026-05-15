import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let toastService: jasmine.SpyObj<ToastService>;

  beforeEach(async () => {
    authService  = jasmine.createSpyObj('AuthService', ['login']);
    toastService = jasmine.createSpyObj('ToastService', ['success', 'error']);

    await TestBed.configureTestingModule({
    imports: [ReactiveFormsModule, LoginComponent],
    providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService },
        { provide: ToastService, useValue: toastService },
    ],
}).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });

  it('el formulario es inválido cuando está vacío', () => {
    expect(component.form.invalid).toBeTrue();
  });

  it('no llama a login si el formulario es inválido', () => {
    component.submit();
    expect(authService.login).not.toHaveBeenCalled();
  });

  it('llama a authService.login con credenciales válidas', () => {
    authService.login.and.returnValue(of(undefined));
    component.form.setValue({ identification: '12345678', password: '123456' });
    component.submit();
    expect(authService.login).toHaveBeenCalledWith({ identification: '12345678', password: '123456' });
  });

  it('muestra toast de error cuando login falla', () => {
    authService.login.and.returnValue(throwError(() => ({ error: { message: 'Credenciales incorrectas' } })));
    component.form.setValue({ identification: '12345678', password: 'wrong' });
    component.submit();
    expect(toastService.error).toHaveBeenCalledWith('Credenciales incorrectas');
  });
});
