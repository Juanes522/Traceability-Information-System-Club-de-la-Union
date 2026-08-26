import { Component, OnInit } from '@angular/core';
import { PartnerProfile } from '../../../../shared/models';
import { PartnerService } from '../../partner.service';
import { NgIf, NgClass, NgFor } from '@angular/common';
import { AccionPipe } from '../../../../shared/pipes/accion.pipe';

@Component({
    selector: 'app-partner-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.scss'],
    imports: [
        NgIf,
        NgClass,
        NgFor,
        AccionPipe,
    ],
})
export class ProfileComponent implements OnInit {
  profile: PartnerProfile | null = null;
  loading = true;
  error = '';

  constructor(private partnerService: PartnerService) {}

  ngOnInit(): void {
    this.partnerService.getProfile().subscribe({
      next: (p) => { this.profile = p; this.loading = false; },
      error: () => { this.error = 'No se pudo cargar el perfil.'; this.loading = false; },
    });
  }

  fullName(p: PartnerProfile): string {
    return [p.firstName, p.secondName, p.lastName].filter(Boolean).join(' ');
  }

  get initials(): string {
    if (!this.profile) return '?';
    return [this.profile.firstName, this.profile.lastName]
      .filter(Boolean).map(w => w[0]).join('').toUpperCase();
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr + 'T00:00:00').toLocaleDateString('es-CO', {
      year: 'numeric', month: 'long', day: 'numeric',
    });
  }
}
