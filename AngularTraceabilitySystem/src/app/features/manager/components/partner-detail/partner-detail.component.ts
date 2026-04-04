import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ManagerService } from '../../manager.service';
import { PartnerProfile, Consumption } from '../../../../shared/models';

type ActiveTab = 'profile' | 'dependents' | 'consumptions';

@Component({
  selector: 'app-manager-partner-detail',
  templateUrl: './partner-detail.component.html',
  styleUrls: ['./partner-detail.component.scss'],
})
export class PartnerDetailComponent implements OnInit {
  @Input() partner!: PartnerProfile;
  @Output() back = new EventEmitter<void>();

  activeTab: ActiveTab = 'profile';
  dependents: PartnerProfile[] = [];
  consumptions: Consumption[] = [];
  loadingDeps = false;
  loadingCons = false;
  expandedConsId: number | null = null;

  constructor(private managerService: ManagerService) {}

  ngOnInit(): void {
    this.loadDependents();
    this.loadConsumptions();
  }

  setTab(tab: ActiveTab): void {
    this.activeTab = tab;
  }

  loadDependents(): void {
    this.loadingDeps = true;
    this.managerService.getDependentsByIdentification(this.partner.identification).pipe(
      catchError(err => {
        if (err.status === 204 || err.status === 404) return of([]);
        return of([]);
      })
    ).subscribe(d => { this.dependents = d; this.loadingDeps = false; });
  }

  loadConsumptions(): void {
    this.loadingCons = true;
    this.managerService.getConsumptionsByIdentification(this.partner.identification).pipe(
      catchError(err => {
        if (err.status === 204 || err.status === 404) return of([]);
        return of([]);
      })
    ).subscribe(c => { this.consumptions = c; this.loadingCons = false; });
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

  formatDate(d: string): string {
    if (!d) return '—';
    return new Date(d + 'T00:00:00').toLocaleDateString('es-CO', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  formatCurrency(v: number): string {
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(v);
  }

  totalFor(c: Consumption): number {
    return c.consumptionValue + c.iva + c.service + c.tip;
  }
}
