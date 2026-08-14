import { Component } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AdminService } from '../../admin.service';
import { PartnerProfile, Consumption } from '../../../../shared/models';
import { NgIf, NgFor, NgClass } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';

type SearchField = 'identification' | 'shareNumber' | 'firstName' | 'secondName';
type ActiveTab   = 'profile' | 'consumptions';

@Component({
    selector: 'app-admin-partners',
    templateUrl: './partners.component.html',
    styleUrls: ['./partners.component.scss'],
    imports: [
        NgIf,
        NgFor,
        ReactiveFormsModule,
        FormsModule,
        NgClass,
        PaginatorComponent,
    ],
})
export class PartnersComponent {
  searchField: SearchField = 'identification';
  searchValue = '';
  results: PartnerProfile[] = [];
  searching = false;
  searched = false;
  error = '';

  selectedPartner: PartnerProfile | null = null;
  activeTab: ActiveTab = 'profile';
  consumptions: Consumption[] = [];
  loadingCons = false;
  consError = false;
  expandedConsId: number | null = null;

  consCurrentPage = 1;
  readonly consPageSize = 10;
  consTotalElements = 0;
  consFrom?: string;
  consTo?: string;
  consRangeError = false;
  private readonly CONS_MAX_RANGE_DAYS = 92;

  // Pagination
  currentPage = 1;
  readonly pageSize = 10;
  mode: 'search' | 'all' = 'search';
  totalElements = 0;

  readonly searchFields: SearchField[] = ['identification', 'shareNumber', 'firstName', 'secondName'];
  readonly fieldLabels: Record<SearchField, string> = {
    identification: 'Cédula',
    shareNumber: 'Número de acción',
    firstName: 'Primer nombre',
    secondName: 'Segundo nombre',
  };
  readonly fieldPlaceholders: Record<SearchField, string> = {
    identification: 'Ej: 0901234561',
    shareNumber: 'Ej: 1001',
    firstName: 'Ej: Carlos',
    secondName: 'Ej: Andrés',
  };

  constructor(private adminService: AdminService) {}

  setField(f: SearchField): void {
    this.mode = 'search';
    this.searchField = f;
    this.searchValue = '';
    this.results = [];
    this.searched = false;
    this.selectedPartner = null;
    this.currentPage = 1;
  }

  search(): void {
    if (!this.searchValue.trim()) return;
    this.mode = 'search';
    this.searching = true;
    this.error = '';
    this.searched = false;
    this.selectedPartner = null;
    this.currentPage = 1;

    const val = this.searchValue.trim();
    let obs: Observable<PartnerProfile[]>;

    switch (this.searchField) {
      case 'identification':
        obs = this.adminService.searchByIdentification(val).pipe(map(p => [p]));
        break;
      case 'shareNumber':
        obs = this.adminService.searchByShareNumber(val);
        break;
      case 'firstName':
        obs = this.adminService.searchByFirstName(val);
        break;
      case 'secondName':
        obs = this.adminService.searchBySecondName(val);
        break;
      default:
        obs = of([]);
    }

    obs.pipe(
      catchError(err => {
        if (err.status === 404 || err.status === 204) return of([]);
        this.error = 'Error al buscar socios. Verifica la conexión con el servidor.';
        return of([]);
      })
    ).subscribe(r => { this.results = r; this.searching = false; this.searched = true; });
  }

  loadAll(): void {
    this.mode = 'all';
    this.error = '';
    this.selectedPartner = null;
    this.currentPage = 1;
    this.loadAllPage(0);
  }

  private loadAllPage(pageIndex: number): void {
    this.searching = true;
    this.error = '';
    this.adminService.getAllPartners(pageIndex, this.pageSize).pipe(
      catchError(() => of(null))
    ).subscribe(page => {
      if (pageIndex !== this.currentPage - 1) {
        return;
      }
      if (page === null) {
        this.error = 'Error al cargar socios.';
        this.results = [];
        this.totalElements = 0;
      } else {
        this.results = page.content;
        this.totalElements = page.totalElements;
      }
      this.searching = false;
      this.searched = true;
    });
  }

