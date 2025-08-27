import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { OrderService } from './order.service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-orders',
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css']
})
export class OrdersComponent implements OnInit {

  orderForm: FormGroup;
  isLoading = false;
  recentOrders: string[] = [];

  constructor(
    private fb: FormBuilder,
    private orderService: OrderService
  ) {
    this.orderForm = this.fb.group({
      customerId: ['customer-123', Validators.required],
      total: [100.00, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
  }

  onSubmit(): void {
    if (this.orderForm.invalid) {
      return;
    }

    this.isLoading = true;
    this.orderService.createOrder(this.orderForm.value)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (response) => {
          // Add the new order ID to the top of the list
          this.recentOrders.unshift(response.orderId);
          // Keep only the last 10 orders for display
          if (this.recentOrders.length > 10) {
            this.recentOrders.pop();
          }
          this.orderForm.patchValue({ total: Math.round(100 + Math.random() * 500) });
        },
        error: (err) => {
          console.error('Failed to create order', err);
          // Here you would show an error message to the user
        }
      });
  }

  replay(): void {
    if (confirm('This will reset the consumer group for the orchestrator. Are you sure?')) {
      this.isLoading = true;
      this.orderService.replayLastFiveMinutes()
        .pipe(finalize(() => this.isLoading = false))
        .subscribe({
          next: () => console.log('Replay command sent successfully.'),
          error: (err) => console.error('Failed to send replay command', err)
        });
    }
  }
}
