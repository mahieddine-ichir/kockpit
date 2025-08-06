export class Operand {
  value: string;
  valueTo: string;
  type: string;

  constructor(value: string, valueTo: string, type: string) {
    this.value = value;
    this.valueTo = valueTo;
    this.type = type;
  }
}

