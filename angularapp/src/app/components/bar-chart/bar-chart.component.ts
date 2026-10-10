import { Component, Input } from '@angular/core';

export interface ChartBar {
  label: string;
  value: number;
}

@Component({
  selector: 'app-bar-chart',
  templateUrl: './bar-chart.component.html',
  styleUrls: ['./bar-chart.component.css']
})
export class BarChartComponent {
  @Input() heading = '';
  @Input() subtitle = '';
  @Input() bars: ChartBar[] = [];
  @Input() unit = 'item';
  @Input() units = 'items';
  @Input() orientation: 'rows' | 'columns' = 'rows';

  get max(): number {
    return Math.max(1, ...this.bars.map(bar => bar.value));
  }

  get total(): number {
    return this.bars.reduce((sum, bar) => sum + bar.value, 0);
  }

  size(bar: ChartBar): number {
    return (bar.value / this.max) * 100;
  }

  share(bar: ChartBar): number {
    return this.total ? Math.round((bar.value / this.total) * 100) : 0;
  }

  describe(bar: ChartBar): string {
    return `${bar.label}: ${bar.value} ${bar.value === 1 ? this.unit : this.units}, ${this.share(bar)}%`;
  }
}