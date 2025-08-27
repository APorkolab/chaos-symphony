import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { SloService, SloMetrics } from './slo.service';
import { Observable, timer, Subscription } from 'rxjs';
import { switchMap, tap } from 'rxjs/operators';

@Component({
  selector: 'app-slo',
  standalone: true,
  imports: [CommonModule, NgxChartsModule],
  templateUrl: './slo.component.html',
  styleUrls: ['./slo.component.css']
})
export class SloComponent implements OnInit, OnDestroy {

  sloMetrics$: Observable<SloMetrics>;
  metrics: SloMetrics = { p95Latency: 0, dltCount: 0, sloBurnRate1h: 0 };
  private subscription: Subscription | undefined;

  // Chart options
  colorScheme = {
    domain: ['#5AA454', '#A10A28', '#C7B42C', '#AAAAAA']
  };
  latencyReferenceLines = [{ value: 2000, name: 'SLO: 2000ms' }];

  constructor(private sloService: SloService) {
    this.sloMetrics$ = timer(0, 5000).pipe( // Poll every 5 seconds
      switchMap(() => this.sloService.getSloMetrics()),
      tap(data => this.metrics = data)
    );
  }

  ngOnInit(): void {
    this.subscription = this.sloMetrics$.subscribe();
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  get p95LatencyColor(): string {
    return this.metrics.p95Latency > 2000 ? 'text-red-500' : 'text-green-500';
  }

  get dltCountColor(): string {
    return this.metrics.dltCount > 0 ? 'text-yellow-500' : 'text-green-500';
  }

  get burnRateColor(): string {
    return this.metrics.sloBurnRate1h > 1 ? 'text-red-500' : 'text-green-500';
  }
}
