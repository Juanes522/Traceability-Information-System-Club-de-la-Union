import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { ManagerService } from './manager.service';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ConsumptionListComponent } from './components/consumption-list/consumption-list.component';
import { SendNotificationComponent } from './components/send-notification/send-notification.component';
import { ConsumptionDetailComponent } from './components/consumption-detail/consumption-detail.component';
import { PartnerSearchComponent } from './components/partner-search/partner-search.component';
import { PartnerDetailComponent } from './components/partner-detail/partner-detail.component';
import { ManagerReportsComponent } from './components/reports/reports.component';

const routes: Routes = [
  { path: 'dashboard',      component: DashboardComponent },
  { path: 'partner-search', component: PartnerSearchComponent },
  { path: 'consumptions',   component: ConsumptionListComponent },
  { path: 'send-notification', component: SendNotificationComponent },
  { path: 'consumption/:id', component: ConsumptionDetailComponent },
  { path: 'reports',        component: ManagerReportsComponent },
  {
    path: 'my',
    loadChildren: () => import('../partner/partner.module').then(m => m.PartnerModule),
  },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
];

@NgModule({
    imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule.forChild(routes), DashboardComponent,
        ConsumptionListComponent,
        SendNotificationComponent,
        ConsumptionDetailComponent,
        PartnerSearchComponent,
        PartnerDetailComponent,
        ManagerReportsComponent],
    providers: [ManagerService],
})
/**
 * Módulo de la rama de gestor, cargado de forma diferida.
 *
 * @remarks
 * Aloja las rutas de la rama y registra su servicio con ámbito de módulo.
 *
 * **Particularidad poco evidente:** vuelve a montar de forma anidada el módulo de socio bajo una subruta propia. Esa rama no
 * tiene entrada de navegación, guarda propia ni pruebas, y como los endpoints del servicio de socio son todos de datos
 * propios, un gestor que llegue allí escribiendo la dirección verá **sus propios** datos de socio.
 *
 * Dos de sus rutas apuntan a componentes que son marcadores de posición sin funcionalidad.
 */
export class ManagerModule {}
