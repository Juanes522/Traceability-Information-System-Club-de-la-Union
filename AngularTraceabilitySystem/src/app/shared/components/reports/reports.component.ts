import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportService } from '../../services/report.service';

type ReportType = 'consumptions' | 'income' | 'partner' | 'security';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.scss'],
})
export class ReportsComponent implements OnInit {
  @Input() showSecurity = false;

  reportType: ReportType = 'consumptions';
  from?: string;
  to?: string;
  environment = '';
  identification = '';
  partnerBy: 'identification' | 'shareNumber' = 'identification';
  downloading = false;
  error = '';
  private readonly MAX_RANGE_DAYS = 366;

  constructor(private reports: ReportService) {}

  ngOnInit(): void {
    this.lastMonth();
  }

  today(): void {
    const now = new Date();
    const start = new Date(now);
    start.setHours(0, 0, 0, 0);
    this.from = this.fmt(start);
    this.to = this.fmt(now);
  }
  lastWeek(): void {
    const now = new Date();
    this.from = this.fmt(new Date(now.getTime() - 7 * 864e5));
    this.to = this.fmt(now);
  }
  lastMonth(): void {
    const now = new Date();
    this.from = this.fmt(new Date(now.getTime() - 30 * 864e5));
    this.to = this.fmt(now);
  }

  download(): void {
    if (!this.from || !this.to) {
      this.error = 'Selecciona el periodo (desde y hasta).';
      return;
    }
    if (!this.isRangeValid()) {
      this.error = 'El rango no puede superar 1 año ni empezar después de la fecha final.';
      return;
    }
    this.error = '';
    this.downloading = true;
    const w = { from: this.from, to: this.to };
    const done = () => { this.downloading = false; };
    const fail = (label: string) => (err: any) => {
      this.downloading = false;
      this.error = err?.status === 404 ? 'Socio no encontrado.' : `No se pudo generar el reporte de ${label}.`;
    };

    if (this.reportType === 'consumptions') {
      this.reports.consumptions(w, this.environment).subscribe({
        next: b => { ReportService.download(b, `consumos-${this.stamp()}.pdf`); done(); },
        error: fail('consumos'),
      });
    } else if (this.reportType === 'income') {
      this.reports.incomeByEnvironment(w).subscribe({
        next: b => { ReportService.download(b, `ingresos-ambiente-${this.stamp()}.pdf`); done(); },
        error: fail('ingresos'),
      });
    } else if (this.reportType === 'partner') {
      this.reports.partnerStatement(this.identification, w, this.partnerBy).subscribe({
        next: b => { ReportService.download(b, `estado-cuenta-${this.identification}.pdf`); done(); },
        error: fail('estado de cuenta'),
      });
    } else {
      this.reports.security(w).subscribe({
        next: b => { ReportService.download(b, `seguridad-${this.stamp()}.pdf`); done(); },
        error: fail('seguridad'),
      });
    }
  }

  private isRangeValid(): boolean {
    if (!this.from || !this.to) {
      return false;
    }
    const f = new Date(this.from).getTime();
    const t = new Date(this.to).getTime();
    if (t < f) {
      return false;
    }
    return (t - f) <= this.MAX_RANGE_DAYS * 24 * 60 * 60 * 1000;
  }

  get needsEnvironment(): boolean { return this.reportType === 'consumptions'; }
  get needsIdentification(): boolean { return this.reportType === 'partner'; }
  get canDownload(): boolean {
    if (this.downloading) { return false; }
    if (!this.from || !this.to) { return false; }
    if (this.reportType === 'partner') { return this.identification.trim().length > 0; }
    return true;
  }

  private fmt(d: Date): string {
    const p = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
  }
  private stamp(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
