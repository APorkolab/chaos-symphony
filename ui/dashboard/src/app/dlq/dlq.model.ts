export interface DlqTopic {
  name: string;
  messageCount: number;
}

export interface DlqMessage {
  id: string; // Typically the message key or a unique identifier
  timestamp: string;
  headers: Record<string, string>;
  payload: string; // The raw message payload as a string
}
