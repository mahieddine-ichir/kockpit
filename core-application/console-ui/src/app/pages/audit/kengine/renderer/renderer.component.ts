import {ChangeDetectorRef, Component, ElementRef, Inject, Input, OnInit, ViewChild} from '@angular/core';
import {Flow} from '../classes/flow';
import {Network} from 'vis';
import {
  BranchStatus,
  ErrorLevel,
  ExecutionTreeModel,
  PredicateExecutionStatus,
  RuleExecution,
  Type
} from '../model/execution-tree.model';
import {Rule} from '../classes/rule';
import {detailGraphOptions, ruleGraphOptions} from './vis-network-options';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {FlowHierarchyService} from "../flow-hierarchy-component/flow-hierarchy.service";
import {FlowHierarchyNode} from "../flow-hierarchy-component/flow-hierarchy-node";

@Component({
  selector: 'wcc-renderer',
  templateUrl: './renderer.component.html',
  styleUrls: ['./renderer.component.scss']
})
export class RendererComponent implements OnInit {
  @Input()
  flows: Flow[] = [];

  @Input()
  focusFlowExecutionsTab: Observable<any>;

  @ViewChild('ruleNetwork') el: ElementRef;

  private ruleGraphInstance: Network;

  @ViewChild('ruleDetailNetwork') el1: ElementRef;

  private detailGraphInstance: Network;

  selectedRule: Rule;
  selectedFlow: Flow;

  flowHierarchy: FlowHierarchyNode[] = [];

  constructor(public dialog: MatDialog,
              private flowHierarchyProcessor: FlowHierarchyService,
              private changeDetector: ChangeDetectorRef,
  ) {
  }

  ngOnInit(): void {
    this.flowHierarchy = this.flowHierarchyProcessor.buildFlowHierarchy(this.flows);
  }

  onFlowSelected(flow: Flow): void {
    this.selectedFlow = flow;
    this.selectedRule = undefined;
    // Detect change to show #ruleNetwork
    this.changeDetector.detectChanges();
    this.initFlowGraph(this.selectedFlow);
  }

  initFlowGraph(selectedFlow: Flow) {
    this.clear();
    if (this.ruleGraphInstance != null) {
      console.log('this.ruleGraphInstance destroying: ', this.ruleGraphInstance.destroy());
    }

    const nodes: VisNode[] = [];
    let i = 0;

    nodes.push(this.createNodeRule(i + '', 'START', 'ellipse', 'start', 'orange', (i % 2)));
    i++;

    selectedFlow.rules.forEach(rule => {
      let color = '#D2E5FF';
      if (rule.error) {
        color = 'red';
      }
      nodes.push(this.createNodeRule(i + '', rule.name + '\n' + '(' + rule.duration + ' ms)', 'box', rule.description, color, (i % 2)));
      i++;
    });

    nodes.push(this.createNodeRule(i + '', 'END', 'ellipse', 'end', 'orange', (i % 2)));

    const edges: VisEdge[] = [];
    let j = 0;
    nodes.forEach(node => {
      if (nodes[j + 1]) {
        edges.push(this.createEdge(node.id, nodes[j + 1].id, '#D2E5FF', true));
      }
      j++;
    });

    const data = {nodes, edges};
    const container = this.el ? this.el.nativeElement : undefined;
    if (container) {
      this.ruleGraphInstance = new Network(container, data, ruleGraphOptions);
      this.ruleGraphInstance.on('click', (properties) => {
        if (properties.nodes.length > 0) {
          const index = parseInt(properties.nodes[0], 10) - 1;
          if (index >= 0 && index < selectedFlow.rules.length) {
            this.selectedRule = selectedFlow.rules[index];
            setTimeout(() => this.initGraphRuleDetail());
          }
        }
      });
    }
  }

  initGraphRuleDetail() {
    const data: VisData = {
      nodes: [],
      edges: [],
    };
    const level = 0;
    const firstNode: RuleExecution = this.selectedRule.refExecutionTree.current;

    const ruleNode = this.createNode(firstNode.id, firstNode.name, firstNode.duration,
      'ellipse', firstNode.description, 'orange', '', level, '');

    data.nodes.push(ruleNode);
    const data_: VisData = this.recursiveInitGraphRuleDetail(data, firstNode.id, this.selectedRule.refExecutionTree.children, level + 1);

    const container = this.el1.nativeElement;
    this.detailGraphInstance = new Network(container, data_, detailGraphOptions);
    this.detailGraphInstance.on('click', (properties) => {
      if (properties.nodes.length > 0) {
        const selectedNode = data.nodes.filter(node => node.id === properties.nodes[0]).pop();
        if (selectedNode && selectedNode.errorDetail.length > 0) {
          this.openDialog(selectedNode.errorDetail);
        }
      }
    });
  }

