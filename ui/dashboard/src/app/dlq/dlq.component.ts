import { Component, OnInit } from '@angular/core';
import { DlqService } from './dlq.service';
import { DlqTopic, DlqMessage } from './dlq.model';
import { finalize } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-dlq',
    templateUrl: './dlq.component.html',
    styleUrls: ['./dlq.component.css'],
    standalone: true,
    imports: [CommonModule],
})
export class DlqComponent implements OnInit {

  topics: DlqTopic[] = [];
  selectedTopic: DlqTopic | null = null;
  messages: DlqMessage[] = [];
  isLoadingTopics = false;
  isLoadingMessages = false;

  constructor(private dlqService: DlqService) { }

  ngOnInit(): void {
    this.loadTopics();
  }

  loadTopics(): void {
    this.isLoadingTopics = true;
    this.dlqService.getDlqTopics()
      .pipe(finalize(() => this.isLoadingTopics = false))
      .subscribe(data => {
        this.topics = data;
      });
  }

  selectTopic(topic: DlqTopic): void {
    this.selectedTopic = topic;
    this.messages = [];
    if (topic) {
      this.isLoadingMessages = true;
      this.dlqService.getMessages(topic.name, 20) // Peek at 20 messages
        .pipe(finalize(() => this.isLoadingMessages = false))
        .subscribe(msgs => this.messages = msgs);
    }
  }

  onReplay(topic: DlqTopic): void {
    if (confirm(`Are you sure you want to replay all messages from "${topic.name}"?`)) {
      this.isLoadingTopics = true; // Use main loader for topic-level actions
      this.dlqService.retryAllForTopic(topic.name)
        .pipe(finalize(() => this.loadTopics())) // Refresh list after action
        .subscribe(response => {
          console.log('Replay response:', response);
          this.selectedTopic = null; // Deselect to avoid stale message view
          this.messages = [];
        });
    }
  }

  onPurge(topic: DlqTopic): void {
    if (confirm(`DANGER: Are you sure you want to permanently delete all messages from "${topic.name}"?`)) {
      this.isLoadingTopics = true;
      this.dlqService.purgeTopic(topic.name)
        .pipe(finalize(() => this.loadTopics()))
        .subscribe(response => {
          console.log('Purge response:', response);
          this.selectedTopic = null;
          this.messages = [];
        });
    }
  }
}
