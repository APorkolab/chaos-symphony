import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

// ngx-charts data format
export interface ChartData {
  name: string;
  series: {
    name: string; // Typically a date/time string
    value: number;
  }[];
}

@Injectable({
  providedIn: 'root'
})
export class SloService {

  constructor() { }

  // Mock data for p95 latency chart
  getP95LatencyData(): Observable<ChartData[]> {
    const data: ChartData[] = [
      {
        name: 'p95 Latency (ms)',
        series: [
          { name: '10:00', value: 850 },
          { name: '10:05', value: 900 },
          { name: '10:10', value: 880 },
          { name: '10:15', value: 1200 },
          { name: '10:20', value: 1800 },
          { name: '10:25', value: 2100 }, // SLO violation
          { name: '10:30', value: 1500 },
        ]
      }
    ];
    return of(data);
  }

  // Mock data for DLQ count chart
  getDlqCountData(): Observable<ChartData[]> {
    const data: ChartData[] = [
      {
        name: 'DLQ Messages',
        series: [
          { name: '10:00', value: 0 },
          { name: '10:05', value: 0 },
          { name: '10:10', value: 1 },
          { name: '10:15', value: 3 },
          { name: '10:20', value: 8 },
          { name: '10:25', value: 15 },
          { name: '10:30', value: 16 },
        ]
      }
    ];
    return of(data);
  }

  // Mock data for SLO burn rate
  getSloBurnRate(): Observable<number> {
    // e.g., 1.5 means we are burning through our error budget 1.5x faster than allowed
    return of(1.5);
  }
}
