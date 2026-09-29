import { Component } from '@angular/core';
import { MetricsDashboardComponent } from '../../../../shared/components/dashboard/metrics-dashboard.component';

@Component({
    selector: 'app-manager-dashboard',
    standalone: true,
    imports: [MetricsDashboardComponent],
    templateUrl: './dashboard.component.html',
})
/**
 * Tablero del gestor.
 *
 * @remarks
 * Envoltorio de una línea sobre el tablero de métricas compartido, con los indicadores de seguridad desactivados. Es la única
 * diferencia respecto del tablero del administrador.
 */
export class DashboardComponent {}
