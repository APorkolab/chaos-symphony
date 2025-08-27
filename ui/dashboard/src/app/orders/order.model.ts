export interface CreateOrderCommand {
  customerId: string;
  total: number;
}

export interface Order {
  id: string;
  status: string;
  total: number;
  createdAt: string;
  // This could be expanded to include the full event timeline
}
