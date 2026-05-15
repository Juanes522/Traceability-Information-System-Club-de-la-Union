import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { noAuthGuard } from './core/guards/no-auth.guard';
import { UnauthorizedComponent } from './shared/components/unauthorized/unauthorized.component';

export const routes: Routes = [
  // Redirect root → login
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },

  // Auth routes (lazy) — only for unauthenticated users
  {
    path: 'auth',
    canActivate: [noAuthGuard],
    loadChildren: () => import('./auth/auth.module').then((m) => m.AuthModule),
  },

  // App shell routes (lazy per role) — authenticated users only
  {
    path: 'app',
    canActivate: [authGuard],
    loadChildren: () => import('./shell/shell-routing.module').then((m) => m.ShellRoutingModule),
  },

  // Unauthorized page — accessible without authentication
  { path: 'unauthorized', component: UnauthorizedComponent },

  // Fallback
  { path: '**', redirectTo: 'auth/login' },
];
