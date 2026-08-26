import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../../core/config/api.config';
import { MetricsWindow } from '../models';

@Injectable({ providedIn: 'root' })
export class ReportService {
  constructor(private http: HttpClient) {}

  private win(w: MetricsWindow): HttpParams {
    let p = new HttpParams();
    if (w.from) { p = p.set('from', w.from); }
    if (w.to) { p = p.set('to', w.to); }
    return p;
  }

  consumptions(w: MetricsWindow, environment?: string): Observable<Blob> {
    let p = this.win(w);
    if (environment) { p = p.set('environment', environment); }
    return this.http.get(`${API_BASE}/reports/consumptions`, { params: p, responseType: 'blob' });
  }

  incomeByEnvironment(w: MetricsWindow): Observable<Blob> {
    return this.http.get(`${API_BASE}/reports/income-by-environment`, { params: this.win(w), responseType: 'blob' });
  }

  partnerStatement(value: string, w: MetricsWindow, by: 'identification' | 'shareNumber' = 'identification'): Observable<Blob> {
    return this.http.get(`${API_BASE}/reports/partner-statement`, { params: this.win(w).set(by, value), responseType: 'blob' });
  }

  security(w: MetricsWindow): Observable<Blob> {
    return this.http.get(`${API_BASE}/reports/security`, { params: this.win(w), responseType: 'blob' });
  }

  static download(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
}
