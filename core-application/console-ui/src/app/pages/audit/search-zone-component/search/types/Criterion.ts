import {BackendSearchType} from "./BackendSearchType";
import {SelectedOperation} from "./SelectedOperation";
import {SearchOperationQuery, SearchQuery} from "../../../request.model";
import {Operand} from "./Operand";

export interface Criterion {
  type: BackendSearchType,
  subtype?: string,
  name: string,
  label: string,
  description: string,
  options?: string[]
}

export class SelectedCriterion {
  type: BackendSearchType;
  subtype?: string;
  name: string;
  operation: SelectedOperation;

  static toDto(x) {
    return {
      type: BackendSearchType.toDto(x.type),
      subtype: x.subtype,
      name: x.name,
      operation: SelectedOperation.toDto(x.operation)
    }
  }

  static toSearchQuery(selectedCriterion: SelectedCriterion): SearchQuery {
    return {
      type: BackendSearchType.toDto(selectedCriterion.type),
      subtype: selectedCriterion.subtype,
      name: selectedCriterion.name,
      operation: new SearchOperationQuery(selectedCriterion.operation.operator.name, selectedCriterion.operation.operand)
    }
}

  constructor(option: Criterion, operation: SelectedOperation) {
    this.type = option.type;
    this.subtype = option.subtype;
    this.name = option.name;
    this.operation = operation;
  }
}
