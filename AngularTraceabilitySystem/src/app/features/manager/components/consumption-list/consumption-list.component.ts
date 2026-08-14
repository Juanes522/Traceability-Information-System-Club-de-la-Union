import { Component, OnInit } from '@angular/core';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ManagerService } from '../../manager.service';
import { Consumption } from '../../../../shared/models';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { NgIf, NgFor } from '@angular/common';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';

@Component({
    selector: 'app-manager-consumption-list',
    templateUrl: './consumption-list.component.html',
    styleUrls: ['./consumption-list.component.scss'],
    imports: [ReactiveFormsModule, FormsModule, NgIf, NgFor, PaginatorComponent],
})
export class ConsumptionListComponent implements OnInit {
  environment = '';
  results: Consumption[] = [];
  searching = false;
  searched = false;
  error = '';
  expandedId: number | null = null;
  pageSize = 10;
  currentPage = 1;
  totalElements = 0;
  from?: string;
  to?: string;
  rangeError = false;

  private readonly MAX_RANGE_DAYS = 92;

  constructor(private managerService: ManagerService) {}

  ngOnInit(): void {
    this.lastWeek();
  }

  today(): void {
    const now = new Date();
    const start = new Date(now);
    start.setHours(0, 0, 0, 0);
    this.applyWindow(this.fmt(start), this.fmt(now));
  }

  lastWeek(): void {
    const now = new Date();
    const from = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    this.applyWindow(this.fmt(from), this.fmt(now));
  }

  lastMonth(): void {
    const now = new Date();
    const from = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
    this.applyWindow(this.fmt(from), this.fmt(now));
  }

  allTime(): void {
    this.applyWindow(undefined, undefined);
  }

  search(): void {
    if (!this.environment.trim()) return;
    this.currentPage = 1;
    this.load();
  }

  goToPage(n: number): void {
    if (n < 1 || n > this.totalPages || n === this.currentPage) return;
    this.currentPage = n;
    this.expandedId = null;
    this.load();
  }

  get pagedResults(): Consumption[] { return this.results; }
  get totalPages(): number { return Math.ceil(this.totalElements / this.pageSize); }

  load(): void {
    if (!this.isRangeValid()) { this.rangeError = true; return; }
    this.rangeError = false;
    this.searching = true;
    this.error = '';
    this.searched = false;
    this.expandedId = null;
    this.managerService.getConsumptionsByEnvironment(this.environment.trim(), {
      from: this.from, to: this.to, page: this.currentPage - 1, size: this.pageSize,
    }).pipe(
      catchError(() => {
        this.error = 'Error al consultar consumos. Verifica la conexión.';
        return of({ content: [] as Consumption[], totalElements: 0, number: 0, size: this.pageSize });
      })
    ).subscribe(page => {
      this.results = page.content;
      this.totalElements = page.totalElements;
      this.searching = false;
      this.searched = true;
    });
  }

  private applyWindow(from?: string, to?: string): void {
    this.from = from;
    this.to = to;
    this.currentPage = 1;
    if (this.environment.trim()) this.load();
  }

  private isRangeValid(): boolean {
    if (!this.from || !this.to) return true;
    const ms = new Date(this.to).getTime() - new Date(this.from).getTime();
    return ms <= this.MAX_RANGE_DAYS * 24 * 60 * 60 * 1000;
  }

  private fmt(d: Date): string {
    const p = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
  }

  toggleExpand(id: number): void { this.expandedId = this.expandedId === id ? null : id; }
  isExpanded(id: number): boolean { return this.expandedId === id; }
  totalFor(c: Consumption): number { return c.consumptionValue + c.iva + c.service + c.tip; }

  formatDateTime(d: string): string {
    if (!d) return '—';
    return new Date(d).toLocaleString('es-CO', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
  formatCurrency(v: number): string {
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(v);
  }
}
