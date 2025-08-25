import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from './order.service';
import { Order, OrderHistory, OrderStatus } from './order.model';
import { Observable, of } from 'rxjs';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css']
})
export class OrdersComponent implements OnInit {

  orders$: Observable<Order[]> = of([]);
  selectedOrderHistory$: Observable<OrderHistory | null> = of(null);

  newOrderAmount: number = 100;

  // For styling the status chips
  statusColors: Record<OrderStatus, string> = {
    [OrderStatus.NEW]: 'bg-blue-500',
    [OrderStatus.PAID]: 'bg-green-500',
    [OrderStatus.ALLOCATED]: 'bg-yellow-500',
    [OrderStatus.SHIPPED]: 'bg-purple-500',
    [OrderStatus.FAILED]: 'bg-red-500',
  };

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.orders$ = this.orderService.getOrders();
  }

  selectOrder(order: Order): void {
    this.selectedOrderHistory$ = this.orderService.getOrderHistory(order.id);
  }

  createOrder(): void {
    if (this.newOrderAmount > 0) {
      this.orderService.createOrder(this.newOrderAmount).subscribe({
        next: () => {
          console.log('Order created successfully');
          // Ideally, we would refresh the list or add the new order
          // For now, we just log it.
          this.loadOrders();
        },
        error: (err) => console.error('Failed to create order', err)
      });
    }
  }
}
