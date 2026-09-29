import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { PartnerService } from './partner.service';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ProfileComponent } from './components/profile/profile.component';
import { ConsumptionsComponent } from './components/consumptions/consumptions.component';
import { NotificationsComponent } from './components/notifications/notifications.component';
import { AccessLogComponent } from './components/access-log/access-log.component';

const routes: Routes = [
  { path: 'dashboard', component: DashboardComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'consumptions', component: ConsumptionsComponent },
  { path: 'notifications', component: NotificationsComponent },
  { path: 'access-log', component: AccessLogComponent },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
];

@NgModule({
    imports: [CommonModule, FormsModule, RouterModule.forChild(routes), DashboardComponent,
        ProfileComponent,
        ConsumptionsComponent,
        NotificationsComponent,
        AccessLogComponent],
    providers: [PartnerService],
})
/**
 * Módulo de la rama de socio, cargado de forma diferida.
 *
 * @remarks
 * Aloja las rutas de la rama y registra su servicio con ámbito de módulo. Todos los componentes son independientes, de modo
 * que su lista de importaciones es inerte.
 *
 * Nótese que el componente de dependientes **no está enrutado**, y que este mismo módulo se vuelve a montar de forma anidada
 * bajo la rama de gestor, en una ruta sin entrada de navegación.
 */
export class PartnerModule {}
