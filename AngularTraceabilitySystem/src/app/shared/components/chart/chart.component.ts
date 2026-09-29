import {
  AfterViewInit, Component, ElementRef, Input, OnChanges, OnDestroy, ViewChild,
} from '@angular/core';
import * as echarts from 'echarts';
import type { EChartsOption } from 'echarts';

@Component({
  selector: 'app-chart',
  standalone: true,
  template: `<div #host [style.height]="height" class="chart-host"></div>`,
  styles: [`.chart-host { width: 100%; } :host { display: block; }`],
})
/**
 * Envuelve la biblioteca de gráficas en un componente reutilizable.
 *
 * @remarks
 * Aísla en un solo punto toda la interacción con la biblioteca: es el **único** archivo de la aplicación que la importa, de
 * modo que sustituirla afectaría solo a este componente.
 *
 * Recibe la configuración ya construida y se limita a aplicarla, sin conocer el dominio. Quienes la construyen son los
 * componentes de tablero, que son los que sí saben qué representa cada serie.
 *
 * Gestiona el ciclo de vida completo de la instancia, incluida su liberación al destruirse, y la redimensiona al cambiar el
 * tamaño de la ventana. Nótese que vigila la ventana y no el contenedor, de modo que un cambio de ancho provocado por
 * plegar la barra lateral **no** redimensiona la gráfica.
 *
 * La biblioteca se importa completa, sin selección de módulos, y eso es lo que más presiona el límite de tamaño del paquete
 * inicial.
 */
export class ChartComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input() options: EChartsOption | null = null;
  @Input() height = '320px';
  @ViewChild('host', { static: true }) host!: ElementRef<HTMLDivElement>;

  private chart?: echarts.ECharts;
  private readonly resizeHandler = () => this.chart?.resize();

  ngAfterViewInit(): void {
    this.chart = echarts.init(this.host.nativeElement, undefined, { renderer: 'canvas' });
    if (this.options) {
      this.chart.setOption(this.options);
    }
    window.addEventListener('resize', this.resizeHandler);
  }

  ngOnChanges(): void {
    if (this.chart && this.options) {
      this.chart.setOption(this.options, true);
    }
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.resizeHandler);
    this.chart?.dispose();
  }
}
