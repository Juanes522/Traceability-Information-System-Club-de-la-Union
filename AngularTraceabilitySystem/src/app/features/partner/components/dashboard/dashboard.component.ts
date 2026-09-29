import { Component } from '@angular/core';
import { PartnerMetricsComponent } from '../../../../shared/components/partner-metrics/partner-metrics.component';

@Component({
    selector: 'app-partner-dashboard',
    standalone: true,
    imports: [PartnerMetricsComponent],
    templateUrl: './dashboard.component.html',
})
/**
 * Tablero del socio.
 *
 * @remarks
 * Envoltorio de una línea sobre el panel de métricas compartido. Al no pasarle ninguna identificación, ese panel consulta los
 * datos del propio usuario.
 *
 * Es uno de los tres componentes de la aplicación llamados igual, distinguibles solo por su ruta de importación y su selector.
 */
export class DashboardComponent {}
