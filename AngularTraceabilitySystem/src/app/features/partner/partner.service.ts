import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../../core/config/api.config';
import { PartnerProfile, Consumption, NotificationPage, ConsumptionPage, LoginHistoryPage } from '../../shared/models/index';

/**
 * Acceso a los datos propios del socio autenticado.
 *
 * @remarks
 * **Todos sus métodos consultan endpoints de datos propios**, sin enviar ningún identificador: la identidad la resuelve el
 * backend desde la sesión. Esa propiedad hace imposible por construcción que un socio acceda a datos de otro.
 *
 * Tiene una consecuencia que conviene conocer: al remontarse este módulo bajo la rama de gestor, ese atajo muestra al gestor
 * **sus propios** datos de socio, no los de nadie más.
 *
 * Los parámetros de fecha solo se envían cuando tienen valor, de modo que una ventana vacía consulta todo el histórico.
 */
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
