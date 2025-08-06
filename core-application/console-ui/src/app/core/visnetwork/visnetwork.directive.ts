import {Directive, ElementRef, EventEmitter, Input, Output} from '@angular/core';

declare var vis: any;

const DEFAULT_OPTIONS = {
  layout: {
    hierarchical: {
      sortMethod: 'directed',
      nodeSpacing: 300,
      treeSpacing: 150,
    }
  },
  edges: {
    smooth: true,
    arrows: {
      to: true
    }
  }
};

@Directive({
  selector: '[wccVisnetwork]'
})
export class VisnetworkDirective {

  @Input()
  public nodes: any;

  @Input()
  public edges: any;

  @Input()
  public options: any;

  @Output()
  click = new EventEmitter<any>();

  private _graph: any;

  constructor(private _element: ElementRef) {
  }

  public createGraph(newNodes: any, newEdges: any) {
    const container = this._element.nativeElement;

    // create a network
    const data = {
      nodes: new vis.DataSet(newNodes),
      edges: new vis.DataSet(newEdges)
    };

    let options = this.options;
    if (options == null) {
      options = DEFAULT_OPTIONS;
    }

    if (!this._graph) {
      this._graph = new vis.Network(container, data, options);
      const clicker = this.click;
      this._graph.on("click", function (params) {
        const nodeIndex = params.nodes[0];
        if (nodeIndex) {
          clicker.emit(data.nodes.get(nodeIndex));
        }
      });
    } else {
      this._graph.setData(data);
      this._graph.redraw();
    }
  }
}
