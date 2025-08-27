import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { switchMap, map, catchError, forkJoin } from 'rxjs/operators';
import { DlqTopic, DlqMessage } from './dlq.model';

@Injectable({
  providedIn: 'root'
})
export class DlqService {
  private apiUrl = 'http://localhost:8089/api/dlq'; // Base URL for the dlq-admin service

  constructor(private http: HttpClient) { }

  getDlqTopics(): Observable<DlqTopic[]> {
    return this.http.get<string[]>(`${this.apiUrl}/topics`).pipe(
      switchMap(topicNames => {
        if (topicNames.length === 0) {
          return of([]);
        }
        const topicObservables = topicNames.map(name =>
          this.http.get<number>(`${this.apiUrl}/${name}/count`).pipe(
            map(count => ({ name, messageCount: count } as DlqTopic)),
            catchError(() => of({ name, messageCount: -1 } as DlqTopic)) // Handle error case
          )
        );
        return forkJoin(topicObservables);
      })
    );
  }

  getMessages(topicName: string, count: number = 10): Observable<DlqMessage[]> {
    return this.http.get<DlqMessage[]>(`${this.apiUrl}/${topicName}/peek?n=${count}`);
  }

  retryAllForTopic(topicName: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${topicName}/replay`, {});
  }

  purgeTopic(topicName: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${topicName}`);
  }
}