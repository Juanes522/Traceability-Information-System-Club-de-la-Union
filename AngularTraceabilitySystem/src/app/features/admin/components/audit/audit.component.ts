import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription, interval } from 'rxjs';
import { AuditService } from '../../audit.service';
import { AuditEvent, AuditFilters } from '../../../../shared/models';
import { PaginatorComponent } from '../../../../shared/components/paginator/paginator.component';

type ActivePreset = 'today' | 'week' | 'month' | 'all' | 'custom';

@Component({
  selector: 'app-admin-audit',
  standalone: true,
  imports: [CommonModule, FormsModule, PaginatorComponent],
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.scss'],
})
export class AuditComponent implements OnInit, OnDestroy {
  filters: AuditFilters = { page: 0, size: 10 };
  events: AuditEvent[] = [];
  totalElements = 0;
  loading = false;
  error = false;
  rangeError = false;

  autoRefresh = true;
  lastUpdated: Date | null = null;
  readonly REFRESH_MS = 30000;

  private activePreset: ActivePreset = 'week';
  private refreshSub?: Subscription;

  private readonly MAX_RANGE_DAYS = 92;

  readonly eventTypes = [
    'LOGIN_SUCCESS', 'LOGIN_FAILED', 'RATE_LIMIT_BLOCK', 'LOGOUT', 'TOKEN_REVOKED',
    'PASSWORD_CHANGED', 'PASSWORD_RESET_REQUESTED', 'PASSWORD_RESET', 'ACCESS_DENIED', 'CHARGE_REGISTERED',
  ];
  readonly results = ['SUCCESS', 'FAILURE'];

  constructor(private auditService: AuditService) {}

  ngOnInit(): void {
    this.lastWeek();
    this.refreshSub = interval(this.REFRESH_MS).subscribe(() => this.onTick());
  }

  ngOnDestroy(): void {
    this.refreshSub?.unsubscribe();
  }

  today(): void {
    this.applyPreset('today', true);
  }

  lastWeek(): void {
    this.applyPreset('week', true);
  }

  lastMonth(): void {
    this.applyPreset('month', true);
  }

  allTime(): void {
    this.applyPreset('all', true);
  }

  search(): void {
    this.activePreset = 'custom';
    this.filters.page = 0;
    this.load();
  }

  refreshNow(): void {
    if (this.activePreset === 'custom') {
      this.load(true);
    } else {
      this.applyPreset(this.activePreset, true);
    }
  }

  toggleAutoRefresh(): void {
    this.autoRefresh = !this.autoRefresh;
  }

  onTick(): void {
    if (!this.canAutoRefresh()) {
      return;
    }
    this.applyPreset(this.activePreset, false);
  }

  canAutoRefresh(): boolean {
    return this.autoRefresh && (this.filters.page ?? 0) === 0 && this.activePreset !== 'custom';
  }

  goToPage(n: number): void {
    const target = n - 1;
    if (target >= 0 && target < this.totalPages && target !== (this.filters.page ?? 0)) {
      this.filters.page = target;
      this.load();
    }
  }

  get totalPages(): number {
    const size = this.filters.size ?? 10;
    return Math.max(1, Math.ceil(this.totalElements / size));
  }

  load(showSpinner = true): void {
    if (!this.isRangeValid()) {
      this.rangeError = true;
      return;
    }
    this.rangeError = false;
    if (showSpinner) {
      this.loading = true;
    }
    this.error = false;
    this.auditService.search(this.toQuery()).subscribe({
      next: (page) => {
        this.events = page.content;
        this.totalElements = page.totalElements;
        this.loading = false;
        this.lastUpdated = new Date();
      },
      error: () => {
        this.events = [];
        this.error = true;
        this.loading = false;
      },
    });
  }

  private applyPreset(preset: ActivePreset, showSpinner: boolean): void {
    this.activePreset = preset;
    if (preset === 'all') {
      this.filters.from = undefined;
      this.filters.to = undefined;
    } else if (preset !== 'custom') {
      const now = new Date();
      let from: Date;
      if (preset === 'today') {
        from = new Date(now);
        from.setHours(0, 0, 0, 0);
      } else if (preset === 'week') {
        from = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
      } else {
        from = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
      }
      this.filters.from = this.toLocalInput(from);
      this.filters.to = this.toLocalInput(now);
    }
    this.filters.page = 0;
    this.load(showSpinner);
  }

  private isRangeValid(): boolean {
    if (!this.filters.from || !this.filters.to) {
      return true;
    }
    const ms = new Date(this.filters.to).getTime() - new Date(this.filters.from).getTime();
    return ms <= this.MAX_RANGE_DAYS * 24 * 60 * 60 * 1000;
  }

  private toLocalInput(d: Date): string {
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  private toQuery(): AuditFilters {
    const query: AuditFilters = { ...this.filters };
    if (query.from) {
      query.from = new Date(query.from).toISOString();
    }
    if (query.to) {
      query.to = new Date(query.to).toISOString();
    }
    return query;
  }
}
