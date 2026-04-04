import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { ChangePasswordComponent } from './change-password.component';
import { AuthApiService } from '../../../core/services/auth-api.service';
import { AuthService } from '../../../core/services/auth.service';

describe('ChangePasswordComponent', () => {
  let component: ChangePasswordComponent;
  let fixture: ComponentFixture<ChangePasswordComponent>;
  let authApiService: jasmine.SpyObj<AuthApiService>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authApiService = jasmine.createSpyObj('AuthApiService', ['changePassword']);
    authService = jasmine.createSpyObj('AuthService', ['clearNeedsPasswordChange'], {
      currentRole: 'ROLE_PARTNER',
    });

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, RouterTestingModule.withRoutes([])],
      declarations: [ChangePasswordComponent],
      providers: [
        { provide: AuthApiService, useValue: authApiService },
        { provide: AuthService, useValue: authService },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    fixture = TestBed.createComponent(ChangePasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });

  it('el formulario es inválido cuando está vacío', () => {
    expect(component.form.invalid).toBeTrue();
  });

  it('no llama a changePassword si el formulario es inválido', () => {
    component.submit();
    expect(authApiService.changePassword).not.toHaveBeenCalled();
  });

  it('llama a changePassword y clearNeedsPasswordChange al enviar con contraseña válida', () => {
    authApiService.changePassword.and.returnValue(of(undefined));
    component.form.setValue({ newPassword: 'nueva123' });
    component.submit();
    expect(authApiService.changePassword).toHaveBeenCalledWith({ newPassword: 'nueva123' });
    expect(authService.clearNeedsPasswordChange).toHaveBeenCalled();
  });

  it('asigna errorMsg cuando changePassword falla', () => {
    authApiService.changePassword.and.returnValue(
      throwError(() => ({ error: { message: 'Error de servidor' } }))
    );
    component.form.setValue({ newPassword: 'nueva123' });
    component.submit();
    expect(component.errorMsg).toBe('Error de servidor');
  });
});
