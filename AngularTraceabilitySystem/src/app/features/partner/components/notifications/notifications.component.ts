import { Component, OnInit } from '@angular/core';
import { NotificationDTO } from '../../../../shared/models';
import { PartnerService } from '../../partner.service';
import { NgIf, NgFor } from '@angular/common';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';

@Component({
    selector: 'app-partner-notifications',
    templateUrl: './notifications.component.html',
    styleUrls: ['./notifications.component.scss'],
    imports: [NgIf, NgFor, PaginatorComponent],
})
export class NotificationsComponent implements OnInit {
  notifications: NotificationDTO[] = [];
  loading = true;
  error = '';
  currentPage = 1;
  pageSize = 10;
  totalElements = 0;

  constructor(private partnerService: PartnerService) {}

  ngOnInit(): void {
    this.loadPage();
  }

  get totalPages(): number { return Math.ceil(this.totalElements / this.pageSize); }

  goToPage(n: number): void {
    if (n < 1 || n > this.totalPages || n === this.currentPage) return;
    this.currentPage = n;
    this.loadPage();
  }

  loadPage(): void {
    this.loading = true;
    this.error = '';
    this.partnerService.getNotifications(this.currentPage - 1, this.pageSize).subscribe({
      next: (page) => {
        this.notifications = page.content;
        this.totalElements = page.totalElements;
        this.loading = false;
      },
      error: () => {
        this.error = 'No se pudieron cargar las notificaciones.';
        this.loading = false;
      },
    });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency', currency: 'USD', minimumFractionDigits: 2, maximumFractionDigits: 2,
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
