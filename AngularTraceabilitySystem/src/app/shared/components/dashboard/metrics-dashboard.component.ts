import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import type { EChartsOption } from 'echarts';
import { ChartComponent } from '../chart/chart.component';
import { MetricsService } from '../../services/metrics.service';
import {
  ConsumptionSummary, EnvironmentTotal, TrendPoint, Comparison,
  PeakHeatmapCell, SecuritySummary,
  AccessSummary, EnvironmentOccupancy, AttendancePoint, MonthlySnapshot,
  ProductRank, CategoryMix, EnvironmentCategory,
} from '../../models';

const GOLD = '#c7a567';
const INK = '#1A1F4D';
const WEEKDAYS = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
const AMBIENTES = ['Restaurante', 'Bar Caballeros', 'Coworking/Business Center', 'Eventos Club', 'Eventos', 'Rooftop', 'Salón Inglés', 'Bar Exterior', 'Peluquería', 'Spa'];

@Component({
  selector: 'app-metrics-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ChartComponent],
  templateUrl: './metrics-dashboard.component.html',
  styleUrls: ['./metrics-dashboard.component.scss'],
})
export class MetricsDashboardComponent implements OnInit {
  @Input() showSecurity = false;

  from?: string;
  to?: string;
  granularity: 'day' | 'week' | 'month' = 'day';
  loading = false;
  error = '';
  rangeError = false;
  private readonly MAX_RANGE_DAYS = 366;

  summary?: ConsumptionSummary;
  comparison?: Comparison;
  security?: SecuritySummary;

  envOptions: EChartsOption | null = null;
  trendOptions: EChartsOption | null = null;
  heatmapOptions: EChartsOption | null = null;

  accessSummary?: AccessSummary;
  occupancyOptions: EChartsOption | null = null;
  attendanceOptions: EChartsOption | null = null;

  snapshotsOptions: EChartsOption | null = null;
  hasSnapshots = false;

  topProductsOptions: EChartsOption | null = null;
  hasTopProducts = false;
  readonly ambientes = AMBIENTES;
  topEnv = '';
  topMode: 'revenue' | 'quantity' = 'revenue';
  categoryMixOptions: EChartsOption | null = null;
  hasCategoryMix = false;
  envCategoryOptions: EChartsOption | null = null;
  hasEnvCategory = false;

  constructor(private metrics: MetricsService) {}

