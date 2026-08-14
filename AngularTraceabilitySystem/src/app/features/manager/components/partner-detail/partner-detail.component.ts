import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ManagerService } from '../../manager.service';
import { PartnerProfile, Consumption } from '../../../../shared/models';
import { NgIf, NgClass, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';

type ActiveTab = 'profile' | 'consumptions';

@Component({
    selector: 'app-manager-partner-detail',
    templateUrl: './partner-detail.component.html',
    styleUrls: ['./partner-detail.component.scss'],
    imports: [
        NgIf,
        NgClass,
        NgFor,
        FormsModule,
        PaginatorComponent,
    ],
})
export class PartnerDetailComponent implements OnInit {
  @Input() partner!: PartnerProfile;
  @Output() back = new EventEmitter<void>();

  activeTab: ActiveTab = 'profile';
  consumptions: Consumption[] = [];
  loadingCons = false;
  consError = false;
  expandedConsId: number | null = null;
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

  setTab(tab: ActiveTab): void {
    this.activeTab = tab;
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
    this.expandedConsId = null;
    this.loadConsumptions();
  }

  onWindowEdit(): void {
    this.currentPage = 1;
    this.loadConsumptions();
  }

  get totalPages(): number { return Math.ceil(this.totalElements / this.pageSize); }

  private applyWindow(from?: string, to?: string): void {
    this.from = from;
    this.to = to;
    this.currentPage = 1;
    this.loadConsumptions();
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

  loadConsumptions(): void {
    if (!this.isRangeValid()) { this.rangeError = true; return; }
    this.rangeError = false;
    this.consError = false;
    this.loadingCons = true;
    this.expandedConsId = null;
    this.managerService.getConsumptionsByIdentification(this.partner.identification, {
      from: this.from, to: this.to, page: this.currentPage - 1, size: this.pageSize,
    }).pipe(
      catchError(() => { this.consError = true; return of({ content: [] as Consumption[], totalElements: 0, number: 0, size: this.pageSize }); })
    ).subscribe(page => {
      this.consumptions = page.content;
      this.totalElements = page.totalElements;
      this.loadingCons = false;
    });
  }

  toggleCon(id: number): void {
    this.expandedConsId = this.expandedConsId === id ? null : id;
  }

  fullName(p: PartnerProfile): string {
    return [p.firstName, p.secondName, p.lastName].filter(Boolean).join(' ');
  }

  getInitials(p: PartnerProfile): string {
    return [p.firstName, p.lastName].filter(Boolean).map(w => w[0]).join('').toUpperCase();
  }

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

  totalFor(c: Consumption): number {
    return c.consumptionValue + c.iva + c.service + c.tip;
  }
}
