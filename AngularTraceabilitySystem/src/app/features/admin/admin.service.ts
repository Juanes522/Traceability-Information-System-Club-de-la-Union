import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../../core/config/api.config';
import { PartnerProfile, Consumption } from '../../shared/models/index';
import { ConsumptionCreateRequest } from '../manager/manager.service';

@Injectable()
export class AdminService {
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

  getDependentsByIdentification(identification: string): Observable<PartnerProfile[]> {
    return this.http.get<PartnerProfile[]>(`${API_BASE}/personpartner/getdependents/identification/${identification}`);
  }

  getConsumptionsByIdentification(identification: string): Observable<Consumption[]> {
    return this.http.get<Consumption[]>(`${API_BASE}/personpartner/getconsumptionsidentification/${identification}`);
  }

  // ── Consumption management ─────────────────────────────────────────────────
  registerConsumption(req: ConsumptionCreateRequest): Observable<Consumption> {
    return this.http.post<Consumption>(`${API_BASE}/partnerconsumption/registerconsumption`, req);
  }

  getConsumptionsByEnvironment(env: string): Observable<Consumption[]> {
    return this.http.get<Consumption[]>(`${API_BASE}/partnerconsumption/by-environment/${encodeURIComponent(env)}`);
  }
}
