import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DlqService } from './dlq.service';
import { DlqTopic, DlqMessage } from './dlq.model';
import { Observable, of } from 'rxjs';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-dlq',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dlq.component.html',
  styleUrls: ['./dlq.component.css']
})
export class DlqComponent implements OnInit {

  topics$: Observable<DlqTopic[]> = of([]);
  messages$: Observable<DlqMessage[]> = of([]);
  selectedTopic: DlqTopic | null = null;

  constructor(private dlqService: DlqService) {}

  ngOnInit(): void {
    this.loadTopics();
  }

  loadTopics(): void {
    this.topics$ = this.dlqService.getDlqTopics();
  }

  selectTopic(topic: DlqTopic): void {
    this.selectedTopic = topic;
    this.messages$ = this.dlqService.getMessages(topic.name);
  }

  replayAll(): void {
    if (!this.selectedTopic) return;
    this.dlqService.retryAllForTopic(this.selectedTopic.name).subscribe({
      next: () => {
        alert('Replay command sent successfully.');
        this.refreshData();
      },
      error: (err) => alert(`Replay failed: ${err.message}`)
    });
  }

  purge(): void {
    if (!this.selectedTopic) return;
    if (confirm(`Are you sure you want to purge all messages from ${this.selectedTopic.name}? This action cannot be undone.`)) {
      this.dlqService.purgeTopic(this.selectedTopic.name).subscribe({
        next: () => {
          alert('Purge command sent successfully.');
          this.refreshData();
        },
        error: (err) => alert(`Purge failed: ${err.message}`)
      });
    }
  }

  private refreshData(): void {
    this.loadTopics();
    if (this.selectedTopic) {
      this.selectTopic(this.selectedTopic);
    }
  }
}
