import { Component } from '@angular/core';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AdminService } from '../../admin.service';
import { Consumption } from '../../../../shared/models';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { NgIf, NgFor } from '@angular/common';

@Component({
    selector: 'app-admin-consumptions',
    templateUrl: './consumptions.component.html',
    styleUrls: ['./consumptions.component.scss'],
    imports: [
        ReactiveFormsModule,
        FormsModule,
        NgIf,
        NgFor,
    ],
})
export class ConsumptionsComponent {
  environment = '';
  results: Consumption[] = [];
  searching = false;
  searched = false;
  error = '';
  expandedId: number | null = null;
  pageSize = 10;
  currentPage = 1;

  constructor(private adminService: AdminService) {}

  get pagedResults(): Consumption[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.results.slice(start, start + this.pageSize);
  }

  get totalPages(): number { return Math.ceil(this.results.length / this.pageSize); }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  goToPage(n: number): void {
    if (n >= 1 && n <= this.totalPages) { this.currentPage = n; this.expandedId = null; }
  }

  search(): void {
    if (!this.environment.trim()) return;
    this.searching = true;
    this.error = '';
    this.searched = false;
    this.currentPage = 1;
    this.adminService.getConsumptionsByEnvironment(this.environment.trim()).pipe(
      catchError(err => {
        if (err.status === 204 || err.status === 404) return of([]);
        this.error = 'Error al consultar consumos. Verifica la conexión.';
        return of([]);
      })
    ).subscribe(r => { this.results = r; this.searching = false; this.searched = true; });
  }

  toggleExpand(id: number): void { this.expandedId = this.expandedId === id ? null : id; }
  isExpanded(id: number): boolean { return this.expandedId === id; }

  totalFor(c: Consumption): number { return c.consumptionValue + c.iva + c.service + c.tip; }

  formatDateTime(d: string): string {
    if (!d) return '—';
    return new Date(d).toLocaleString('es-CO', {
      year: 'numeric', month: 'short', day: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  }

  formatCurrency(v: number): string {
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(v);
  }
}
