import { Component } from '@angular/core';
import { PartnerMetricsComponent } from '../../../../shared/components/partner-metrics/partner-metrics.component';

@Component({
    selector: 'app-partner-dashboard',
    standalone: true,
    imports: [PartnerMetricsComponent],
    templateUrl: './dashboard.component.html',
})
export class DashboardComponent {}
