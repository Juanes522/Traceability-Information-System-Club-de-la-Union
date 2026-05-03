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
      return !text ||
        c.enviroment.toLowerCase().includes(text) ||
        c.table.toLowerCase().includes(text) ||
        c.waiterName.toLowerCase().includes(text);
    });
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

  formatDateTime(dt: string): string {
    if (!dt) return '—';
    return new Date(dt).toLocaleString('es-CO', {
      year: 'numeric', month: 'short', day: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  }

  totalFor(c: Consumption): number {
    return c.consumptionValue + c.iva + c.service + c.tip;
  }
}
