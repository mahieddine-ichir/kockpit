export interface DashboardPage {
  items: DashboardSearchRequest[];
  totalSize: number;
  size: number;
  from: number;
}

export interface DashboardSearchRequest {
  index: string;
  id: string;
  requestId: string;
  domain: string;
  env: string;
  appId: string;
  start: Date;
  end: Date;
  indexedKeyValues: DashboardIndexedKeyValue[];
}

export class DashboardTimelineProperty {
  name?: string;
  properties?: string[];
  displayFn?: any;
}

export class ExecutionLagTime {
  property: string;
  source: string;
}

export class DashboardIndexedKeyValue {
  key: string;
  value: string;
}
