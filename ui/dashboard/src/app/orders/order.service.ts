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
    // TODO: A real implementation would fetch this from a dedicated history endpoint.
    // For now, we simulate a realistic history timeline on the frontend
    // to demonstrate the UI component's functionality.
    return this.http.get<Order>(`${this.apiUrl}/${id}`).pipe(
      map(order => {
        const historyEvents = [];
        const createdTime = new Date(order.createdAt);

        historyEvents.push({ status: OrderStatus.NEW, timestamp: order.createdAt });

        if ([OrderStatus.PAID, OrderStatus.ALLOCATED, OrderStatus.SHIPPED].includes(order.status)) {
          historyEvents.push({ status: OrderStatus.PAID, timestamp: new Date(createdTime.getTime() + 1000).toISOString() });
        }
        if ([OrderStatus.ALLOCATED, OrderStatus.SHIPPED].includes(order.status)) {
          historyEvents.push({ status: OrderStatus.ALLOCATED, timestamp: new Date(createdTime.getTime() + 2000).toISOString() });
        }
        if (order.status === OrderStatus.SHIPPED) {
          historyEvents.push({ status: OrderStatus.SHIPPED, timestamp: new Date(createdTime.getTime() + 3000).toISOString() });
        }
        if (order.status === OrderStatus.FAILED) {
            historyEvents.push({ status: OrderStatus.FAILED, timestamp: new Date(createdTime.getTime() + 4000).toISOString() });
        }

        const history: OrderHistory = {
          ...order,
          history: historyEvents.reverse(), // Show most recent first
        };
        return history;
      })
    );
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