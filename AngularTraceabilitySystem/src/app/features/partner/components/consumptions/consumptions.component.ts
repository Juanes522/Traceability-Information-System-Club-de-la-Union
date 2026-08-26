import { Component, OnInit } from '@angular/core';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Consumption } from '../../../../shared/models';
import { PartnerService } from '../../partner.service';
import { NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';

@Component({
    selector: 'app-partner-consumptions',
    templateUrl: './consumptions.component.html',
    styleUrls: ['./consumptions.component.scss'],
    imports: [NgIf, NgFor, FormsModule, PaginatorComponent],
})
export class ConsumptionsComponent implements OnInit {
  results: Consumption[] = [];
  loading = false;
  error = '';
  expandedId: number | null = null;

  pageSize = 10;
  currentPage = 1;
  totalElements = 0;
  from?: string;
  to?: string;
  rangeError = false;

  private readonly MAX_RANGE_DAYS = 92;

  constructor(private partnerService: PartnerService) {}

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

  goToPage(n: number): void {
    if (n < 1 || n > this.totalPages || n === this.currentPage) return;
    this.currentPage = n;
    this.expandedId = null;
    this.load();
  }

  onWindowEdit(): void {
    this.currentPage = 1;
    this.load();
  }

  get totalPages(): number { return Math.ceil(this.totalElements / this.pageSize); }

  load(): void {
    if (!this.isRangeValid()) { this.rangeError = true; return; }
    this.rangeError = false;
    this.loading = true;
    this.error = '';
    this.expandedId = null;
    this.partnerService.getConsumptions({
      from: this.from, to: this.to, page: this.currentPage - 1, size: this.pageSize,
    }).pipe(
      catchError(() => {
        this.error = 'No se pudieron cargar los consumos.';
        return of({ content: [] as Consumption[], totalElements: 0, number: 0, size: this.pageSize });
      })
    ).subscribe(page => {
      this.results = page.content;
      this.totalElements = page.totalElements;
      this.loading = false;
    });
  }

  private applyWindow(from?: string, to?: string): void {
    this.from = from;
    this.to = to;
    this.currentPage = 1;
    this.load();
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

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(amount);
  }
  formatDateTime(dt: string): string {
    if (!dt) return '—';
    return new Date(dt).toLocaleString('es-CO', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
  totalFor(c: Consumption): number { return c.consumptionValue + c.iva + c.service + c.tip; }
}
