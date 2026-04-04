import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    authService = jasmine.createSpyObj('AuthService', ['login']);

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [LoginComponent],
      providers: [{ provide: AuthService, useValue: authService }],
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
    component.form.setValue({ email: 'user@test.com', password: '123456' });
    component.submit();
    expect(authService.login).toHaveBeenCalledWith({ email: 'user@test.com', password: '123456' });
  });

  it('asigna errorMsg cuando login falla', () => {
    authService.login.and.returnValue(throwError(() => ({ error: { message: 'Credenciales incorrectas' } })));
    component.form.setValue({ email: 'user@test.com', password: 'wrong' });
    component.submit();
    expect(component.errorMsg).toBe('Credenciales incorrectas');
  });
});
