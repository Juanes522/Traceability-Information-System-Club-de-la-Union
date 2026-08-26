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
