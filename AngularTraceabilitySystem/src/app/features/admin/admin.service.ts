import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../../core/config/api.config';
import { PartnerProfile, Consumption, PartnerPage, ConsumptionPage } from '../../shared/models/index';
import { ConsumptionCreateRequest } from '../manager/manager.service';

@Injectable()
export class AdminService {
  constructor(private http: HttpClient) {}

  // ── Partner search ─────────────────────────────────────────────────────────
  getAllPartners(page: number, size: number): Observable<PartnerPage> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<PartnerPage>(`${API_BASE}/personpartner/getallpaged`, { params });
  }

  searchByIdentification(identification: string): Observable<PartnerProfile> {
    return this.http.get<PartnerProfile>(`${API_BASE}/personpartner/getbyidentification/${identification}`);
  }

  searchByShareNumber(shareNumber: string): Observable<PartnerProfile[]> {
    return this.http.get<PartnerProfile[]>(`${API_BASE}/personpartner/getbysharenumber/${shareNumber}`);
  }

  searchByFirstName(name: string): Observable<PartnerProfile[]> {
    return this.http.get<PartnerProfile[]>(`${API_BASE}/personpartner/getbyfirstname/${name}`);
  }

  searchBySecondName(name: string): Observable<PartnerProfile[]> {
    return this.http.get<PartnerProfile[]>(`${API_BASE}/personpartner/getbysecondname/${name}`);
  }

  getConsumptionsByIdentification(identification: string, filters: { from?: string; to?: string; page: number; size: number }): Observable<ConsumptionPage> {
    let params = new HttpParams().set('page', String(filters.page)).set('size', String(filters.size));
    if (filters.from) { params = params.set('from', filters.from); }
    if (filters.to) { params = params.set('to', filters.to); }
    return this.http.get<ConsumptionPage>(`${API_BASE}/personpartner/getconsumptionsidentification/${identification}`, { params });
  }

  // ── Consumption management ─────────────────────────────────────────────────
  registerConsumption(req: ConsumptionCreateRequest): Observable<Consumption> {
    return this.http.post<Consumption>(`${API_BASE}/partnerconsumption/registerconsumption`, req);
  }

  getConsumptionsByEnvironment(env: string, filters: { from?: string; to?: string; page: number; size: number }): Observable<ConsumptionPage> {
    let params = new HttpParams().set('page', String(filters.page)).set('size', String(filters.size));
    if (filters.from) { params = params.set('from', filters.from); }
    if (filters.to) { params = params.set('to', filters.to); }
    return this.http.get<ConsumptionPage>(`${API_BASE}/partnerconsumption/by-environment/${encodeURIComponent(env)}`, { params });
  }
}
