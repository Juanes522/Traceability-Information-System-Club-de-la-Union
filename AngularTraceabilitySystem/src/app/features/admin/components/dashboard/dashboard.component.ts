import { Component } from '@angular/core';
import { MetricsDashboardComponent } from '../../../../shared/components/dashboard/metrics-dashboard.component';

@Component({
    selector: 'app-admin-dashboard',
    standalone: true,
    imports: [MetricsDashboardComponent],
    templateUrl: './dashboard.component.html',
})
export class DashboardComponent {}
