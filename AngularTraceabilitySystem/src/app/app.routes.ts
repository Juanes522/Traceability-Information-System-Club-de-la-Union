import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { noAuthGuard } from './core/guards/no-auth.guard';
import { UnauthorizedComponent } from './shared/components/unauthorized/unauthorized.component';

/**
 * Tabla de rutas raíz de la aplicación.
 *
 * @remarks
 * Divide la aplicación en dos ramas protegidas por guardas inversas: la de autenticación, vedada a quien ya tiene sesión, y
 * la del contenedor principal, vedada a quien no la tiene. Ambas se cargan de forma diferida.
 *
 * Cualquier ruta desconocida redirige al inicio de sesión en lugar de mostrar un error.
 *
 * **Esta es la tabla que la aplicación usa realmente.** Existe otro archivo con una copia literal de estas rutas envuelta en
 * un módulo de enrutamiento, al que no hace referencia nadie.
 */
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
