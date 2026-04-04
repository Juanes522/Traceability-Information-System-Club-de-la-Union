import { Component, OnInit } from '@angular/core';
import { PartnerProfile } from '../../../../shared/models';
import { PartnerService } from '../../partner.service';

@Component({
  selector: 'app-partner-dependents',
  templateUrl: './dependents.component.html',
  styleUrls: ['./dependents.component.scss'],
})
export class DependentsComponent implements OnInit {
  dependents: PartnerProfile[] = [];
  loading = true;
  error = '';

  constructor(private partnerService: PartnerService) {}

  ngOnInit(): void {
    this.partnerService.getDependents().subscribe({
      next: (d) => { this.dependents = d; this.loading = false; },
      error: (err) => {
        // 204 No Content — backend returns empty on no dependents
        if (err.status === 204) {
          this.dependents = [];
        } else {
          this.error = 'No se pudieron cargar los dependientes.';
        }
        this.loading = false;
      },
    });
  }

  fullName(p: PartnerProfile): string {
    return [p.firstName, p.secondName, p.lastName].filter(Boolean).join(' ');
  }

  getInitials(p: PartnerProfile): string {
    return [p.firstName, p.lastName].filter(Boolean).map(w => w[0]).join('').toUpperCase();
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr + 'T00:00:00').toLocaleDateString('es-CO', {
      year: 'numeric', month: 'short', day: 'numeric',
    });
  }
}
