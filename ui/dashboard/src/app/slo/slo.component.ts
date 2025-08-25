import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { SloService, ChartData } from './slo.service';
import { Observable, of } from 'rxjs';

@Component({
  selector: 'app-slo',
  standalone: true,
  imports: [CommonModule, NgxChartsModule],
  templateUrl: './slo.component.html',
  styleUrls: ['./slo.component.css']
})
export class SloComponent implements OnInit {

  p95LatencyData$: Observable<ChartData[]> = of([]);
  dlqCountData$: Observable<ChartData[]> = of([]);
  sloBurnRate$: Observable<number> = of(0);

  // Chart options
  view: [number, number] = [700, 300];
  legend: boolean = true;
  showXAxisLabel: boolean = true;
  showYAxisLabel: boolean = true;
  xAxisLabel: string = 'Time';
  yAxisLabelLatency: string = 'Latency (ms)';
  yAxisLabelDlq: string = 'Count';
  colorScheme = {
    domain: ['#5AA454', '#A10A28', '#C7B42C', '#AAAAAA']
  };

  // Reference line for SLO
  latencyReferenceLines = [
    { value: 2000, name: 'SLO: 2000ms' }
  ];

  constructor(private sloService: SloService) { }

  ngOnInit(): void {
    this.p95LatencyData$ = this.sloService.getP95LatencyData();
    this.dlqCountData$ = this.sloService.getDlqCountData();
    this.sloBurnRate$ = this.sloService.getSloBurnRate();
  }
}
