import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../../core/config/api.config';
import { AuditFilters, AuditPage } from '../../shared/models';

/**
 * Consulta la bitácora de auditoría del backend.
 *
 * @remarks
 * Construye los parámetros recorriendo el objeto de filtros y descartando los vacíos, de modo que una consulta sin filtros
 * devuelve la bitácora completa paginada.
 *
 * Esa construcción genérica tiene una contrapartida: **no valida qué claves acepta el backend**, así que un nombre de filtro
 * mal escrito se envía igualmente y el servidor lo ignora en silencio, devolviendo más resultados de los esperados sin señalar
 * ningún error.
 */
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
