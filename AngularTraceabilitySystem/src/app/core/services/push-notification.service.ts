import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SwPush } from '@angular/service-worker';
import { API_BASE } from '../config/api.config';

@Injectable({ providedIn: 'root' })
export class PushNotificationService {

  constructor(private swPush: SwPush, private http: HttpClient) {}

  get isSupported(): boolean {
    return this.swPush.isEnabled;
  }

  subscribeToPartnerNotifications(): void {
    if (!this.swPush.isEnabled) return;

    this.http.get(`${API_BASE}/push/vapid-public-key`, { responseType: 'text' }).subscribe({
      next: (vapidKey) => {
        this.swPush.requestSubscription({ serverPublicKey: vapidKey }).then((sub) => {
          const payload = {
            endpoint: sub.endpoint,
            p256dhKey: this.arrayBufferToBase64(sub.getKey('p256dh')),
            authKey:   this.arrayBufferToBase64(sub.getKey('auth')),
          };
          this.http.post(`${API_BASE}/push/subscribe`, payload).subscribe();
        }).catch(() => {});
      },
      error: () => {},
    });
  }

  unsubscribe(): void {
    if (!this.swPush.isEnabled) return;
    this.swPush.unsubscribe().catch(() => {});
  }

  private arrayBufferToBase64(buffer: ArrayBuffer | null): string {
    if (!buffer) return '';
    return btoa(String.fromCharCode(...new Uint8Array(buffer)));
  }
}
