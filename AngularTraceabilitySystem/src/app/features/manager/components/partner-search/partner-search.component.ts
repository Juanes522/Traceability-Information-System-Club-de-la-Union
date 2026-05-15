import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ManagerService } from '../../manager.service';
import { PartnerProfile } from '../../../../shared/models';
import { NgIf, NgFor, NgClass } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { PartnerDetailComponent } from '../partner-detail/partner-detail.component';

type SearchField = 'identification' | 'shareNumber' | 'firstName' | 'secondName';

@Component({
    selector: 'app-manager-partner-search',
    templateUrl: './partner-search.component.html',
    styleUrls: ['./partner-search.component.scss'],
    imports: [
        NgIf,
        NgFor,
        ReactiveFormsModule,
        FormsModule,
        NgClass,
        PartnerDetailComponent,
    ],
})
export class PartnerSearchComponent implements OnInit {
  searchField: SearchField = 'identification';
  searchValue = '';
  results: PartnerProfile[] = [];
  searching = false;
  searched = false;
  error = '';
  selectedPartner: PartnerProfile | null = null;

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

  constructor(private managerService: ManagerService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadAll();
  }

  setField(field: SearchField): void {
    this.searchField = field;
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
        obs = this.managerService.searchByIdentification(val).pipe(map(p => [p]));
        break;
      case 'shareNumber':
        obs = this.managerService.searchByShareNumber(val);
        break;
      case 'firstName':
        obs = this.managerService.searchByFirstName(val);
        break;
      case 'secondName':
        obs = this.managerService.searchBySecondName(val);
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
    ).subscribe(r => { this.results = r; this.searching = false; this.searched = true; this.cdr.detectChanges(); });
  }

  loadAll(): void {
    this.searching = true;
    this.error = '';
    this.selectedPartner = null;
    this.currentPage = 1;
    this.managerService.getAllPartners().pipe(
      catchError(err => {
        if (err.status === 204) return of([]);
        this.error = 'Error al cargar socios.';
        return of([]);
      })
    ).subscribe(r => { this.results = r; this.searching = false; this.searched = true; this.cdr.detectChanges(); });
  }

  selectPartner(p: PartnerProfile): void { this.selectedPartner = p; }
  backToResults(): void { this.selectedPartner = null; }

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
}
