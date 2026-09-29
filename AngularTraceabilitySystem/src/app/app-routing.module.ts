import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { noAuthGuard } from './core/guards/no-auth.guard';
import { UnauthorizedComponent } from './shared/components/unauthorized/unauthorized.component';

const routes: Routes = [
  // Redirect root → login
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },

  // Auth routes (lazy) — only for unauthenticated users
  {
    path: 'auth',
    canActivate: [noAuthGuard],
    loadChildren: () => import('./auth/auth.module').then((m) => m.AuthModule),
  },

  // App shell routes (lazy per role) — authenticated users only
  // PasswordChangeGuard removed: the modal in ShellComponent handles forced password change
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

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
/**
 * Módulo de enrutamiento raíz. **Código muerto.**
 *
 * @remarks
 * Duplica literalmente la tabla de rutas que la aplicación sí usa, envuelta en la forma antigua basada en módulos. Una
 * búsqueda en el proyecto no encuentra ninguna referencia a este módulo fuera de su propio archivo.
 *
 * Se documenta en lugar de ignorarse porque es una **trampa de mantenimiento**: quien edite las rutas aquí no verá ningún
 * efecto, y quien las edite en el archivo vivo dejará esta copia divergiendo en silencio.
 */
export class AppRoutingModule {}
