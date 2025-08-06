import {Rule} from './rule';

export class Flow {
  rules: Rule[] = [];
  name = '';
  startTime: number;
  endTime: number;
  status: string;

  constructor(name: string) {
    this.name = name;
  }

  addRule(rule: Rule) {
    this.rules.push(rule);
  }

}
