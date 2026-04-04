import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { ManagerService } from './manager.service';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { RegisterConsumptionComponent } from './components/register-consumption/register-consumption.component';
import { ConsumptionListComponent } from './components/consumption-list/consumption-list.component';
import { SendNotificationComponent } from './components/send-notification/send-notification.component';
import { ConsumptionDetailComponent } from './components/consumption-detail/consumption-detail.component';
import { PartnerSearchComponent } from './components/partner-search/partner-search.component';
import { PartnerDetailComponent } from './components/partner-detail/partner-detail.component';

const routes: Routes = [
  { path: 'dashboard',      component: DashboardComponent },
  { path: 'partner-search', component: PartnerSearchComponent },
  { path: 'register',       component: RegisterConsumptionComponent },
  { path: 'consumptions',   component: ConsumptionListComponent },
  { path: 'send-notification', component: SendNotificationComponent },
  { path: 'consumption/:id', component: ConsumptionDetailComponent },
  // Personal profile — reuses PartnerModule routes (/profile, /consumptions, /dependents, /notifications)
  {
    path: 'my',
    loadChildren: () => import('../partner/partner.module').then(m => m.PartnerModule),
  },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
];

@NgModule({
  declarations: [
    DashboardComponent,
    RegisterConsumptionComponent,
    ConsumptionListComponent,
    SendNotificationComponent,
    ConsumptionDetailComponent,
    PartnerSearchComponent,
    PartnerDetailComponent,
  ],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule.forChild(routes)],
  providers: [ManagerService],
})
export class ManagerModule {}
