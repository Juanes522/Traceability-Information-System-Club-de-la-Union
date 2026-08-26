import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PartnerService } from '../../partner.service';
import { LoginHistory } from '../../../../shared/models';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';

@Component({
    selector: 'app-partner-access-log',
    templateUrl: './access-log.component.html',
    styleUrls: ['./access-log.component.scss'],
    imports: [CommonModule, PaginatorComponent],
})
export class AccessLogComponent implements OnInit {
  logins: LoginHistory[] = [];
  loading = true;
  error = '';
  currentPage = 1;
  readonly pageSize = 10;
  totalElements = 0;

  constructor(private partnerService: PartnerService) {}

  ngOnInit(): void { this.loadPage(); }

  loadPage(): void {
    this.loading = true;
    this.error = '';
    this.partnerService.getLoginHistory(this.currentPage - 1, this.pageSize).subscribe({
      next: (page) => { this.logins = page.content; this.totalElements = page.totalElements; this.loading = false; },
      error: () => { this.error = 'No se pudo cargar el historial de accesos.'; this.loading = false; },
    });
  }

  get totalPages(): number { return Math.ceil(this.totalElements / this.pageSize); }

  goToPage(n: number): void {
    if (n < 1 || n > this.totalPages || n === this.currentPage) return;
    this.currentPage = n;
    this.loadPage();
  }

  formatDateTime(dt: string): string {
    if (!dt) return '—';
    return new Date(dt).toLocaleString('es-CO', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}
