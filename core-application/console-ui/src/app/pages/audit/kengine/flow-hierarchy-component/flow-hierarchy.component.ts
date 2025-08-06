import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FlowHierarchyNode} from "./flow-hierarchy-node";
import {FlatTreeControl} from "@angular/cdk/tree";
import {MatTreeFlatDataSource, MatTreeFlattener} from "@angular/material/tree";
import {Flow} from "../classes/flow";
import {first, last} from "lodash-es";

interface FlatNode {
  expandable: boolean;
  name: string;
  level: number;
  startTime: number;
  endTime: number;
  flow: Flow;
}

@Component({
  selector: 'app-flow-hierarchy',
  templateUrl: './flow-hierarchy.component.html',
  styleUrls: ['./flow-hierarchy.component.scss']
})
export class FlowHierarchyComponent implements OnInit {
  @Input() hierarchy: FlowHierarchyNode[] = [];
  @Output() flowSelected = new EventEmitter<Flow>();

  treeControl = new FlatTreeControl<FlatNode>(
    node => node.level, node => node.expandable
  );

  treeFlattener: MatTreeFlattener<FlowHierarchyNode, FlatNode>;

  dataSource: MatTreeFlatDataSource<FlowHierarchyNode, FlatNode>;

  selectedFlow: Flow | null = null;
  allNodesExpanded = false;
  timelineMinTime: number;
  timelineMaxTime: number;

  constructor() {
    this.treeFlattener = new MatTreeFlattener(
      this.transformer,
      node => node.level,
      node => node.expandable,
      node => node.children
    );

    this.dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
  }

  ngOnInit(): void {
    this.dataSource.data = this.hierarchy;
    setTimeout(() => this.expandAndSelectFirstElement(), 0);
    this.computeMaxTime();
    this.computeMinTime();
  }

  showToggle(): boolean {
    return this.treeControl.dataNodes.length > 1;
  }

  toggleExpandCollapse(): void {
    if (this.allNodesExpanded) {
      this.treeControl.collapseAll();
    } else {
      this.treeControl.expandAll();
    }
    this.allNodesExpanded = !this.allNodesExpanded;
  }

  expandAndSelectFirstElement(): void {
    if (this.treeControl.dataNodes.length > 0) {
      const firstNode = this.treeControl.dataNodes[0];
      this.onFlowClick(firstNode);
    }
  }

  onFlowClick(node: FlatNode): void {
    if (this.treeControl.isExpanded(node)) {
      this.treeControl.collapseDescendants(node);
    } else {
      this.treeControl.expand(node);
    }
    this.selectedFlow = node.flow;
    this.flowSelected.emit(node.flow);
  }

  transformer = (node: FlowHierarchyNode, level: number) => {
    return {
      expandable: !!node.children && node.children.length > 0,
      name: node.flow.name,
      level: level,
      startTime: node.flow.startTime,
      endTime: node.flow.endTime,
      flow: node.flow
    };
  };

  hasChild = (_: number, node: FlatNode) => node.expandable;

  private computeMaxTime() {
    this.timelineMaxTime = new Date(last(this.hierarchy).flow.endTime).getTime();
  }

  private computeMinTime() {
    this.timelineMinTime = new Date(first(this.hierarchy).flow.startTime).getTime();
  }

  calculateExecutionDuration(row: FlatNode): number {
    const duration = new Date(row.endTime).getTime() - new Date(row.startTime).getTime();
    return duration > 0 ? duration : 1;
  }

  calculateExecutionPercentage(row: FlatNode, totalStartTime: number, totalEndTime: number): number {
    const executionDuration = this.calculateExecutionDuration(row);
    console.log((executionDuration / this.totalDuration(totalStartTime, totalEndTime)) * 100);
    return (executionDuration / this.totalDuration(totalStartTime, totalEndTime)) * 100;
  }

  calculateExecutionOffsetPercentage(row: FlatNode, totalStartTime: number, totalEndTime: number): number {
    const offsetDuration = new Date(row.startTime).getTime() - totalStartTime;
    return (offsetDuration / this.totalDuration(totalStartTime, totalEndTime)) * 100;
  }

  private totalDuration(totalStartTime: number, totalEndTime: number): number {
    const totalDuration = totalEndTime - totalStartTime;
    return totalDuration > 0 ? totalDuration : 1;
  }

  flowStatusColor(status: string) {
    switch (status) {
      case 'VALID':
        return 'green';
      case 'WARNING':
        return 'orange';
      case 'ERROR':
      case 'BADREQUEST':
        return 'red';
      default:
        return '';
    }
  }

  getTimelineColor(node: any): string {
    switch (node.flow.status) {
      case 'VALID':
        return '#4caf50'; // Vert pour les flows complétés
      case 'WARNING':
        return '#ff9800'; // Orange pour les flows en cours
      case 'ERROR':
      case 'BADREQUEST':
        return '#f44336'; // Rouge pour les flows échoués
      default:
        return '#e0e0e0'; // Gris par défaut
    }
  }
}
