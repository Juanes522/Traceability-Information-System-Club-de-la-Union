import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';

@Component({
  selector: 'app-paginator',
  standalone: true,
  imports: [NgFor, NgIf],
  templateUrl: './paginator.component.html',
  styleUrls: ['./paginator.component.scss'],
})
/**
 * Control de paginación reutilizable, con elipsis para rangos largos.
 *
 * @remarks
 * Es puramente de presentación: no conoce el origen de los datos ni pide nada. Recibe la página actual y el total, y emite
 * la página solicitada; quien decide si eso se resuelve en el cliente o en el servidor es el componente contenedor.
 *
 * Numera las páginas **desde uno**, que es lo natural para el usuario. Los componentes que lo usan se encargan de convertir
 * a la numeración desde cero que espera el backend, conversión que aparece repetida en cada uno de ellos.
 *
 * Cuando hay pocas páginas las muestra todas; a partir de cierto número condensa el rango dejando la primera, la última y
 * el entorno de la actual, con elipsis entre medias. Se oculta por completo si solo hay una página.
 *
 * Es el componente mejor factorizado y mejor probado de la aplicación.
 */
export class PaginatorComponent {
  @Input() currentPage = 1;
  @Input() totalPages = 1;
  @Output() pageChange = new EventEmitter<number>();

  get pages(): (number | '…')[] {
    const total = this.totalPages;
    if (total <= 7) {
      return Array.from({ length: total }, (_, i) => i + 1);
    }
    const cur = this.currentPage;
    const nums = Array.from(new Set([1, cur - 1, cur, cur + 1, total]))
      .filter(n => n >= 1 && n <= total)
      .sort((a, b) => a - b);
    const out: (number | '…')[] = [];
    let prev = 0;
    for (const n of nums) {
      if (n - prev > 1) { out.push('…'); }
      out.push(n);
      prev = n;
    }
    return out;
  }

  isNumber(p: number | '…'): p is number {
    return p !== '…';
  }

  go(p: number | '…'): void {
    if (p === '…' || p === this.currentPage || p < 1 || p > this.totalPages) {
      return;
    }
    this.pageChange.emit(p);
  }

  prev(): void {
    if (this.currentPage > 1) { this.pageChange.emit(this.currentPage - 1); }
  }

  next(): void {
    if (this.currentPage < this.totalPages) { this.pageChange.emit(this.currentPage + 1); }
  }
}
