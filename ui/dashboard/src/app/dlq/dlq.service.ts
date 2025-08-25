import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { DlqTopic, DlqMessage } from './dlq.model';

@Injectable({
  providedIn: 'root'
})
export class DlqService {
  private apiUrl = 'http://localhost:8089/api/dlq'; // Base URL for the dlq-admin service

  constructor(private http: HttpClient) { }

  // Get all topics with messages in the DLQ
  getDlqTopics(): Observable<DlqTopic[]> {
    const mockTopics: DlqTopic[] = [
      { name: 'payment.requested.DLT', messageCount: 5 },
      { name: 'inventory.requested.DLT', messageCount: 2 },
    ];
    return of(mockTopics);
    // return this.http.get<DlqTopic[]>(`${this.apiUrl}/topics`);
  }

  // Get messages for a specific topic
  getMessages(topicName: string): Observable<DlqMessage[]> {
    const mockMessages: DlqMessage[] = [
      {
        id: 'order-123-fail',
        timestamp: new Date().toISOString(),
        headers: { 'x-death': 'count: 3', 'x-exception-message': 'Database connection failed' },
        payload: '{"orderId": "order-123-fail", "amount": 99.99, "currency": "HUF"}'
      },
      {
        id: 'order-456-fail',
        timestamp: new Date().toISOString(),
        headers: { 'x-death': 'count: 3', 'x-exception-message': 'Invalid item ID' },
        payload: '{"orderId": "order-456-fail", "amount": 150.00, "currency": "HUF"}'
      }
    ];
    return of(mockMessages);
    // return this.http.get<DlqMessage[]>(`${this.apiUrl}/${topicName}/messages`);
  }

  // Retry all messages for a topic
  retryAll(topicName: string): Observable<any> {
    console.log(`Retrying all messages for topic: ${topicName}`);
    return of({ status: 'OK', message: `Retried all messages for ${topicName}` });
    // return this.http.post(`${this.apiUrl}/${topicName}/replay`, {});
  }
}
