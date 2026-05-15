import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../../core/config/api.config';
import { PartnerProfile, Consumption, NotificationDTO } from '../../shared/models/index';

@Injectable()
export class PartnerService {
  constructor(private http: HttpClient) {}

  getProfile(): Observable<PartnerProfile> {
    return this.http.get<PartnerProfile>(`${API_BASE}/personpartner/me`);
  }

  getConsumptions(): Observable<Consumption[]> {
    return this.http.get<Consumption[]>(`${API_BASE}/personpartner/getconsumptions/me`);
  }

  getNotifications(): Observable<NotificationDTO[]> {
    return this.http.get<NotificationDTO[]>(`${API_BASE}/personpartner/notifications/me`);
  }
}
