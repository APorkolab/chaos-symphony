import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ChaosService } from './chaos.service';
import { ChaosRule } from './chaos.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-chaos',
  templateUrl: './chaos.component.html',
  styleUrls: ['./chaos.component.css']
})
export class ChaosComponent implements OnInit {

  rules: Record<string, ChaosRule> = {};
  topics: string[] = [];
  ruleForm: FormGroup;
  isLoading = false;
  canaryEnabled = false;

  constructor(
    private fb: FormBuilder,
    private chaosService: ChaosService
  ) {
    this.ruleForm = this.fb.group({
      topic: ['', Validators.required],
      pDrop: [0, [Validators.min(0), Validators.max(1)]],
      pDup: [0, [Validators.min(0), Validators.max(1)]],
      maxDelayMs: [0, Validators.min(0)],
      pCorrupt: [0, [Validators.min(0), Validators.max(1)]]
    });
  }

  ngOnInit(): void {
    this.loadRules();
  }

  loadRules(): void {
    this.isLoading = true;
    this.chaosService.getRules()
      .pipe(finalize(() => this.isLoading = false))
      .subscribe(data => {
        this.rules = data;
        this.topics = Object.keys(data);
      });
  }

  editRule(topic: string): void {
    const rule = this.rules[topic];
    if (rule) {
      // Need to add topic since it's part of the form but not the rule object
      const formData = { topic, ...rule };
      this.ruleForm.setValue(formData);
    }
  }

  onSubmit(): void {
    if (this.ruleForm.invalid) {
      return;
    }
    this.isLoading = true;
    const { topic, ...ruleValues } = this.ruleForm.value;
    const rule: ChaosRule = ruleValues;

    this.chaosService.updateRule(topic, rule)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe(() => {
        this.loadRules(); // Refresh the list after update
        this.ruleForm.reset({
          topic: '', pDrop: 0, pDup: 0, maxDelayMs: 0, pCorrupt: 0
        });
      });
  }

  onDelete(topic: string): void {
    if (confirm(`Are you sure you want to delete the rule for topic "${topic}"?`)) {
      this.isLoading = true;
      this.chaosService.deleteRule(topic)
        .pipe(finalize(() => this.isLoading = false))
        .subscribe(() => this.loadRules());
    }
  }

  onCanaryToggle(): void {
    this.isLoading = true;
    // The service takes the desired state, so we use the current model value
    this.chaosService.setCanary(this.canaryEnabled, 0.05)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: () => console.log(`Canary toggled to ${this.canaryEnabled}`),
        error: (err) => {
          console.error('Failed to toggle canary', err);
          // Revert the toggle on error
          this.canaryEnabled = !this.canaryEnabled;
        }
      });
  }
}
