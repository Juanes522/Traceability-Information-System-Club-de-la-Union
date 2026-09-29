import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SwPush } from '@angular/service-worker';
import { API_BASE } from '../config/api.config';

/**
 * Gestiona la suscripción del navegador a las notificaciones push.
 *
 * @remarks
 * El alta requiere tres pasos encadenados: obtener la clave pública del servidor, pedir la suscripción al navegador
 * —que solicitará permiso al usuario— y registrarla en el backend.
 *
 * Solo se activa para los socios, desde el contenedor principal, porque son los únicos destinatarios de avisos de consumo.
 *
 * **Todos sus caminos de error están silenciados**, de modo que una suscripción fallida es invisible tanto para el usuario
 * como para el desarrollador. Y el método de baja ni se invoca desde ninguna parte ni llama al backend: se limita al
 * navegador, dejando huérfano el registro del servidor.
 */
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
