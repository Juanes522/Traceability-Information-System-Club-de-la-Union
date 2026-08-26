import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../../core/config/api.config';
import { PartnerProfile, Consumption, NotificationPage, ConsumptionPage, LoginHistoryPage } from '../../shared/models/index';

@Injectable()
export class PartnerService {
  constructor(private http: HttpClient) {}

  getProfile(): Observable<PartnerProfile> {
    return this.http.get<PartnerProfile>(`${API_BASE}/personpartner/me`);
  }

  getConsumptions(filters: { from?: string; to?: string; page: number; size: number }): Observable<ConsumptionPage> {
    let params = new HttpParams().set('page', String(filters.page)).set('size', String(filters.size));
    if (filters.from) { params = params.set('from', filters.from); }
    if (filters.to) { params = params.set('to', filters.to); }
    return this.http.get<ConsumptionPage>(`${API_BASE}/personpartner/getconsumptions/me`, { params });
  }

  getNotifications(page = 0, size = 10): Observable<NotificationPage> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<NotificationPage>(`${API_BASE}/personpartner/notifications/me`, { params });
  }

  getLoginHistory(page = 0, size = 10): Observable<LoginHistoryPage> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<LoginHistoryPage>(`${API_BASE}/personpartner/my-logins`, { params });
  }
}
