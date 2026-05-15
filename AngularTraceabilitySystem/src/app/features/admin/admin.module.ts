import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { AdminService } from './admin.service';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { PartnersComponent } from './components/partners/partners.component';
import { ConsumptionsComponent } from './components/consumptions/consumptions.component';
import { AdminRegisterConsumptionComponent } from './components/register-consumption/register-consumption.component';

const routes: Routes = [
  { path: 'dashboard',  component: DashboardComponent },
  { path: 'partners',   component: PartnersComponent },
  { path: 'consumptions', component: ConsumptionsComponent },
  { path: 'register',   component: AdminRegisterConsumptionComponent },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
];

@NgModule({
    imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule.forChild(routes), DashboardComponent,
        PartnersComponent,
        ConsumptionsComponent,
        AdminRegisterConsumptionComponent],
    providers: [AdminService],
})
export class AdminModule {}
