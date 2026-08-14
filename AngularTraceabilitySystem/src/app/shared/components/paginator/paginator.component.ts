import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';

@Component({
  selector: 'app-paginator',
  standalone: true,
  imports: [NgFor, NgIf],
  templateUrl: './paginator.component.html',
  styleUrls: ['./paginator.component.scss'],
})
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
