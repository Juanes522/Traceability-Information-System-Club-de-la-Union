import { Component, OnInit } from '@angular/core';
import { NotificationDTO } from '../../../../shared/models';
import { PartnerService } from '../../partner.service';
import { NgIf, NgFor } from '@angular/common';

@Component({
    selector: 'app-partner-notifications',
    templateUrl: './notifications.component.html',
    styleUrls: ['./notifications.component.scss'],
    imports: [NgIf, NgFor],
})
export class NotificationsComponent implements OnInit {
  notifications: NotificationDTO[] = [];
  loading = true;
  error = '';

  constructor(private partnerService: PartnerService) {}

  ngOnInit(): void {
    this.partnerService.getNotifications().subscribe({
      next: (list) => {
        this.notifications = list;
        this.loading = false;
      },
      error: (err) => {
        if (err.status === 204) {
          this.notifications = [];
        } else {
          this.error = 'No se pudieron cargar las notificaciones.';
        }
        this.loading = false;
      },
    });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency', currency: 'COP', maximumFractionDigits: 0,
    }).format(amount);
  }

  formatDateTime(dt: string): string {
    if (!dt) return '—';
    return new Date(dt).toLocaleString('es-CO', {
      year: 'numeric', month: 'short', day: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  }
}
