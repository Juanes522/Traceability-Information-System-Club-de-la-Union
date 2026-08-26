import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import type { EChartsOption } from 'echarts';
import { ChartComponent } from '../chart/chart.component';
import { MetricsService } from '../../services/metrics.service';
import { ConsumptionSummary, EnvironmentTotal, TrendPoint, ProductRank } from '../../models';

const GOLD = '#c7a567';
const INK = '#1A1F4D';

@Component({
  selector: 'app-partner-metrics',
  standalone: true,
  imports: [CommonModule, FormsModule, ChartComponent],
  templateUrl: './partner-metrics.component.html',
  styleUrls: ['./partner-metrics.component.scss'],
})
export class PartnerMetricsComponent implements OnInit {
  @Input() identification?: string;

  from?: string;
  to?: string;
  granularity: 'day' | 'week' | 'month' = 'day';
  loading = false;
  error = '';
  private readonly MAX_RANGE_DAYS = 366;
  summary?: ConsumptionSummary;
  visits?: number;
  lastVisit?: string | null;
  envOptions: EChartsOption | null = null;
  trendOptions: EChartsOption | null = null;
  topProductsOptions: EChartsOption | null = null;
  hasTopProducts = false;

  constructor(private metrics: MetricsService) {}

  ngOnInit(): void {
    this.lastMonth();
  }

  today(): void {
    const now = new Date();
    const start = new Date(now);
    start.setHours(0, 0, 0, 0);
    this.applyWindow(this.fmt(start), this.fmt(now));
  }
  lastWeek(): void {
    const now = new Date();
    this.applyWindow(this.fmt(new Date(now.getTime() - 7 * 864e5)), this.fmt(now));
  }
  lastMonth(): void {
    const now = new Date();
    this.applyWindow(this.fmt(new Date(now.getTime() - 30 * 864e5)), this.fmt(now));
  }

  setGranularity(g: 'day' | 'week' | 'month'): void {
    this.granularity = g;
    this.load();
  }

  load(): void {
    if (!this.isRangeValid()) {
      this.error = 'El rango no puede superar 1 año ni empezar después de la fecha final.';
      return;
    }
    this.loading = true;
    this.error = '';
    const w = { from: this.from, to: this.to };
    const obs = this.identification
      ? this.metrics.partner(this.identification, w, this.granularity)
      : this.metrics.partnerMe(w, this.granularity);
    obs.pipe(catchError(() => {
      this.error = 'No se pudieron cargar las métricas.';
      return of(undefined);
    })).subscribe(m => {
      if (m) {
        this.summary = m.summary;
        this.visits = m.visits;
        this.lastVisit = m.lastVisit;
        this.envOptions = this.buildEnv(m.byEnvironment);
        this.trendOptions = this.buildTrend(m.trend);
      }
      this.loading = false;
    });
    const products = this.identification
      ? this.metrics.productsPartner(this.identification, w, 10)
      : this.metrics.productsPartnerMe(w, 10);
    products.pipe(catchError(() => of([] as ProductRank[]))).subscribe(rows => {
      const list = Array.isArray(rows) ? rows : [];
      this.hasTopProducts = list.length > 0;
      this.topProductsOptions = this.buildTopProducts(list);
    });
  }

  private applyWindow(from: string, to: string): void {
    this.from = from;
    this.to = to;
    this.load();
  }

  private isRangeValid(): boolean {
    if (!this.from || !this.to) {
      return true;
    }
    const f = new Date(this.from).getTime();
    const t = new Date(this.to).getTime();
    if (t < f) {
      return false;
    }
    return (t - f) <= this.MAX_RANGE_DAYS * 24 * 60 * 60 * 1000;
  }

  private buildEnv(rows: EnvironmentTotal[]): EChartsOption {
    const sorted = [...(rows ?? [])].sort((a, b) => a.total - b.total);
    return {
      grid: { left: 110, right: 24, top: 10, bottom: 24 },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: sorted.map(r => r.environment) },
      series: [{ type: 'bar', data: sorted.map(r => r.total), itemStyle: { color: GOLD } }],
    };
  }

  private buildTrend(points: TrendPoint[]): EChartsOption {
    const pts = points ?? [];
    return {
      grid: { left: 48, right: 24, top: 20, bottom: 60 },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: pts.map(p => p.bucket), boundaryGap: false },
      yAxis: { type: 'value' },
      dataZoom: [{ type: 'inside' }, { type: 'slider', height: 18, bottom: 20 }],
      series: [{
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: pts.map(p => p.total),
        lineStyle: { color: INK },
        areaStyle: { color: 'rgba(26,31,77,0.12)' },
        itemStyle: { color: INK },
      }],
    };
  }

  private buildTopProducts(rows: ProductRank[]): EChartsOption {
    const sorted = [...(rows ?? [])].slice(0, 10).sort((a, b) => a.revenue - b.revenue);
    return {
      grid: { left: 140, right: 24, top: 10, bottom: 24 },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: sorted.map(r => r.name) },
      series: [{ type: 'bar', data: sorted.map(r => r.revenue), itemStyle: { color: GOLD } }],
    };
  }

  private fmt(d: Date): string {
    const p = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
  }

  formatCurrency(v: number | undefined): string {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(v ?? 0);
  }
}
