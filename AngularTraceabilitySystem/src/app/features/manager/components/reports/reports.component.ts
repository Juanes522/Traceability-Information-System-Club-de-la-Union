import { Component } from '@angular/core';
import { ReportsComponent } from '../../../../shared/components/reports/reports.component';

@Component({
    selector: 'app-manager-reports',
    standalone: true,
    imports: [ReportsComponent],
    templateUrl: './reports.component.html',
})
export class ManagerReportsComponent {}
