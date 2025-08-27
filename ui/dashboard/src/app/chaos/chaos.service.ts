import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChaosRule } from './chaos.model';

@Injectable({
  providedIn: 'root'
})
export class ChaosService {

  // Use a relative path that will be proxied by the Angular dev server
  // or a reverse proxy in production to the chaos-svc.
  private readonly rulesApiUrl = '/api/chaos/rules';
  private readonly canaryApiUrl = '/api/canary/config';

  constructor(private http: HttpClient) { }

  /**
   * Fetches the current map of all chaos rules from the backend.
   * The key is the topic name, the value is the rule.
   */
  getRules(): Observable<Record<string, ChaosRule>> {
    return this.http.get<Record<string, ChaosRule>>(this.rulesApiUrl);
  }

  /**
   * Updates the rule for a specific topic.
   * @param topic The topic to apply the rule to (e.g., 'payment.requested').
   * @param rule The chaos rule to apply.
   */
  updateRule(topic: string, rule: ChaosRule): Observable<ChaosRule> {
    return this.http.put<ChaosRule>(`${this.rulesApiUrl}/${topic}`, rule);
  }

  /**
   * Deletes the chaos rule for a specific topic.
   * @param topic The topic from which to remove the rule.
   */
  deleteRule(topic: string): Observable<void> {
    return this.http.delete<void>(`${this.rulesApiUrl}/${topic}`);
  }

  /**
   * Clears all chaos rules from the system.
   */
  clearAllRules(): Observable<void> {
    return this.http.delete<void>(this.rulesApiUrl);
  }

  /**
   * Enables or disables the canary release for the payment service.
   * @param enabled Whether to enable the canary.
   * @param percentage The percentage of traffic to send to the canary (e.g., 0.05 for 5%).
   */
  setCanary(enabled: boolean, percentage: number): Observable<void> {
    const config = { enabled, percentage };
    return this.http.post<void>(this.canaryApiUrl, config);
  }
}