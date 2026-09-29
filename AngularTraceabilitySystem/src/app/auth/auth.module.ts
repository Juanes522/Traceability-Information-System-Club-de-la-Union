import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { ChangePasswordComponent } from './components/change-password/change-password.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './components/reset-password/reset-password.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'change-password', component: ChangePasswordComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
];

@NgModule({
    imports: [CommonModule, ReactiveFormsModule, RouterModule.forChild(routes), LoginComponent, ChangePasswordComponent, ForgotPasswordComponent, ResetPasswordComponent],
})
/**
 * Módulo de las pantallas de autenticación, cargado de forma diferida.
 *
 * @remarks
 * Existe únicamente para alojar las rutas de esta rama: todos sus componentes son independientes, de modo que su lista de
 * importaciones es inerte y las rutas los referencian directamente.
 *
 * Nótese que **todas** sus rutas quedan tras la guarda que expulsa a los usuarios con sesión, lo que vuelve inalcanzable la
 * pantalla de cambio de contraseña, pensada precisamente para ellos.
 */
export class AuthModule {}
