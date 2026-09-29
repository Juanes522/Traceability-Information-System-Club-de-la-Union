import { Component } from '@angular/core';
import { Observable } from 'rxjs';
import { Toast, ToastService } from '../../../core/services/toast.service';
import { NgFor, NgClass, AsyncPipe } from '@angular/common';

@Component({
    selector: 'app-toast',
    templateUrl: './toast.component.html',
    styleUrls: ['./toast.component.scss'],
    imports: [
        NgFor,
        NgClass,
        AsyncPipe,
    ],
})
/**
 * Muestra la pila de avisos efímeros.
 *
 * @remarks
 * Se declara una sola vez en el componente raíz, **fuera del área de enrutamiento**, de modo que un aviso sobrevive a la
 * navegación en lugar de desaparecer con la pantalla que lo originó.
 *
 * No tiene entradas ni salidas: se limita a observar la cola del servicio de avisos.
 *
 * Carece de anotaciones de región activa para accesibilidad, de modo que los lectores de pantalla no anuncian los avisos.
 */
export class ToastComponent {
  toasts$: Observable<Toast[]>;

  constructor(private toastService: ToastService) {
    this.toasts$ = this.toastService.toasts$;
  }

  dismiss(id: number): void {
    this.toastService.dismiss(id);
  }

  trackById(_: number, t: Toast): number {
    return t.id;
  }
}
