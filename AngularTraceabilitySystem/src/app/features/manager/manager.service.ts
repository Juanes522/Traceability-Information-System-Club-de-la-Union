import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../../core/config/api.config';
import { Consumption, PartnerProfile, ConsumptionPage } from '../../shared/models/index';

export interface ConsumptionCreateRequest {
  partnerId: number;
  enviroment: string;
  account: number;
  table: string;
  waiterName: string;
  isPartner: string;
  consumptionValue: number;
  iva: number;
  service: number;
  tip: number;
  consumptionOpening: string; // YYYY-MM-DD
  consumptionClosing: string | null; // YYYY-MM-DD
}

@Injectable()
export class ManagerService {
  constructor(private http: HttpClient) {}

  // ── Partner search ─────────────────────────────────────────────────────────
  getAllPartners(): Observable<PartnerProfile[]> {
    return this.http.get<PartnerProfile[]>(`${API_BASE}/personpartner/getall`);
  }

  /** Returns a SINGLE object, not an array */
  searchByIdentification(identification: string): Observable<PartnerProfile> {
    return this.http.get<PartnerProfile>(`${API_BASE}/personpartner/getbyidentification/${identification}`);
  }

  /** Returns array: titular + dependents sharing that share number */
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
