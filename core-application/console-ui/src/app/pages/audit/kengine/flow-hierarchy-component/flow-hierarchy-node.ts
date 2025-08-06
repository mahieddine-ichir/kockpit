import {Flow} from "../classes/flow";

export class FlowHierarchyNode {
  flow: Flow;
  children: FlowHierarchyNode[] = [];
  isOpen: boolean = false;

  constructor(flow: Flow) {
    this.flow = flow;
  }

  addChild(child: FlowHierarchyNode) {
    this.children.push(child);
  }

  toggle() {
    this.isOpen = !this.isOpen;
  }
}
