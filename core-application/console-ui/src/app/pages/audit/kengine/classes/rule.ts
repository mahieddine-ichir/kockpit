import {BranchStatus, ErrorLevel, ExecutionTreeModel, PredicateExecutionStatus, RuleExecution, Type} from '../model/execution-tree.model';
import {cloneDeep} from 'lodash';

export class Rule {
  name = '';
  description = '';
  duration = 0;
  refExecutionTree: ExecutionTreeModel;
  error: boolean;

  constructor(name: string, description: string, duration: number, error: boolean) {
    this.name = name;
    this.description = description;
    this.duration = duration;
    this.error = error;

    const current: RuleExecution = this.createRuleExecution(this.name, this.description, Type.Rule, this.duration, BranchStatus.Origin);

    const executionTree: ExecutionTreeModel = {
      current : current,
      children: [],
      lastly: null
    };
    this.refExecutionTree = executionTree;
  }

  public initTreeWithData(data: any, execData: any) {
    this.refExecutionTree = this.initRuleExecutionsTree(cloneDeep(data));
    this.refExecutionTree = this.setExecution(cloneDeep(execData));
  }

  private initRuleExecutionsTree(data: any) {

    const current: RuleExecution = {
      name: this.name,
      description: this.description,
      type: Type.Rule,
      duration: this.duration,
      error: ErrorLevel.None,
      executed: false,
      branchStatus : BranchStatus.Not_From_Predicate,
      predicateExecutionStatus: null,
      id: this.generateId(this.name, BranchStatus.Not_From_Predicate)
    };

    const executionTree: ExecutionTreeModel = {
      current : current,
      children: this.recursiveInitRuleExecutionTree(data, BranchStatus.Not_From_Predicate),
      lastly: null
    };
    return  executionTree;
  }

  private recursiveInitRuleExecutionTree(execRule: any, branchStatus: BranchStatus): ExecutionTreeModel[] {
    let tree_: ExecutionTreeModel;
    let lastly: any = null;
    if (execRule.lastly != null && execRule.lastly !== undefined) {
      lastly = this.recursiveInitRuleExecutionTree(execRule.lastly, BranchStatus.Not_From_Predicate)[0];
      execRule.lastly = null;
    }
    if (this.hasActions(execRule) ) {

      tree_ = this.createActionNodeTree(execRule, branchStatus, lastly);

    } else if (this.hasPredicate(execRule)) {

      tree_ = this.createPredicateNodeTree(execRule, branchStatus, lastly);

    } else if (execRule.predicates.length === 0 && execRule.actions.length === 0) {
      let children: ExecutionTreeModel[] = [];
      if (execRule.ok) {
        children = children.concat(this.recursiveInitRuleExecutionTree(execRule.ok, BranchStatus.Predicate_ok));
      }
      if (execRule.ko) {
        children.concat(this.recursiveInitRuleExecutionTree(execRule.ko, BranchStatus.Predicate_ko));
      }
      return children;
    }
    return [tree_];
  }

  private hasPredicate(execRule: any) {
    return execRule.predicates.length > 0;
  }

  private hasActions(execRule: any) {
    return execRule.actions.length > 0;
  }

  private createPredicateNodeTree(execRule: any, branchStatus: BranchStatus, lastly: any) {
    const predicate = execRule.predicates[0];
    const current = this.createRuleExecution(predicate.name, predicate.description, Type.Predicate, 0, branchStatus);
    let children: ExecutionTreeModel[] = [];
    if (execRule.ok) {
      children = children.concat(this.recursiveInitRuleExecutionTree(execRule.ok, BranchStatus.Predicate_ok));
    }
    if (execRule.ko) {
      children = children.concat(this.recursiveInitRuleExecutionTree(execRule.ko, BranchStatus.Predicate_ko));
    }
    return {
      current : current,
      children : children,
      lastly: lastly
    };
  }

