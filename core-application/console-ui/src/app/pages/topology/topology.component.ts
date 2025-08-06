import {AfterViewInit, Component, OnDestroy, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {fadeOutAnimation} from '../../core/common/route.animation';
import {FormBuilder} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {BreadCrumbService} from '../../kiss-components/breadcrumb/breadcrumb-service';
import {MatDialog} from '@angular/material/dialog';
import {ConsoleConfigService} from '../../services/console-config.service';
import {TopologyService} from './topology.service';
import {VisnetworkDirective} from '../../core/visnetwork/visnetwork.directive';
import {VistopologyDirective} from './vistopology.directive';


@Component({
  selector: 'fury-all-in-one-table',
  templateUrl: './topology.component.html',
  styleUrls: ['./topology.component.scss'],
  animations: [fadeOutAnimation],
  host: {'[@fadeOutAnimation]': 'true'}
})

export class TopologyComponent implements OnInit, AfterViewInit, OnDestroy {
  domain: string;
  env: string;

  topologyOptions = {
    autoResize: true,
    layout: {
      hierarchical: {
        direction: 'LR',
        nodeSpacing: 300,
        treeSpacing: 300,
        levelSeparation: 250,
      }
    },
    edges: {
      smooth: true,
      arrows: {
        to: true
      }
    }
  };

  @ViewChild(VistopologyDirective)
  private graph: VistopologyDirective;

  constructor(private dialog: MatDialog,
              private fb: FormBuilder,
              private router: Router,
              private topologyService: TopologyService,
              public breadCrumbService: BreadCrumbService,
              private route: ActivatedRoute,
              private consoleConfigService: ConsoleConfigService
  ) {
  }

  ngOnInit() {
    this.domain = this.route.snapshot.params['domain'];
    this.env = this.route.snapshot.params['env'];
    this.route.params.subscribe(params => {
      console.log('params:', params);

      this.domain = this.route.snapshot.params['domain'];
      this.env = this.route.snapshot.params['env'];
      this.breadCrumbStatusManager();
      this.reset();
    });

    this.reset();
  }

  breadCrumbStatusManager() {
    const currentBreadCrumbItem = {
      title: 'Topology',
      link: '/topology',
      icon: 'view_list',
      action: '1',
      data: {}
    };

    if (this.breadCrumbService.breadcrumb.length > 1) {
      this.breadCrumbService.reset(currentBreadCrumbItem.title);
    }

    this.breadCrumbService.emitChangeItem(currentBreadCrumbItem);
  }

  ngAfterViewInit() {}

  ngOnDestroy() {
    this.reset();
  }

  reset() {
    this.topologyService.topology(this.domain, this.env).subscribe(value => {
      console.log('value: ', value);
      // For each application
      const nodes = [];
      const edges = [];
      for (let i = 0; i < value.length; i++) {
        const appTopology = value[i];
        let color = '#FFFFFF';
        if (appTopology.instances.length <= 0) {
          color = 'rgba(255,0,0,0.65)';
        } else if (appTopology.instances.length > 1) {
          color = '#b6d8bf';
        }
        nodes.push({ id: i, label: appTopology.applicationId, color: color });
        for (let j = 0; j < appTopology.instances.length; j++) {
          const appInstance = appTopology.instances[j];
          const appInstanceId = `${i}-${j}`;
          nodes.push({ id: appInstanceId, label: appInstance.instanceId });
          edges.push({ from: i, to: appInstanceId });
        }
      }
      this.graph.createGraph(nodes, edges);
    });
  }

  selectTopology(e) {
    console.log('select topology: ', e);
  }
}
