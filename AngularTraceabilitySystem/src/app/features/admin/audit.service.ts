import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../../core/config/api.config';
import { AuditFilters, AuditPage } from '../../shared/models';

@Injectable()
export class AuditService {
  constructor(private http: HttpClient) {}

  search(filters: AuditFilters): Observable<AuditPage> {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    });
    return this.http.get<AuditPage>(`${API_BASE}/audit`, { params });
  }
}
