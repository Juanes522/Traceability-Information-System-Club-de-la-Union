import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { NoAuthGuard } from './core/guards/no-auth.guard';
import { PasswordChangeGuard } from './core/guards/password-change.guard';
import { RoleGuard } from './core/guards/role.guard';
import { UnauthorizedComponent } from './shared/components/unauthorized/unauthorized.component';

const routes: Routes = [
  // Redirect root → login
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },

  // Auth routes (lazy) — only for unauthenticated users
  {
    path: 'auth',
    canActivate: [NoAuthGuard],
    loadChildren: () => import('./auth/auth.module').then((m) => m.AuthModule),
  },

  // App shell routes (lazy per role) — authenticated users only
  // PasswordChangeGuard removed: the modal in ShellComponent handles forced password change
  {
    path: 'app',
    canActivate: [AuthGuard],
    loadChildren: () => import('./shell/shell-routing.module').then((m) => m.ShellRoutingModule),
  },

  // Unauthorized page — accessible without authentication
  { path: 'unauthorized', component: UnauthorizedComponent },

  // Fallback
  { path: '**', redirectTo: 'auth/login' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