  selectPartner(p: PartnerProfile): void {
    this.selectedPartner = p;
    this.activeTab = 'profile';
    this.consumptions = [];
    this.consLastWeek();
  }

  backToResults(): void { this.selectedPartner = null; }
  setTab(t: ActiveTab): void { this.activeTab = t; }

  loadConsumptions(): void {
    if (!this.isConsRangeValid()) { this.consRangeError = true; return; }
    this.consRangeError = false;
    this.consError = false;
    this.loadingCons = true;
    this.expandedConsId = null;
    this.adminService.getConsumptionsByIdentification(this.selectedPartner!.identification, {
      from: this.consFrom, to: this.consTo, page: this.consCurrentPage - 1, size: this.consPageSize,
    }).pipe(
      catchError(() => { this.consError = true; return of({ content: [] as Consumption[], totalElements: 0, number: 0, size: this.consPageSize }); })
    ).subscribe(page => {
      this.consumptions = page.content;
      this.consTotalElements = page.totalElements;
      this.loadingCons = false;
    });
  }

  consToday(): void {
    const now = new Date();
    const start = new Date(now);
    start.setHours(0, 0, 0, 0);
    this.applyConsWindow(this.consFmt(start), this.consFmt(now));
  }

  consLastWeek(): void {
    const now = new Date();
    const from = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    this.applyConsWindow(this.consFmt(from), this.consFmt(now));
  }

  consLastMonth(): void {
    const now = new Date();
    const from = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
    this.applyConsWindow(this.consFmt(from), this.consFmt(now));
  }

  consAllTime(): void {
    this.applyConsWindow(undefined, undefined);
  }

  consGoToPage(n: number): void {
    if (n < 1 || n > this.consTotalPages || n === this.consCurrentPage) return;
    this.consCurrentPage = n;
    this.expandedConsId = null;
    this.loadConsumptions();
  }

  onConsWindowEdit(): void {
    this.consCurrentPage = 1;
    this.loadConsumptions();
  }

  get consTotalPages(): number { return Math.ceil(this.consTotalElements / this.consPageSize); }

  private applyConsWindow(from?: string, to?: string): void {
    this.consFrom = from;
    this.consTo = to;
    this.consCurrentPage = 1;
    this.loadConsumptions();
  }

  private isConsRangeValid(): boolean {
    if (!this.consFrom || !this.consTo) return true;
    const ms = new Date(this.consTo).getTime() - new Date(this.consFrom).getTime();
    return ms <= this.CONS_MAX_RANGE_DAYS * 24 * 60 * 60 * 1000;
  }

  private consFmt(d: Date): string {
    const p = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
  }

  toggleCon(id: number): void {
    this.expandedConsId = this.expandedConsId === id ? null : id;
  }

  // Pagination helpers
  get totalPages(): number {
    const total = this.mode === 'all' ? this.totalElements : this.results.length;
    return Math.ceil(total / this.pageSize);
  }
  get pagedResults(): PartnerProfile[] {
    if (this.mode === 'all') return this.results;
    const start = (this.currentPage - 1) * this.pageSize;
    return this.results.slice(start, start + this.pageSize);
  }
  get resultCount(): number { return this.mode === 'all' ? this.totalElements : this.results.length; }
  goToPage(n: number): void {
    if (n < 1 || n > this.totalPages || n === this.currentPage) return;
    this.currentPage = n;
    if (this.mode === 'all') this.loadAllPage(n - 1);
  }

  fullName(p: PartnerProfile): string {
    return [p.firstName, p.secondName, p.lastName].filter(Boolean).join(' ');
  }

  getInitials(p: PartnerProfile): string {
    return [p.firstName, p.lastName].filter(Boolean).map(w => w[0]).join('').toUpperCase();
  }

  formatDate(d: string): string {
    if (!d) return '—';
    return new Date(d + 'T00:00:00').toLocaleDateString('es-CO', { year: 'numeric', month: 'short', day: 'numeric' });
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

  totalFor(c: Consumption): number { return c.consumptionValue + c.iva + c.service + c.tip; }
}