  ngOnInit(): void {
    this.lastMonth();
    this.metrics.snapshots().pipe(catchError(() => of([] as MonthlySnapshot[])))
      .subscribe(list => {
        this.hasSnapshots = list.length > 0;
        this.snapshotsOptions = this.buildSnapshots(list);
      });
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
      this.rangeError = true;
      this.loading = false;
      return;
    }
    this.rangeError = false;
    this.loading = true;
    this.error = '';
    this.loadTopProducts();
    const w = { from: this.from, to: this.to };
    forkJoin({
      summary: this.metrics.consumptionSummary(w).pipe(catchError(() => of(undefined))),
      comparison: this.metrics.comparison().pipe(catchError(() => of(undefined))),
      env: this.metrics.byEnvironment(w).pipe(catchError(() => of([] as EnvironmentTotal[]))),
      trend: this.metrics.trend(w, this.granularity).pipe(catchError(() => of([] as TrendPoint[]))),
      heatmap: this.metrics.peakHeatmap(w).pipe(catchError(() => of([] as PeakHeatmapCell[]))),
      security: this.showSecurity
        ? this.metrics.securitySummary(w).pipe(catchError(() => of({ loginFailedCount: 0, rateLimitBlockCount: 0, accessDeniedCount: 0, criticalAlertCount: 0, degraded: true } as SecuritySummary)))
        : of(undefined),
      accessSummary: this.metrics.accessSummary(w).pipe(catchError(() => of(undefined))),
      occupancy: this.metrics.accessOccupancy().pipe(catchError(() => of([] as EnvironmentOccupancy[]))),
      attendance: this.metrics.accessAttendance(w, this.granularity).pipe(catchError(() => of([] as AttendancePoint[]))),
      categoryMix: this.metrics.productsCategoryMix(w).pipe(catchError(() => of([] as CategoryMix[]))),
      envCategory: this.metrics.productsByEnvironment(w).pipe(catchError(() => of([] as EnvironmentCategory[]))),
    }).subscribe(res => {
      this.summary = res.summary;
      this.comparison = res.comparison;
      this.security = res.security;
      this.envOptions = this.buildEnv(res.env);
      this.trendOptions = this.buildTrend(res.trend);
      this.heatmapOptions = this.buildHeatmap(res.heatmap);
      this.accessSummary = res.accessSummary;
      this.occupancyOptions = this.buildOccupancy(res.occupancy);
      this.attendanceOptions = this.buildAttendance(res.attendance);
      this.hasCategoryMix = res.categoryMix.length > 0;
      this.categoryMixOptions = this.buildCategoryMix(res.categoryMix);
      this.hasEnvCategory = res.envCategory.length > 0;
      this.envCategoryOptions = this.buildEnvCategory(res.envCategory);
      this.error = res.summary ? '' : 'No se pudieron cargar las métricas.';
      this.loading = false;
    });
  }

  loadTopProducts(): void {
    if (!this.isRangeValid()) { return; }
    this.metrics.productsTop({ from: this.from, to: this.to }, 10, this.topEnv || undefined, this.topMode)
      .pipe(catchError(() => of([] as ProductRank[])))
      .subscribe(rows => {
        this.hasTopProducts = rows.length > 0;
        this.topProductsOptions = this.buildTopProducts(rows, this.topMode);
      });
  }
  setTopMode(mode: 'revenue' | 'quantity'): void {
    this.topMode = mode;
    this.loadTopProducts();
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
    const sorted = [...rows].sort((a, b) => a.total - b.total);
    return {
      grid: { left: 110, right: 24, top: 10, bottom: 24 },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: sorted.map(r => r.environment) },
      series: [{
        type: 'bar',
        data: sorted.map(r => r.total),
        itemStyle: { color: GOLD },
        label: { show: true, position: 'right', formatter: (p: any) => `${(sorted[p.dataIndex].percentage).toFixed(0)}%` },
      }],
    };
  }

  private buildTrend(points: TrendPoint[]): EChartsOption {
    return {
      grid: { left: 48, right: 24, top: 20, bottom: 60 },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: points.map(p => p.bucket), boundaryGap: false },
      yAxis: { type: 'value' },
      dataZoom: [{ type: 'inside' }, { type: 'slider', height: 18, bottom: 20 }],
      series: [{
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: points.map(p => p.total),
        lineStyle: { color: GOLD },
        areaStyle: { color: 'rgba(199,165,103,0.18)' },
        itemStyle: { color: GOLD },
      }],
    };
  }

  private buildOccupancy(rows: EnvironmentOccupancy[]): EChartsOption {
    const sorted = [...(rows ?? [])].sort((a, b) => a.partners - b.partners);
    return {
      grid: { left: 110, right: 24, top: 10, bottom: 24 },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: sorted.map(r => r.environment) },
      series: [{ type: 'bar', data: sorted.map(r => r.partners), itemStyle: { color: GOLD } }],
    };
  }

  private buildAttendance(points: AttendancePoint[]): EChartsOption {
    const pts = points ?? [];
    return {
      grid: { left: 48, right: 24, top: 20, bottom: 60 },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: pts.map(p => p.bucket), boundaryGap: false },
      yAxis: { type: 'value' },
      dataZoom: [{ type: 'inside' }, { type: 'slider', height: 18, bottom: 20 }],
      series: [{ type: 'line', smooth: true, showSymbol: false, data: pts.map(p => p.count), lineStyle: { color: INK }, areaStyle: { color: 'rgba(26,31,77,0.12)' }, itemStyle: { color: INK } }],
    };
  }

  private buildSnapshots(list: MonthlySnapshot[]): EChartsOption {
    return {
      grid: { left: 56, right: 24, top: 20, bottom: 40 },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: list.map(s => s.yearMonth) },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: list.map(s => s.totalBilled), itemStyle: { color: INK } }],
    };
  }

  private buildHeatmap(cells: PeakHeatmapCell[]): EChartsOption {
    const data = cells.map(c => [c.hour, c.weekday, Math.round(c.total)]);
    const max = cells.reduce((m, c) => Math.max(m, c.total), 0);
    return {
      tooltip: {
        position: 'top',
        formatter: (p: any) => `${WEEKDAYS[p.value[1]]} ${p.value[0]}:00 — ${p.value[2]}`,
      },
      grid: { left: 44, right: 12, top: 56, bottom: 40 },
      xAxis: { type: 'category', data: Array.from({ length: 24 }, (_, h) => `${h}`), splitArea: { show: true } },
      yAxis: { type: 'category', data: WEEKDAYS, splitArea: { show: true } },
      visualMap: {
        min: 0, max: max || 1, calculable: true, orient: 'horizontal',
        left: 'center', top: 6, itemWidth: 12, itemHeight: 90,
        inRange: { color: ['#f5f1e8', GOLD, INK] },
        text: ['más consumo', 'menos'],
      },
      series: [{ type: 'heatmap', data, emphasis: { itemStyle: { shadowBlur: 6 } } }],
    };
  }

  private buildTopProducts(rows: ProductRank[], mode: 'revenue' | 'quantity' = 'revenue'): EChartsOption {
    const sorted = [...(rows ?? [])].slice(0, 10)
      .sort((a, b) => mode === 'quantity' ? a.quantity - b.quantity : a.revenue - b.revenue);
    return {
      grid: { left: 160, right: 24, top: 10, bottom: 24 },
      tooltip: {
        trigger: 'axis', axisPointer: { type: 'shadow' },
        formatter: (p: any) => {
          const r = sorted[p[0].dataIndex];
          return `${r.name}<br/>${r.quantity} u · ${this.formatCurrency(r.revenue)}`;
        },
      },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: sorted.map(r => r.name) },
      series: [{ type: 'bar', data: sorted.map(r => mode === 'quantity' ? r.quantity : r.revenue), itemStyle: { color: GOLD } }],
    };
  }

  private catLabel(v: string): string {
    return v && v.trim() ? v : 'Sin categoría';
  }

  private buildCategoryMix(rows: CategoryMix[]): EChartsOption {
    const list = [...(rows ?? [])].sort((a, b) => b.revenue - a.revenue);
    const top = list.slice(0, 15).map(r => ({ label: `${this.catLabel(r.category)} · ${this.catLabel(r.subcategory)}`, revenue: r.revenue }));
    const rest = list.slice(15);
    if (rest.length > 0) {
      top.push({ label: `Otros (${rest.length})`, revenue: rest.reduce((s, r) => s + r.revenue, 0) });
    }
    const sorted = top.sort((a, b) => a.revenue - b.revenue);
    return {
      grid: { left: 220, right: 24, top: 10, bottom: 24 },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: sorted.map(i => i.label) },
      series: [{ type: 'bar', data: sorted.map(i => i.revenue), itemStyle: { color: INK } }],
    };
  }

  private buildEnvCategory(rows: EnvironmentCategory[]): EChartsOption {
    const list = rows ?? [];
    const environments = Array.from(new Set(list.map(r => r.environment)));
    const categories = Array.from(new Set(list.map(r => r.category)));
    const series = categories.map(cat => ({
      name: this.catLabel(cat),
      type: 'bar' as const,
      stack: 'total',
      data: environments.map(env => {
        const match = list.find(r => r.environment === env && r.category === cat);
        return match ? match.revenue : 0;
      }),
    }));
    return {
      grid: { left: 48, right: 24, top: 40, bottom: 90 },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { top: 0 },
      xAxis: { type: 'category', data: environments, axisLabel: { interval: 0, rotate: 30 } },
      yAxis: { type: 'value' },
      series,
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
