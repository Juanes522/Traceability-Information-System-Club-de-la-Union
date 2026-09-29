import { Component } from '@angular/core';
import { MetricsDashboardComponent } from '../../../../shared/components/dashboard/metrics-dashboard.component';

@Component({
    selector: 'app-admin-dashboard',
    standalone: true,
    imports: [MetricsDashboardComponent],
    templateUrl: './dashboard.component.html',
})
/**
 * Tablero del administrador.
 *
 * @remarks
 * Envoltorio de una línea sobre el tablero de métricas compartido, con los indicadores de seguridad **activados**. Es la única
 * diferencia respecto del tablero del gestor.
 */
export class DashboardComponent {}
