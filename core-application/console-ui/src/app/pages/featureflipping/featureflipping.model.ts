
export interface FeatureflippingConfig {
  id: string;
  lastUpdatedTimestamp: number;
  properties: Map<string, KeyValue>;
  changes: PropertyChange[];
  issuedCommands: CommandIssued[];
}

export interface KeyValue {
  key: string;
  value: string;
  lastUpdatedTimestamp: number;
  description: string;
  comment: string;
  source: string;
  status: string;
  expiration: Date;
  instances: PropertyInstance[];
}

export interface PropertyInstance {
  applicationInstance: string;
  status: string;
  currentValue: string;
}

export enum ExecutionModeEnum {
  ALWAYS_ON = 'ALWAYS_ON',
  SCHEDULED_TASK = 'SCHEDULED_TASK'
}

export interface DialogData {
  domain: string;
  env: string;
  applicationId: string;
  name: string;
  value: string;
  instances: PropertyInstance[];
}

export interface PropertyChange {
  timestamp: number;
  propertyName: string;
  valueBeforeChange: string;
  valueAfterChange: string;
  logMessage: string;
  username: string;
}

export interface CommandIssued {
  status: string;
  message: string;
  type: string;
  timestamp: number;
  requestId: string;
  applicationInstance: string;
  propertyName: string;
  logMessage: string;
  propertyValue: string;
}
