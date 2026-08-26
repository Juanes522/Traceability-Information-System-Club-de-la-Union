import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { AdminService } from './admin.service';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { PartnersComponent } from './components/partners/partners.component';
import { ConsumptionsComponent } from './components/consumptions/consumptions.component';
import { AuditComponent } from './components/audit/audit.component';
import { AuditService } from './audit.service';
import { AdminReportsComponent } from './components/reports/reports.component';

const routes: Routes = [
  { path: 'dashboard',  component: DashboardComponent },
  { path: 'partners',   component: PartnersComponent },
  { path: 'consumptions', component: ConsumptionsComponent },
  { path: 'audit',      component: AuditComponent },
  { path: 'reports',    component: AdminReportsComponent },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
];

@NgModule({
    imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule.forChild(routes), DashboardComponent,
        PartnersComponent,
        ConsumptionsComponent,
        AuditComponent,
        AdminReportsComponent],
    providers: [AdminService, AuditService],
})
export class AdminModule {}
