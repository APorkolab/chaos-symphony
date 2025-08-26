import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DlqService } from './dlq.service';
import { DlqTopic, DlqMessage } from './dlq.model';
import { Observable, of, tap } from 'rxjs';
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
  messages: DlqMessage[] = [];
  messages$: Observable<DlqMessage[]> = of([]);
  selectedTopic: DlqTopic | null = null;
  selectedMessages = new Set<string>();

  constructor(private dlqService: DlqService) {}

  ngOnInit(): void {
    this.loadTopics();
  }

  loadTopics(): void {
    this.topics$ = this.dlqService.getDlqTopics();
  }

  selectTopic(topic: DlqTopic): void {
    this.selectedTopic = topic;
    this.selectedMessages.clear();
    this.messages$ = this.dlqService.getMessages(topic.name).pipe(
      tap(messages => this.messages = messages)
    );
  }

  get isAllSelected(): boolean {
    return this.messages.length > 0 && this.selectedMessages.size === this.messages.length;
  }

  toggleSelectAll(event: Event): void {
    const isChecked = (event.target as HTMLInputElement).checked;
    if (isChecked) {
      this.messages.forEach(m => this.selectedMessages.add(m.id));
    } else {
      this.selectedMessages.clear();
    }
  }

  toggleMessageSelection(messageId: string): void {
    if (this.selectedMessages.has(messageId)) {
      this.selectedMessages.delete(messageId);
    } else {
      this.selectedMessages.add(messageId);
    }
  }

  retrySelectedMessages(): void {
    if (this.selectedTopic && this.selectedMessages.size > 0) {
      const idsToRetry = Array.from(this.selectedMessages);
      this.dlqService.retrySelected(this.selectedTopic.name, idsToRetry).subscribe({
        next: () => {
          console.log(`Retry successful for ${idsToRetry.length} messages in ${this.selectedTopic?.name}`);
          // Refresh data
          this.selectTopic(this.selectedTopic!); // Reload messages for the current topic
          this.loadTopics(); // Refresh topic counts
        },
        error: (err) => console.error('Retry failed', err)
      });
    }
  }
}
