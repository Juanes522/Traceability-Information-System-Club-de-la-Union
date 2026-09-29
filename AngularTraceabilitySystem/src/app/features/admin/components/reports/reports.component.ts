import { Component } from '@angular/core';
import { ReportsComponent } from '../../../../shared/components/reports/reports.component';

@Component({
    selector: 'app-admin-reports',
    standalone: true,
    imports: [ReportsComponent],
    templateUrl: './reports.component.html',
})
/**
 * Pantalla de reportes del administrador.
 *
 * @remarks
 * Envoltorio de una línea sobre el formulario de reportes compartido, con el reporte de seguridad **activado**. Es la única
 * diferencia respecto de la pantalla del gestor.
 */
export class AdminReportsComponent {}
