import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DlqTopic, DlqMessage } from './dlq.model';

@Injectable({
  providedIn: 'root'
})
export class DlqService {
  private apiUrl = 'http://localhost:8089/api/dlq'; // Base URL for the dlq-admin service

  constructor(private http: HttpClient) { }

  getDlqTopics(): Observable<DlqTopic[]> {
    return this.http.get<DlqTopic[]>(`${this.apiUrl}/topics`);
  }

  // The backend endpoint is `/peek`, so we'll use that.
  getMessages(topicName: string, count: number = 10): Observable<DlqMessage[]> {
    return this.http.get<DlqMessage[]>(`${this.apiUrl}/${topicName}/peek?n=${count}`);
  }

  retryAllForTopic(topicName: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${topicName}/replay`, {});
  }

  retrySelected(topicName: string, messageIds: string[]): Observable<any> {
    // This assumes a backend endpoint that can accept a list of IDs to retry.
    // The dlq-admin service would need to be updated to handle this.
    return this.http.post(`${this.apiUrl}/${topicName}/retry-selected`, { messageIds });
  }
}