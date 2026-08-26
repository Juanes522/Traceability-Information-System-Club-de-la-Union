import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../../core/config/api.config';
import {
  ConsumptionSummary, EnvironmentTotal, TrendPoint, Comparison,
  PeakHeatmapCell, SecuritySummary, MetricsWindow, PartnerMetrics,
  AccessSummary, EnvironmentOccupancy, AttendancePoint, MonthlySnapshot,
  ProductRank, CategoryMix, EnvironmentCategory,
} from '../models';

@Injectable({ providedIn: 'root' })
export class MetricsService {
  constructor(private http: HttpClient) {}

  private win(w: MetricsWindow): HttpParams {
    let p = new HttpParams();
    if (w.from) { p = p.set('from', w.from); }
    if (w.to) { p = p.set('to', w.to); }
    return p;
  }

  consumptionSummary(w: MetricsWindow): Observable<ConsumptionSummary> {
    return this.http.get<ConsumptionSummary>(`${API_BASE}/metrics/consumption/summary`, { params: this.win(w) });
  }
  byEnvironment(w: MetricsWindow): Observable<EnvironmentTotal[]> {
    return this.http.get<EnvironmentTotal[]>(`${API_BASE}/metrics/consumption/by-environment`, { params: this.win(w) });
  }
  trend(w: MetricsWindow, granularity: 'day' | 'week' | 'month'): Observable<TrendPoint[]> {
    return this.http.get<TrendPoint[]>(`${API_BASE}/metrics/consumption/trend`, { params: this.win(w).set('granularity', granularity) });
  }
  comparison(month?: string): Observable<Comparison> {
    let p = new HttpParams();
    if (month) { p = p.set('month', month); }
    return this.http.get<Comparison>(`${API_BASE}/metrics/consumption/comparison`, { params: p });
  }
  peakHeatmap(w: MetricsWindow): Observable<PeakHeatmapCell[]> {
    return this.http.get<PeakHeatmapCell[]>(`${API_BASE}/metrics/consumption/peak-heatmap`, { params: this.win(w) });
  }
  securitySummary(w: MetricsWindow): Observable<SecuritySummary> {
    return this.http.get<SecuritySummary>(`${API_BASE}/metrics/security/summary`, { params: this.win(w) });
  }
  partnerMe(w: MetricsWindow, granularity: 'day' | 'week' | 'month'): Observable<PartnerMetrics> {
    return this.http.get<PartnerMetrics>(`${API_BASE}/metrics/partner/me`, { params: this.win(w).set('granularity', granularity) });
  }
  partner(identification: string, w: MetricsWindow, granularity: 'day' | 'week' | 'month'): Observable<PartnerMetrics> {
    return this.http.get<PartnerMetrics>(`${API_BASE}/metrics/partner/${identification}`, { params: this.win(w).set('granularity', granularity) });
  }
  accessSummary(w: MetricsWindow): Observable<AccessSummary> {
    return this.http.get<AccessSummary>(`${API_BASE}/metrics/access/summary`, { params: this.win(w) });
  }
  accessOccupancy(): Observable<EnvironmentOccupancy[]> {
    return this.http.get<EnvironmentOccupancy[]>(`${API_BASE}/metrics/access/occupancy`);
  }
  accessAttendance(w: MetricsWindow, granularity: 'day' | 'week' | 'month'): Observable<AttendancePoint[]> {
    return this.http.get<AttendancePoint[]>(`${API_BASE}/metrics/access/attendance`, { params: this.win(w).set('granularity', granularity) });
  }
  snapshots(): Observable<MonthlySnapshot[]> {
    return this.http.get<MonthlySnapshot[]>(`${API_BASE}/metrics/snapshots`);
  }
  productsTop(w: MetricsWindow, limit = 20, environment?: string, sort?: string): Observable<ProductRank[]> {
    let p = this.win(w).set('limit', String(limit));
    if (environment) { p = p.set('environment', environment); }
    if (sort) { p = p.set('sort', sort); }
    return this.http.get<ProductRank[]>(`${API_BASE}/metrics/products/top`, { params: p });
  }
  productsCategoryMix(w: MetricsWindow): Observable<CategoryMix[]> {
    return this.http.get<CategoryMix[]>(`${API_BASE}/metrics/products/category-mix`, { params: this.win(w) });
  }
  productsByEnvironment(w: MetricsWindow): Observable<EnvironmentCategory[]> {
    return this.http.get<EnvironmentCategory[]>(`${API_BASE}/metrics/products/by-environment`, { params: this.win(w) });
  }
  productsPartnerMe(w: MetricsWindow, limit = 10): Observable<ProductRank[]> {
    return this.http.get<ProductRank[]>(`${API_BASE}/metrics/products/partner/me`, { params: this.win(w).set('limit', String(limit)) });
  }
  productsPartner(identification: string, w: MetricsWindow, limit = 10): Observable<ProductRank[]> {
    return this.http.get<ProductRank[]>(`${API_BASE}/metrics/products/partner/${identification}`, { params: this.win(w).set('limit', String(limit)) });
  }
}
