import {Operand} from "./search-zone-component/search/types/Operand";

export class SearchOperationQuery {
  operator: string;
  operand: Operand;

  constructor(operator: string, operand: Operand) {
    this.operator = operator;
    this.operand = operand;
  }

}

export class SearchQuery {
  type: string;
  subtype?: string;
  name: string;
  mode?: string;
  operation: SearchOperationQuery;
}

export class AuditRequest {
  requestId: string;
  date: Date;
  traceId: string;
  contactId: string;
  parentId: string;
  serviceName: string;
  start: Date;
  typeRequest: string;
  result: string;
  executionUUID: string;
  create: boolean;
  time: number;
  status: string;
  username: string;
  origin: string;
  executionLog: ExecutionLog;
  serviceCallTime: Number;
  version: String;
  httpStatusCode: Number;
  host: String;
  errorMessage: string;
  originalJsonValue: string;
}

export interface AuditRequestPage {
  items: AuditRequest[];
  totalSize: number;
  size: number;
  from: number;
}

export class ExternalCall {
  id: string;
  order: number;
  type: string;
}

export class KengineLog {
  log: string;
  date: Date;
  action: string;
}

export class Execution {
  executionName: string;
  creationTimestamp: number;
  position: number;
  error: string;
  errorMessage: string;
  errorDetails: string;
  date: Date;
  time: number;
  logs: KengineLog[];

  executionUUID: string;
  engineVersion: number;
  executionRules: any[];
  rules: any[];
  referential: any;
  startTime: number;
  endTime: number;
}

export class ExecutionLog {
  ruleCode: string;
  actionCode: string;
  predicateCode: string;
  errorMessage: string;
}

