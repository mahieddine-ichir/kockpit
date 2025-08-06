export interface RuleExecution {
  name: string;
  description: string;
  type: Type;
  executed: boolean;
  error: ErrorLevel;
  duration: number;
  branchStatus: BranchStatus;
  predicateExecutionStatus: PredicateExecutionStatus | null;
  id: string;
  errorDetails?: string;
}

export interface ExecutionTreeModel {
  current: RuleExecution;
  children: ExecutionTreeModel[];
  lastly: ExecutionTreeModel|null;
}

export enum Type {
  Action,
  Predicate,
  Rule ,
}

export enum BranchStatus {
  Predicate_ok,
  Predicate_ko,
  Not_From_Predicate,
  Origin ,
}

export enum PredicateExecutionStatus {
  Ok,
  Ko,
}
export enum ErrorLevel {
  Warning,
  Error,
  None
}
