import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SloMetrics {
  p95Latency: number;
  dltCount: number;
  sloBurnRate1h: number;
}

@Injectable({
  providedIn: 'root'
})
export class SloService {
  private apiUrl = 'http://localhost:8095/api/metrics/slo'; // streams-analytics service

  constructor(private http: HttpClient) { }

  getSloMetrics(): Observable<SloMetrics> {
    return this.http.get<SloMetrics>(this.apiUrl);
  }
}
