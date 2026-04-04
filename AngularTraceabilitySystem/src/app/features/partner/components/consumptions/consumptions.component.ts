import { Component, OnInit } from '@angular/core';
import { Consumption } from '../../../../shared/models';
import { PartnerService } from '../../partner.service';

@Component({
  selector: 'app-partner-consumptions',
  templateUrl: './consumptions.component.html',
  styleUrls: ['./consumptions.component.scss'],
})
export class ConsumptionsComponent implements OnInit {
  private all: Consumption[] = [];

  filtered: Consumption[] = [];
  loading = true;
  error = '';

  searchText = '';
  stateFilter: 'ALL' | 'A' | 'C' = 'ALL';
  expandedId: number | null = null;

  constructor(private partnerService: PartnerService) {}

  ngOnInit(): void {
    this.partnerService.getConsumptions().subscribe({
      next: (c) => {
        this.all = c.sort(
          (a, b) => new Date(b.consumptionOpening).getTime() - new Date(a.consumptionOpening).getTime()
        );
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        if (err.status === 204) {
          this.all = [];
          this.applyFilters();
        } else {
          this.error = 'No se pudieron cargar los consumos.';
        }
        this.loading = false;
      },
    });
  }

  applyFilters(): void {
    const text = this.searchText.trim().toLowerCase();
    this.filtered = this.all.filter(c => {
      const matchesState = this.stateFilter === 'ALL' || c.stateAccount === this.stateFilter;
      const matchesText = !text ||
        c.enviroment.toLowerCase().includes(text) ||
        c.table.toLowerCase().includes(text) ||
        c.waiterName.toLowerCase().includes(text);
      return matchesState && matchesText;
    });
  }

  setStateFilter(f: 'ALL' | 'A' | 'C'): void {
    this.stateFilter = f;
    this.applyFilters();
  }

  toggleExpand(id: number): void {
    this.expandedId = this.expandedId === id ? null : id;
  }

  isExpanded(id: number): boolean {
    return this.expandedId === id;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency', currency: 'COP', maximumFractionDigits: 0,
    }).format(amount);
  }

  formatDate(dt: string): string {
    if (!dt) return '—';
    return new Date(dt + 'T00:00:00').toLocaleDateString('es-CO', {
      year: 'numeric', month: 'short', day: 'numeric',
    });
  }

  totalFor(c: Consumption): number {
    return c.consumptionValue + c.iva + c.service + c.tip;
  }

  get openCount(): number  { return this.all.filter(c => c.stateAccount === 'A').length; }
  get closedCount(): number { return this.all.filter(c => c.stateAccount === 'C').length; }
}
