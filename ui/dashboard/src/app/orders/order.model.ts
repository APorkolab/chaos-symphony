export interface Order {
  id: string;
  status: OrderStatus;
  total: number;
  createdAt: string;
}

export enum OrderStatus {
  NEW = 'NEW',
  PAID = 'PAID',
  ALLOCATED = 'ALLOCATED',
  SHIPPED = 'SHIPPED',
  FAILED = 'FAILED'
}

export interface OrderEvent {
  status: OrderStatus;
  timestamp: string;
}

export interface OrderHistory extends Order {
  history: OrderEvent[];
}