  private createActionNodeTree(execRule: any, branchStatus: BranchStatus, lastly: any) {
    const action = execRule.actions[0];
    const current = this.createRuleExecution(action.name, action.description, Type.Action, 0, branchStatus);
    execRule.actions.splice(0, 1);
    const tree_ = {
      current: current,
      children: this.recursiveInitRuleExecutionTree(execRule, branchStatus),
      lastly: lastly,
    };
    return tree_;
  }

  private setExecution(execData: any) {
    return {
      current: this.refExecutionTree.current,
      children : this.recursiveExecution(this.refExecutionTree.children, execData, BranchStatus.Not_From_Predicate),
      lastly: null
    };
  }

  private recursiveExecution(tree: ExecutionTreeModel[], execData: any, branchStatus: BranchStatus ) {

    if (!execData || execData.length === 0) {
      return tree;
    }

    const currentExec = execData[0];
    let nodeExecuted: ExecutionTreeModel;

    if (branchStatus === BranchStatus.Predicate_ko || branchStatus === BranchStatus.Predicate_ok) {
      nodeExecuted = tree.filter( t => t.current.name === currentExec.name && t.current.branchStatus === branchStatus)[0];
    } else {
      nodeExecuted = tree.filter( t => t.current.name === currentExec.name)[0];
    }

    if (nodeExecuted) {

      let errorMessage = '';
      let errorDetails = '';

      if (currentExec.error === 'ERROR') {
        nodeExecuted.current.error = ErrorLevel.Error;
        errorMessage = ' ERROR : ' + currentExec.errorMessage;
        errorDetails = currentExec.errorDetails;
      }

      if (currentExec.error === 'WARNING') {
        nodeExecuted.current.error = ErrorLevel.Warning;
        errorMessage = ' WARNING : ' + currentExec.errorMessage;
        errorDetails = currentExec.errorDetails
      }

      nodeExecuted.current.description = nodeExecuted.current.description + errorMessage;
      nodeExecuted.current.executed = true;
      nodeExecuted.current.duration = currentExec.time;
      nodeExecuted.current.errorDetails = errorDetails;
      execData.splice(0, 1);

      if ( nodeExecuted.current.type === Type.Predicate ) {
        this.handlePredicateExecution(currentExec, nodeExecuted, execData);
      } else if ( nodeExecuted.current.type === Type.Action) {
        this.handleActionExecutin(nodeExecuted, execData);
      }
      if (nodeExecuted.lastly && execData.length > 0) {
        this.handleLastlyExecution(nodeExecuted, execData);
      }
    }
    return tree;
  }

  private handleActionExecutin(nodeExecuted: ExecutionTreeModel, execData: any) {
    nodeExecuted.children = this.recursiveExecution(nodeExecuted.children, execData, nodeExecuted.current.branchStatus);
  }

  private handlePredicateExecution(currentExec, nodeExecuted: ExecutionTreeModel, execData: any) {
    if (currentExec.condition) {
      nodeExecuted.current.predicateExecutionStatus = PredicateExecutionStatus.Ok;
      nodeExecuted.children = this.recursiveExecution(nodeExecuted.children, execData, BranchStatus.Predicate_ok);
    } else if (!currentExec.condition) {
      nodeExecuted.current.predicateExecutionStatus = PredicateExecutionStatus.Ko;
      nodeExecuted.children = this.recursiveExecution(nodeExecuted.children, execData, BranchStatus.Predicate_ko);
    }
  }

  private handleLastlyExecution(nodeExecuted: ExecutionTreeModel, execData: any) {
    nodeExecuted.lastly = this.recursiveExecution([nodeExecuted.lastly], execData, BranchStatus.Not_From_Predicate)[0];
  }

  createRuleExecution(name: string, description: string, type: Type, duration: number, branchStatus: BranchStatus) {
    return {
      name: name,
      description: description,
      type: type,
      executed: false,
      error: ErrorLevel.None,
      duration: duration,
      branchStatus: branchStatus,
      id: this.generateId(name, branchStatus),
      predicateExecutionStatus: null,
    };
  }

  generateId(name: string, branchStatus: BranchStatus) {
    return name + '_' + branchStatus + '_' + Math.random();
  }
}

