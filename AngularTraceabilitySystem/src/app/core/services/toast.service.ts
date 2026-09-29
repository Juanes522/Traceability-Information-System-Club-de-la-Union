import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error';
}

/**
 * Cola de avisos efímeros que se muestran en una esquina de la pantalla.
 *
 * @remarks
 * Cada aviso se descarta solo transcurridos unos segundos. Solo existen dos variantes, éxito y error, lo que obliga a
 * encajar en una de ellas mensajes que no son ninguna de las dos —el cierre de sesión por inactividad se anuncia como
 * éxito.
 *
 * **Defecto conocido:** el identificador de cada aviso se deriva del instante de creación, de modo que dos avisos generados
 * en el mismo milisegundo comparten identificador; como el descarte filtra por ese valor, cerrar uno cierra ambos.
 *
 * Es el único servicio de esta carpeta que declara aquí su propio tipo en lugar de hacerlo en el módulo de modelos
 * compartidos.
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  toasts$ = this.toastsSubject.asObservable();

  success(message: string): void { this.add(message, 'success'); }
  error(message: string): void   { this.add(message, 'error'); }

  private add(message: string, type: 'success' | 'error'): void {
    const id = Date.now();
    this.toastsSubject.next([...this.toastsSubject.value, { id, message, type }]);
    setTimeout(() => this.dismiss(id), 4500);
  }

  dismiss(id: number): void {
    this.toastsSubject.next(this.toastsSubject.value.filter((t) => t.id !== id));
  }
}
