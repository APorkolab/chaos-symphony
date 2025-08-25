import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { ChaosRule, ChaosSettings, FaultType } from './chaos.model';

@Injectable({
  providedIn: 'root'
})
export class ChaosService {
  private apiUrl = 'http://localhost:8088/api/chaos'; // Assuming this is the base URL for the chaos-svc

  // Use a BehaviorSubject to hold the current state of settings
  private settings = new BehaviorSubject<ChaosSettings>({
    [FaultType.DELAY]: { enabled: false, probability: 0.1, delayMs: 1000 },
    [FaultType.DUPLICATE]: { enabled: false, probability: 0.1 },
    [FaultType.MUTATE]: { enabled: false, probability: 0.1 },
    [FaultType.DROP]: { enabled: false, probability: 0.1 },
  });

  settings$ = this.settings.asObservable();

  constructor(private http: HttpClient) { }

  // In a real app, this would fetch rules from the backend and map them to ChaosSettings
  loadSettings(): void {
    // For now, we just use the initial mock state
    // this.http.get<ChaosRule[]>(`${this.apiUrl}/rules`).subscribe(rules => {
    //   const newSettings = this.mapRulesToSettings(rules);
    //   this.settings.next(newSettings);
    // });
  }

  // Update a specific setting
  updateSetting(faultType: FaultType, setting: { enabled: boolean; probability: number; delayMs?: number }): Observable<any> {
    console.log(`Updating ${faultType} setting to:`, setting);

    // Update the local state
    const currentSettings = this.settings.getValue();
    currentSettings[faultType] = setting;
    this.settings.next(currentSettings);

    // In a real app, this would translate the setting into API calls
    // (e.g., DELETE existing rule, POST new rule if enabled)
    // For now, just return a success observable
    return of({ status: 'OK' });

    // Example of real implementation logic:
    // const rule: Partial<ChaosRule> = {
    //   targetTopic: 'all', // Or make this configurable
    //   faultType: faultType,
    //   probability: setting.probability,
    //   delayMs: setting.delayMs
    // };
    // if(setting.enabled) {
    //   return this.http.post(`${this.apiUrl}/rules`, rule);
    // } else {
    //   // Assuming we have a way to identify the rule to delete
    //   return this.http.delete(`${this.apiUrl}/rules/by-type/${faultType}`);
    // }
  }
}