  recursiveInitGraphRuleDetail(data: VisData, parent_id: string, children: ExecutionTreeModel[], level: number): VisData {
    let data_ = data;
    children.forEach(child => {
      const img = this.getImageNode(child.current.type, child.current.executed,
        child.current.predicateExecutionStatus, child.current.error);

      const node = this.createNode(child.current.id, child.current.name, child.current.duration,
        'image', child.current.description, '', img, level, child.current.errorDetails);

      data_.nodes.push(node);
      data_.edges.push(this.createEdge(parent_id, node.id, this.getEdgeColor(child.current.branchStatus), child.current.executed));
      const id = child.current.id;
      data_ = this.recursiveInitGraphRuleDetail(data_, id, child.children, level + 1);

      if (child.lastly) {
        data_ = this.handleLastlyInGraph(child, level, data_);
      }
    });
    return data_;
  }

  private handleLastlyInGraph(child: ExecutionTreeModel, level: number, data_: VisData) {
    const imgForLastly = this.getImageNode(child.lastly.current.type, child.lastly.current.executed,
      child.lastly.current.predicateExecutionStatus, child.lastly.current.error);

    const lastlyNode: VisNode = this.createNode(child.lastly.current.id, child.lastly.current.name, child.lastly.current.duration, 'image',
      child.lastly.current.description, '', imgForLastly, level, child.current.errorDetails);

    /*    if (this.lastlyIsInRuleLevel(level)) {
          data_ = this.setLastlyExecutionInRule(data_, lastlyNode, child.lastly, level);
        } else if (this.lastlyIsInExecutionLevel(level)) {
          data_ = this.setLastlyInExecution(data_, lastlyNode, child.lastly);
        }*/

    data_ = this.setLastlyInExecution(data_, lastlyNode, child.lastly);

    return data_;
  }

  lastlyIsInRuleLevel(level: number) {
    return level === 1;
  }

  lastlyIsInExecutionLevel(level: number) {
    return level > 1;
  }


  setLastlyExecutionInRule(data: VisData, lastlyNode: VisNode, lastly: any, level: number): VisData {
    let data_ = data;
    const startLastly: VisNode = {
      id: 'lastly_',
      label: 'LASTLY',
      shape: 'box',
      title: 'lastly execute',
      color: 'orange',
      image: '',
      level: 0,
    };
    data_.nodes.push(startLastly);
    data_.nodes.push(lastlyNode);
    data_.edges.push(this.createEdge(startLastly.id, lastlyNode.id, 'purple', lastly.current.executed));
    data_ = this.recursiveInitGraphRuleDetail(data_, lastlyNode.id, lastly.children, level + 1);
    return data_;
  }

  setLastlyInExecution(data: VisData, lastlyNode: VisNode, lastly: any): VisData {
    let data_ = data;
    const last_level = this.getLastLevelOfTree(data, data_, lastlyNode, lastly);

    lastlyNode.level = last_level + 1;
    data_.nodes.push(lastlyNode);

    data_ = this.recursiveInitGraphRuleDetail(data_, lastlyNode.id, lastly.children, last_level + 2);
    return data_;
  }

  private getLastLevelOfTree(data: VisData, data_: VisData, lastlyNode: VisNode, lastly: any) {
    const leafs: VisNode[] = data.nodes.filter(node => data.edges.filter(edge => edge.from === node.id).length === 0);

    let last_level = leafs[0].level;

    leafs.forEach(leaf => {
      if (last_level < leaf.level) {
        last_level = leaf.level;
      }
      data_.edges.push(this.createEdge(leaf.id, lastlyNode.id, 'purple', lastly.current.executed));
    });
    return last_level;
  }

  createEdge(from: string, to: string, color: string, isExecuted: boolean): VisEdge {
    let width = 1;
    if (isExecuted) {
      width = 3;
    }
    return {
      from: from,
      to: to,
      color: {color: color},
      dashes: !isExecuted,
      width: width
    };
  }

