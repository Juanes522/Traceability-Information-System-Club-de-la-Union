import { Component } from '@angular/core';
import { ReportsComponent } from '../../../../shared/components/reports/reports.component';

@Component({
    selector: 'app-admin-reports',
    standalone: true,
    imports: [ReportsComponent],
    templateUrl: './reports.component.html',
})
export class AdminReportsComponent {}
