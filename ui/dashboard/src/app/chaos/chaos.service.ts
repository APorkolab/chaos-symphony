import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, tap, switchMap, forkJoin } from 'rxjs';
import { ChaosRule, ChaosSettings, FaultType } from './chaos.model';

@Injectable({
  providedIn: 'root'
})
export class ChaosService {
  private apiUrl = 'http://localhost:8088/api/chaos/rules'; // Base URL for the chaos-svc rules

  private activeRules = new Map<FaultType, string>(); // Maps fault type to the created rule ID

  private settings = new BehaviorSubject<ChaosSettings>({
    [FaultType.DELAY]: { enabled: false, probability: 0.1, delayMs: 1000 },
    [FaultType.DUPLICATE]: { enabled: false, probability: 0.1 },
    [FaultType.MUTATE]: { enabled: false, probability: 0.1 },
    [FaultType.DROP]: { enabled: false, probability: 0.1 },
  });

  settings$ = this.settings.asObservable();

  constructor(private http: HttpClient) { }

  loadSettings(): void {
    this.http.get<ChaosRule[]>(this.apiUrl).subscribe(rules => {
      const newSettings = this.settings.getValue();
      // Reset all to disabled
      Object.values(FaultType).forEach(ft => newSettings[ft].enabled = false);
      this.activeRules.clear();

      // Apply rules from backend
      rules.forEach(rule => {
        if (newSettings[rule.faultType]) {
          newSettings[rule.faultType].enabled = true;
          newSettings[rule.faultType].probability = rule.probability;
          if(rule.delayMs) newSettings[rule.faultType].delayMs = rule.delayMs;
          this.activeRules.set(rule.faultType, rule.id);
        }
      });
      this.settings.next(newSettings);
    });
  }

  updateSetting(faultType: FaultType, setting: { enabled: boolean; probability: number; delayMs?: number }): Observable<any> {
    const existingRuleId = this.activeRules.get(faultType);

    const delete$ = existingRuleId ? this.http.delete(`${this.apiUrl}/${existingRuleId}`) : of(null);

    return delete$.pipe(
      switchMap(() => {
        if (setting.enabled) {
          const newRule = {
            targetTopic: 'all', // Target all topics for simplicity
            types: [faultType],
            p: setting.probability,
            delayMs: setting.delayMs,
          };
          return this.http.post<ChaosRule>(this.apiUrl, newRule).pipe(
            tap(createdRule => {
              this.activeRules.set(faultType, createdRule.id);
            })
          );
        } else {
          this.activeRules.delete(faultType);
          return of(null); // Nothing to do if disabled and already deleted
        }
      }),
      tap(() => this.loadSettings()) // Refresh state from backend after change
    );
  }
}
