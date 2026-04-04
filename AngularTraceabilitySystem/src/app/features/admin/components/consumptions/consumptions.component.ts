import { Component } from '@angular/core';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AdminService } from '../../admin.service';
import { Consumption } from '../../../../shared/models';

@Component({
  selector: 'app-admin-consumptions',
  templateUrl: './consumptions.component.html',
  styleUrls: ['./consumptions.component.scss'],
})
export class ConsumptionsComponent {
  environment = '';
  results: Consumption[] = [];
  searching = false;
  searched = false;
  error = '';
  expandedId: number | null = null;

  constructor(private adminService: AdminService) {}

  search(): void {
    if (!this.environment.trim()) return;
    this.searching = true;
    this.error = '';
    this.searched = false;
    this.adminService.getConsumptionsByEnvironment(this.environment.trim()).pipe(
      catchError(err => {
        if (err.status === 204 || err.status === 404) return of([]);
        this.error = 'Error al consultar consumos. Verifica la conexión.';
        return of([]);
      })
    ).subscribe(r => { this.results = r; this.searching = false; this.searched = true; });
  }

  get openCount(): number { return this.results.filter(c => c.stateAccount === 'A').length; }
  get closedCount(): number { return this.results.filter(c => c.stateAccount !== 'A').length; }

  toggleExpand(id: number): void { this.expandedId = this.expandedId === id ? null : id; }
  isExpanded(id: number): boolean { return this.expandedId === id; }

  totalFor(c: Consumption): number { return c.consumptionValue + c.iva + c.service + c.tip; }

  formatDate(d: string): string {
    if (!d) return '—';
    return new Date(d + 'T00:00:00').toLocaleDateString('es-CO', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  formatCurrency(v: number): string {
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(v);
  }
}
