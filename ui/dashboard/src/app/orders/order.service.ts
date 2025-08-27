import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateOrderCommand, Order } from './order.model';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private orderApiUrl = '/api/orders'; // Proxied to order-api
  private replayApiUrl = '/api/replay'; // Proxied to streams-analytics

  constructor(private http: HttpClient) { }

  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.orderApiUrl);
  }

  createOrder(command: CreateOrderCommand): Observable<{ orderId: string }> {
    return this.http.post<{ orderId: string }>(this.orderApiUrl, command);
  }

  // The original file had this, which is a good feature to keep.
  // It calls the streams-analytics service.
  replayLastFiveMinutes(): Observable<void> {
    const request = {
      consumerGroupId: 'orchestrator-order-created', // This needs to be known
      duration: 'PT5M' // ISO 8601 duration format
    };
    return this.http.post<void>(this.replayApiUrl, request);
  }
}