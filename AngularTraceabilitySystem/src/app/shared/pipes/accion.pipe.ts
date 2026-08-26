import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'accion', standalone: true })
export class AccionPipe implements PipeTransform {
  transform(value: number | null | undefined): string {
    return value == null ? '' : String(value).padStart(6, '0');
  }
}
