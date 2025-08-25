import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DlqService } from './dlq.service';
import { DlqTopic, DlqMessage } from './dlq.model';
import { Observable, of } from 'rxjs';

@Component({
  selector: 'app-dlq',
  standalone: true,
  imports: [CommonModule],
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

  retryAllMessages(): void {
    if (this.selectedTopic) {
      this.dlqService.retryAll(this.selectedTopic.name).subscribe({
        next: () => {
          console.log(`Retry successful for ${this.selectedTopic?.name}`);
          // Refresh data
          this.loadTopics();
          this.messages$ = of([]);
          this.selectedTopic = null;
        },
        error: (err) => console.error('Retry failed', err)
      });
    }
  }
}
