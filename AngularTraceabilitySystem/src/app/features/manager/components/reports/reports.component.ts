import { Component } from '@angular/core';
import { ReportsComponent } from '../../../../shared/components/reports/reports.component';

@Component({
    selector: 'app-manager-reports',
    standalone: true,
    imports: [ReportsComponent],
    templateUrl: './reports.component.html',
})
/**
 * Pantalla de reportes del gestor.
 *
 * @remarks
 * Envoltorio de una línea sobre el formulario de reportes compartido, con el reporte de seguridad desactivado. La clase se
 * llama de forma distinta al componente que envuelve para evitar la colisión de nombres.
 *
 * Recuérdese que ocultar esa opción es solo presentación: quien impide de verdad que un gestor descargue ese reporte es la
 * autorización del backend.
 */
export class ManagerReportsComponent {}
