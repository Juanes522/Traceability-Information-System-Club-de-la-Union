import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ShellComponent } from './shell.component';
import { ShellModule } from './shell.module';
import { RoleGuard } from '../core/guards/role.guard';

const routes: Routes = [
  {
    path: '',
    component: ShellComponent,
    children: [
      {
        path: 'partner',
        canActivate: [RoleGuard],
        data: { role: 'ROLE_PARTNER' },
        loadChildren: () => import('../features/partner/partner.module').then((m) => m.PartnerModule),
      },
      {
        path: 'manager',
        canActivate: [RoleGuard],
        data: { role: 'ROLE_MANAGER' },
        loadChildren: () => import('../features/manager/manager.module').then((m) => m.ManagerModule),
      },
      {
        path: 'admin',
        canActivate: [RoleGuard],
        data: { role: 'ROLE_ADMIN' },
        loadChildren: () => import('../features/admin/admin.module').then((m) => m.AdminModule),
      },
      // Default child redirect
      { path: '', redirectTo: 'partner', pathMatch: 'full' },
    ],
  },
];

@NgModule({
  imports: [ShellModule, RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ShellRoutingModule {}
