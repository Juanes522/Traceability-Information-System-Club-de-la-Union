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
  declarations: [
    DashboardComponent,
    ProfileComponent,
    ConsumptionsComponent,
    NotificationsComponent,
    AccessLogComponent,
  ],
  imports: [CommonModule, FormsModule, RouterModule.forChild(routes)],
  providers: [PartnerService],
})
export class PartnerModule {}
