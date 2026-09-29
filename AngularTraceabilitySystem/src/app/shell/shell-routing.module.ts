import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ShellComponent } from './shell.component';

import { roleGuard } from '../core/guards/role.guard';

const routes: Routes = [
  {
    path: '',
    component: ShellComponent,
    children: [
      {
        path: 'partner',
        canActivate: [roleGuard],
        data: { role: 'ROLE_PARTNER' },
        loadChildren: () => import('../features/partner/partner.module').then((m) => m.PartnerModule),
      },
      {
        path: 'manager',
        canActivate: [roleGuard],
        data: { role: 'ROLE_MANAGER' },
        loadChildren: () => import('../features/manager/manager.module').then((m) => m.ManagerModule),
      },
      {
        path: 'admin',
        canActivate: [roleGuard],
        data: { role: 'ROLE_ADMIN' },
        loadChildren: () => import('../features/admin/admin.module').then((m) => m.AdminModule),
      },
      // Default child redirect
      { path: '', redirectTo: 'partner', pathMatch: 'full' },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
/**
 * Rutas de la aplicación autenticada, cargadas de forma diferida por rol.
 *
 * @remarks
 * Monta el contenedor principal y cuelga de él las tres ramas de funcionalidad, cada una protegida por la guarda de rol y
 * cargada solo cuando se visita.
 *
 * **Defecto latente:** la ruta vacía redirige a la rama de socio, de modo que un gestor o un administrador que llegue a la
 * raíz de la aplicación acaba en la pantalla de acceso no autorizado. Rara vez ocurre porque el inicio de sesión navega
 * directamente al tablero del rol, pero sí puede alcanzarse al expirar la sesión.
 */
export class ShellRoutingModule {}
