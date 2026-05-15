import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ManagerService } from '../../manager.service';
import { PartnerProfile, Consumption } from '../../../../shared/models';
import { NgIf, NgClass, NgFor } from '@angular/common';

type ActiveTab = 'profile' | 'consumptions';

@Component({
    selector: 'app-manager-partner-detail',
    templateUrl: './partner-detail.component.html',
    styleUrls: ['./partner-detail.component.scss'],
    imports: [
        NgIf,
        NgClass,
        NgFor,
    ],
})
export class PartnerDetailComponent implements OnInit {
  @Input() partner!: PartnerProfile;
  @Output() back = new EventEmitter<void>();

  activeTab: ActiveTab = 'profile';
  consumptions: Consumption[] = [];
  loadingCons = false;
  expandedConsId: number | null = null;

  constructor(private managerService: ManagerService) {}

  ngOnInit(): void {
    this.loadConsumptions();
  }

  setTab(tab: ActiveTab): void {
    this.activeTab = tab;
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
