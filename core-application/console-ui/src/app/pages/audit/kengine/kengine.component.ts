import {Component, Input, OnInit} from '@angular/core';
import {Execution} from '../request.model';
import {Flow} from './classes/flow';
import {Rule} from './classes/rule';
import {Subject} from 'rxjs';

@Component({
  selector: 'wcc-kengine',
  templateUrl: './kengine.component.html',
  styleUrls: ['./kengine.component.scss']
})
export class KengineComponent implements OnInit {

  @Input()
  executions: Execution[] = [];

  @Input()
  focusFlowExecutionsTab: Subject<void>;

  execution: Execution;
  flows: Flow[] = [];

  constructor() {
  }

  ngOnInit(): void {
    console.log('executions', this.executions);
    this.initFlows();
  }

  initFlows() {
    let i = 1;
    this.executions.forEach(exec => {
      let name = 'flow' + i;
      if (exec.executionName) {
        name = exec.executionName;
      }
      const flow: Flow = new Flow(name);
      flow.startTime = exec.startTime;
      flow.endTime = exec.endTime;
      flow.status = exec.error;
      exec.executionRules.forEach(execRule => {
        this.createFlowRules(execRule, exec, flow);
      });
      this.flows.push(flow);
      i++;
    });
  }

  private createFlowRules(execRule, exec: Execution, flow: Flow) {
    let error = false;
    if (execRule.error === 'ERROR') {
      error = true;
    }
    const rule: Rule = new Rule(execRule.name, exec.referential[execRule.name].details.description, execRule.time, error);
    rule.initTreeWithData(exec.referential[execRule.name], exec.rules[execRule.name]);
    flow.addRule(rule);
  }
}
