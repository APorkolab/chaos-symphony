import { Routes } from '@angular/router';
import { OrdersComponent } from './orders/orders.component';
import { DlqComponent } from './dlq/dlq.component';
import { ChaosComponent } from './chaos/chaos.component';
import { SloComponent } from './slo/slo.component';
import { LoginComponent } from './login/login.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'orders', component: OrdersComponent },
  { path: 'dlq', component: DlqComponent },
  { path: 'chaos', component: ChaosComponent },
  { path: 'slo', component: SloComponent },
  { path: '', redirectTo: '/orders', pathMatch: 'full' }
];
