import {BackEndOperation} from "./BackEndOperation";
import {Operand} from "./Operand";

export class SelectedOperation {

  operator: BackEndOperation;
  operand: Operand;

  constructor(operator: BackEndOperation, operand: Operand) {
    this.operator = operator;
    this.operand = operand;
  }

  static toDto(x) {
    return new SelectedOperation(BackEndOperation.toDto(x.operator), x.operand);
  }
}
