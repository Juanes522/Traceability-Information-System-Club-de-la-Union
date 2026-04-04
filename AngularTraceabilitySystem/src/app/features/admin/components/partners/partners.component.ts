import { Component } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AdminService } from '../../admin.service';
import { PartnerProfile, Consumption } from '../../../../shared/models';

type SearchField = 'identification' | 'shareNumber' | 'firstName' | 'secondName';
type ActiveTab   = 'profile' | 'dependents' | 'consumptions';

@Component({
  selector: 'app-admin-partners',
  templateUrl: './partners.component.html',
  styleUrls: ['./partners.component.scss'],
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
  dependents: PartnerProfile[] = [];
  consumptions: Consumption[] = [];
  loadingDeps = false;
  loadingCons = false;
  expandedConsId: number | null = null;

  // Pagination
  currentPage = 1;
  readonly pageSize = 10;

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
    this.searchField = f;
    this.searchValue = '';
    this.results = [];
    this.searched = false;
    this.selectedPartner = null;
    this.currentPage = 1;
  }

  search(): void {
    if (!this.searchValue.trim()) return;
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
    this.searching = true;
    this.error = '';
    this.selectedPartner = null;
    this.currentPage = 1;
    this.adminService.getAllPartners().pipe(
      catchError(err => {
        if (err.status === 204) return of([]);
        this.error = 'Error al cargar socios.';
        return of([]);
      })
    ).subscribe(r => { this.results = r; this.searching = false; this.searched = true; });
  }

  selectPartner(p: PartnerProfile): void {
    this.selectedPartner = p;
    this.activeTab = 'profile';
    this.dependents = [];
    this.consumptions = [];
    this.loadDependents();
    this.loadConsumptions();
  }

  backToResults(): void { this.selectedPartner = null; }
  setTab(t: ActiveTab): void { this.activeTab = t; }

  loadDependents(): void {
    this.loadingDeps = true;
    this.adminService.getDependentsByIdentification(this.selectedPartner!.identification).pipe(
      catchError(err => {
        if (err.status === 204 || err.status === 404) return of([]);
        return of([]);
      })
    ).subscribe(d => { this.dependents = d; this.loadingDeps = false; });
  }

  loadConsumptions(): void {
    this.loadingCons = true;
    this.adminService.getConsumptionsByIdentification(this.selectedPartner!.identification).pipe(
      catchError(err => {
        if (err.status === 204 || err.status === 404) return of([]);
        return of([]);
      })
    ).subscribe(c => { this.consumptions = c; this.loadingCons = false; });
  }

  toggleCon(id: number): void {
    this.expandedConsId = this.expandedConsId === id ? null : id;
  }

  // Pagination helpers
  get totalPages(): number { return Math.ceil(this.results.length / this.pageSize); }
  get pagedResults(): PartnerProfile[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.results.slice(start, start + this.pageSize);
  }
  get pageNumbers(): number[] { return Array.from({ length: this.totalPages }, (_, i) => i + 1); }
  goToPage(n: number): void { if (n >= 1 && n <= this.totalPages) this.currentPage = n; }

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

  formatCurrency(v: number): string {
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(v);
  }

  totalFor(c: Consumption): number { return c.consumptionValue + c.iva + c.service + c.tip; }
}
