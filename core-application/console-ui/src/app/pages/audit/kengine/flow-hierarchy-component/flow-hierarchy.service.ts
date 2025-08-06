import {FlowHierarchyNode} from "./flow-hierarchy-node";
import {Flow} from "../classes/flow";
import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class FlowHierarchyService {

  buildFlowHierarchy(flows: Flow[]): FlowHierarchyNode[] {
    // Trier les flows par startTime
    flows.sort((a, b) => a.startTime - b.startTime);

    const rootFlows: FlowHierarchyNode[] = [];
    const stack: FlowHierarchyNode[] = [];

    flows.forEach(flow => {
      const node = new FlowHierarchyNode(flow);

      // Remonter dans la pile pour trouver le bon parent
      while (stack.length > 0 && !this.isChild(node, stack[stack.length - 1])) {
        stack.pop();
      }

      if (stack.length > 0) {
        // Ajouter le flow comme enfant du dernier flow dans la pile
        stack[stack.length - 1].addChild(node);
      } else {
        // Si pas de parent, c'est un flow racine
        rootFlows.push(node);
      }

      // Ajouter le node à la pile
      stack.push(node);
    });

    return rootFlows;
  }

  private isChild(child: FlowHierarchyNode, parent: FlowHierarchyNode): boolean {
    return child.flow.startTime >= parent.flow.startTime && child.flow.endTime <= parent.flow.endTime;
  }
}
