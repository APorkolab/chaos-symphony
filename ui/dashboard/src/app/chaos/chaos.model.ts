export interface ChaosRule {
  id: string;
  targetTopic: string;
  faultType: FaultType;
  probability: number; // 0.0 to 1.0
  delayMs?: number;
  // Other potential fields like 'errorCode', 'exceptionMessage', etc.
}

export enum FaultType {
  DELAY = 'DELAY',
  DUPLICATE = 'DUPLICATE',
  MUTATE = 'MUTATE',
  DROP = 'DROP'
}

// A simplified model for the UI state
export interface ChaosSettings {
  [key: string]: {
    enabled: boolean;
    probability: number;
    delayMs?: number;
  };
}
