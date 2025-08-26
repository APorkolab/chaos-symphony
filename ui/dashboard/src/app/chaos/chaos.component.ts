import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChaosService } from './chaos.service';
import { ChaosSettings, FaultType } from './chaos.model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-chaos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chaos.component.html',
  styleUrls: ['./chaos.component.css']
})
export class ChaosComponent implements OnInit {

  settings$!: Observable<ChaosSettings>;
  faultTypes = Object.values(FaultType);

  // A local copy for two-way binding with ngModel
  localSettings: ChaosSettings = {
    [FaultType.DELAY]: { enabled: false, probability: 0, delayMs: 0 },
    [FaultType.DUPLICATE]: { enabled: false, probability: 0 },
    [FaultType.MUTATE]: { enabled: false, probability: 0 },
    [FaultType.DROP]: { enabled: false, probability: 0 },
  };
  canaryEnabled: boolean = false;

  constructor(private chaosService: ChaosService) {}

  ngOnInit(): void {
    this.settings$ = this.chaosService.settings$;
    this.settings$.subscribe(settings => {
      // Deep copy to avoid direct mutation of the service's state
      this.localSettings = JSON.parse(JSON.stringify(settings));
    });
    this.chaosService.loadSettings();
  }

  onSettingChange(faultType: FaultType): void {
    const setting = this.localSettings[faultType];
    this.chaosService.updateSetting(faultType, setting).subscribe({
      next: () => console.log(`${faultType} updated successfully.`),
      error: (err) => console.error(`Failed to update ${faultType}`, err)
    });
  }

  onCanaryToggle(): void {
    this.chaosService.setCanary(this.canaryEnabled).subscribe({
      next: () => console.log(`Canary mode set to ${this.canaryEnabled}`),
      error: (err) => {
        console.error('Failed to set canary mode', err);
        // Revert the toggle on error
        this.canaryEnabled = !this.canaryEnabled;
      }
    });
  }
}
