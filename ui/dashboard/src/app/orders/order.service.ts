import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Order, OrderHistory } from './order.model';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = 'http://localhost:8080/api/orders'; // Base URL for the order-api

  constructor(private http: HttpClient) { }

  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.apiUrl);
  }

  getOrderHistory(id: string): Observable<OrderHistory> {
    const historyUrl = `http://localhost:8095/api/orders/${id}/history`;
    return this.http.get<OrderHistory>(historyUrl);
  }

  createOrder(amount: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/start?amount=${amount}`, {});
  }

  replayLastFiveMinutes(): Observable<any> {
    const request = {
      consumerGroupId: 'orchestrator-order-created',
      duration: '5m'
    };
    // Note: This calls a different service (streams-analytics)
    return this.http.post('http://localhost:8095/api/replay', request);
  }
}