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
    // We get the current state and build a "partial" history from it.
    // A real implementation would have a dedicated history endpoint.
    return this.http.get<Order>(`${this.apiUrl}/${id}`).pipe(
      map(order => {
        // Create a synthetic history based on current status
        const history: OrderHistory = {
          ...order,
          history: [
            { status: order.status, timestamp: order.createdAt } // Simplified
          ]
        };
        return history;
      })
    );
  }

  createOrder(amount: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/start?amount=${amount}`, {});
  }
}