  createNodeRule(id: string, label: string, shape: string, title: string, color: string, level: number): VisNode {
    if (label.length > 40) {
      label = label.replace(/(.{40})/g, '$1\n');
    }
    return {
      id: id,
      label: label,
      shape: shape,
      title: title,
      color: color,
      image: null,
      level: level
    };
  }

  createNode(id: string, label: string, time: number, shape: string, title: string, color: string, img: string, level: number, errorDetail: string): VisNode {
    if (label.length > 20) {
      label = label.replace(/(.{20})/g, '$1\n');
    }
    return {
      id: id,
      label: label + '\n(' + time + ' ms)',
      shape: shape,
      title: title,
      color: color,
      image: img,
      level: level,
      errorDetail: errorDetail
    };
  }

  clear() {
    if (this.detailGraphInstance != null) {
      this.detailGraphInstance.destroy();
    }

    const data = {nodes: [], edges: []};
    const container = this.el1 ? this.el1.nativeElement : undefined;
    if (container) {
      this.detailGraphInstance = new Network(container, data, {});
    }
  }

  getEdgeColor(branchStatus: BranchStatus): string {
    if (branchStatus === BranchStatus.Predicate_ko) {
      return 'rgba(255,0,0,0.4)';
    }
    if (branchStatus === BranchStatus.Predicate_ok) {
      return 'rgba(0,128,0,0.4)';
    }
    return '#D2E5FF';
  }

  getImageNode(type: Type, executed: boolean, predicateExecutionStatus: PredicateExecutionStatus, error: ErrorLevel): string {
    let img = '';
    if (type === Type.Predicate) {
      img = this.getImagePredicate(error, executed, predicateExecutionStatus);
    }
    if (type === Type.Action) {
      img = this.getImageAction(error, executed);
    }
    return img;
  }

  private getImagePredicate(error: ErrorLevel, executed: boolean, predicateExecutionStatus: PredicateExecutionStatus) {
    let img = '';
    const path = '../../../../../assets/img/kengine-display/';
    if (error === ErrorLevel.Error) {
      img = path + 'predicate_error.png';
    } else if (error === ErrorLevel.Warning) {
      if (predicateExecutionStatus === PredicateExecutionStatus.Ok) {
        img = path + 'predicate_warning_ok.png';
      }
      if (predicateExecutionStatus === PredicateExecutionStatus.Ko) {
        img = path + 'predicate_warning_ko.png';
      }
    } else if (!executed) {
      img = path + 'predicate_grey.png';
    } else if (executed) {

      if (predicateExecutionStatus === PredicateExecutionStatus.Ok) {
        img = path + 'predicate_ok.png';
      } else if (predicateExecutionStatus === PredicateExecutionStatus.Ko) {
        img = path + 'predicate_ko.png';
      }
    }
    return img;
  }

  private getImageAction(error: ErrorLevel, executed: boolean) {
    let img = '';
    const path = '../../../../../assets/img/kengine-display/';
    if (error === ErrorLevel.Error) {
      img = path + 'action_error.png';
    } else if (error === ErrorLevel.Warning) {
      img = path + 'action_warning.png';
    } else if (executed) {
      img = path + 'action.png';
    } else if (!executed) {
      img = path + 'action_grey.png';
    }
    return img;
  }

  openDialog(errorDetails: string): void {
    const dialogRef = this.dialog.open(DialogStackTrace, {
      data: {errorDetails: errorDetails},
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }
}


export interface DialogData {
  errorDetails: string;
}

@Component({
  selector: 'stacktrace',
  templateUrl: 'dialog-stack-trace.html',
  styleUrls: ['./renderer.component.scss']
})
export class DialogStackTrace {
  // @ts-ignore
  constructor(
    public dialogRef: MatDialogRef<DialogStackTrace>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
  ) {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }
}

export interface VisNode {
  id: string;
  label: string;
  shape: string;
  title: string;
  color: string;
  image: string;
  level: number;
  errorDetail?: string;
}

export interface VisEdge {
  from: string;
  to: string;
  dashes: boolean;
  color: { color: string };
  width: number;
}

export interface VisData {
  nodes: VisNode[];
  edges: VisEdge[];
}
