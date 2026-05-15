import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ManagerService } from '../../manager.service';
import { ToastService } from '../../../../core/services/toast.service';
import { PartnerProfile } from '../../../../shared/models';
import { NgIf } from '@angular/common';

@Component({
    selector: 'app-manager-register-consumption',
    templateUrl: './register-consumption.component.html',
    styleUrls: ['./register-consumption.component.scss'],
    imports: [
        NgIf,
        ReactiveFormsModule,
        FormsModule,
    ],
})
export class RegisterConsumptionComponent {
  // Step 1 — find partner
  identificationInput = '';
  partnerFound: PartnerProfile | null = null;
  searchingPartner = false;
  partnerError = '';

  // Step 2 — consumption form
  form: FormGroup;
  submitting = false;

  constructor(
    private managerService: ManagerService,
    private toast: ToastService,
    private fb: FormBuilder,
  ) {
    const today = new Date().toISOString().split('T')[0];
    this.form = this.fb.group({
      enviroment:         ['', Validators.required],
      table:              ['', Validators.required],
      waiterName:         ['', Validators.required],
      consumptionValue:   [null, [Validators.required, Validators.min(0.01)]],
      iva:                [null, [Validators.required, Validators.min(0)]],
      service:            [null, [Validators.required, Validators.min(0)]],
      tip:                [0,    [Validators.required, Validators.min(0)]],
      consumptionOpening: [today, Validators.required],
      consumptionClosing: [null],
    });
  }

  findPartner(): void {
    if (!this.identificationInput.trim()) return;
    this.searchingPartner = true;
    this.partnerError = '';
    this.partnerFound = null;

    this.managerService.searchByIdentification(this.identificationInput.trim()).pipe(
      map(p => p),
      catchError(err => {
        this.partnerError = err.status === 404
          ? 'No se encontró ningún socio con esa cédula.'
          : 'Error al buscar el socio. Verifica la conexión.';
        return of(null);
      })
    ).subscribe(p => {
      this.partnerFound = p;
      this.searchingPartner = false;
    });
  }

  clearPartner(): void {
    this.partnerFound = null;
    this.identificationInput = '';
    this.partnerError = '';
    this.form.reset({ tip: 0, consumptionOpening: new Date().toISOString().split('T')[0], consumptionClosing: null });
  }

  fullName(p: PartnerProfile): string {
    return [p.firstName, p.secondName, p.lastName].filter(Boolean).join(' ');
  }

  get total(): number {
    const v = this.form.value;
    return (v.consumptionValue || 0) + (v.iva || 0) + (v.service || 0) + (v.tip || 0);
  }

  formatCurrency(v: number): string {
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(v);
  }

  submit(): void {
    if (this.form.invalid || !this.partnerFound) return;
    this.submitting = true;

    const req = {
      partnerId:          this.partnerFound.personId,
      account:            this.partnerFound.shareNumber,
      isPartner:          'S',
      ...this.form.value,
    };

    this.managerService.registerConsumption(req).pipe(
      catchError(err => {
        const msg = err?.error?.message ?? 'No se pudo registrar el consumo. Inténtalo de nuevo.';
        this.toast.error(msg);
        this.submitting = false;
        return of(null);
      })
    ).subscribe(result => {
      if (result !== null) {
        this.toast.success('Consumo registrado correctamente.');
        this.clearPartner();
        this.submitting = false;
      }
    });
  }
}
