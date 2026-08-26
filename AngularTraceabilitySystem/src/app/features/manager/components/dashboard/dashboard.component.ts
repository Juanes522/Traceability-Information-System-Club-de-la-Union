import { Component } from '@angular/core';
import { MetricsDashboardComponent } from '../../../../shared/components/dashboard/metrics-dashboard.component';

@Component({
    selector: 'app-manager-dashboard',
    standalone: true,
    imports: [MetricsDashboardComponent],
    templateUrl: './dashboard.component.html',
})
export class DashboardComponent {}
