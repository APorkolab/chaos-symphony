import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Order, OrderHistory } from './order.model';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = 'http://localhost:8080/api/orders'; // Assuming this is the base URL for the order-api

  constructor(private http: HttpClient) { }

  // Method to get all orders (will be needed for the table view)
  getOrders(): Observable<Order[]> {
    // For now, returning mock data until the backend endpoint is ready
    return of([
      { id: '123-abc', status: 'SHIPPED', total: 1500, createdAt: new Date().toISOString() },
      { id: '456-def', status: 'PAID', total: 2500, createdAt: new Date().toISOString() },
    ]);
    // return this.http.get<Order[]>(this.apiUrl);
  }

  // Method to get a single order with its history (for the timeline view)
  getOrderHistory(id: string): Observable<OrderHistory> {
    // Mock data for the timeline
    const mockHistory: OrderHistory = {
      id: id,
      status: 'SHIPPED',
      total: 1500,
      createdAt: new Date().toISOString(),
      history: [
        { status: 'NEW', timestamp: new Date(Date.now() - 30000).toISOString() },
        { status: 'PAID', timestamp: new Date(Date.now() - 20000).toISOString() },
        { status: 'ALLOCATED', timestamp: new Date(Date.now() - 10000).toISOString() },
        { status: 'SHIPPED', timestamp: new Date().toISOString() },
      ]
    };
    return of(mockHistory);
    // return this.http.get<OrderHistory>(`${this.apiUrl}/${id}/history`);
  }


  // Method to create a new order
  createOrder(amount: number): Observable<any> {
    // The backend endpoint from the README was /api/orders/start?amount=...
    return this.http.post(`${this.apiUrl}/start?amount=${amount}`, {});
  }
}
