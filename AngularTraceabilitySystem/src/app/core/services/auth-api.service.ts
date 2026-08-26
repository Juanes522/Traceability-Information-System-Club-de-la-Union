import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../config/api.config';
import { UserSession, ChangePasswordRequest, ForgotPasswordRequest, ResetPasswordRequest, ConsentPolicy } from '../../shared/models';

@Injectable()
export class AuthApiService {
  constructor(private http: HttpClient) {}

  login(credentials: { identification: string; password: string }): Observable<UserSession> {
    return this.http.post<UserSession>(`${API_BASE}/auth/login`, credentials);
  }

  logout(reason?: string): Observable<string> {
    const options: { responseType: 'text'; params?: HttpParams } = { responseType: 'text' };
    if (reason) { options.params = new HttpParams().set('reason', reason); }
    return this.http.post(`${API_BASE}/auth/logout`, null, options as any) as unknown as Observable<string>;
  }

  changePassword(req: ChangePasswordRequest): Observable<void> {
    return this.http.post(`${API_BASE}/auth/change-password`, req, {
      responseType: 'text' as 'json',
    }) as unknown as Observable<void>;
  }

  forgotPassword(req: ForgotPasswordRequest): Observable<string> {
    return this.http.post(`${API_BASE}/auth/forgot-password`, req, {
      responseType: 'text' as 'json',
    }) as unknown as Observable<string>;
  }

  resetPassword(req: ResetPasswordRequest): Observable<string> {
    return this.http.post(`${API_BASE}/auth/reset-password`, req, {
      responseType: 'text' as 'json',
    }) as unknown as Observable<string>;
  }

  acceptConsent(): Observable<void> {
    return this.http.post(`${API_BASE}/auth/accept-consent`, null, {
      responseType: 'text' as 'json',
    }) as unknown as Observable<void>;
  }

  getConsentPolicy(): Observable<ConsentPolicy> {
    return this.http.get<ConsentPolicy>(`${API_BASE}/auth/consent`);
  }
}
