import {AfterViewInit, Component, OnDestroy, OnInit} from '@angular/core';
import {fadeOutAnimation} from '../../core/common/route.animation';
import {FormBuilder} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {BreadCrumbService} from '../../kiss-components/breadcrumb/breadcrumb-service';
import {MatDialog} from '@angular/material/dialog';
import {CacheService} from './cache.service';
import {MatTableDataSource} from '@angular/material/table';
import {ListColumn} from '../../core/common/list/list-column.model';
import {TopologyService} from '../topology/topology.service';
import {CacheCommand, CacheCommands, CacheMetric, CacheState, InstanceCacheState} from './cache.model';
import {DatePipe, DecimalPipe} from '@angular/common';


@Component({
  selector: 'fury-cache-table',
  templateUrl: './cache.component.html',
  styleUrls: ['./cache.component.scss'],
  animations: [fadeOutAnimation],
  host: {'[@fadeOutAnimation]': 'true'},
  providers: [DatePipe, DecimalPipe]
})

export class CacheComponent implements OnInit, AfterViewInit, OnDestroy {
  private cacheName: string;
  private applicationId: string;
  pageSize = 50;
  statsDataSource: MatTableDataSource<InstanceCacheState> | null;
  commandsDataSource: MatTableDataSource<CacheCommand> | null;
  statsColumns: ListColumn[] = [];
  statsDisplayedColumns: string[] = [];
  commandsColumns: ListColumn[] = [];
  commandsDisplayedColumns: string[] = ['dateTime', 'command', 'cacheInstanceOperationResults'];

  domain: string;
  env: string;
  cacheState: CacheState;
  cacheCommands: CacheCommands;

  constructor(private dialog: MatDialog,
              private fb: FormBuilder,
              private router: Router,
              private topologyService: TopologyService,
              private cacheService: CacheService,
              public breadCrumbService: BreadCrumbService,
              private route: ActivatedRoute,
              private datepipe: DatePipe,
              private decimalPipe: DecimalPipe
  ) {
  }

  ngOnInit() {

    this.statsDataSource = new MatTableDataSource();
    this.commandsDataSource = new MatTableDataSource();
    // Listen changes
    this.route.params.subscribe(params => {
      this.fetchRouteParams();
      this.initBreadCrumb();
      this.fetchData();
    });
  }

  fetchRouteParams() {
    this.applicationId = this.route.snapshot.params['applicationId'];
    this.domain = this.route.snapshot.params['domain'];
    this.env = this.route.snapshot.params['env'];
    this.cacheName = this.route.snapshot.params['cacheName'];
  }

  emptyCache(): void {
    this.cacheService.emptyCache(this.domain, this.env, this.applicationId, this.cacheName).subscribe({
      complete: () => this.reloadComponent()
    });
  }

  resetStats(): void {
    this.cacheService.resetStats(this.domain, this.env, this.applicationId, this.cacheName).subscribe({
      complete: () => this.reloadComponent()
    });
  }

  fetchData(): void {
    this.cacheService.getCacheState(this.domain, this.env, this.applicationId, this.cacheName).subscribe({
      next: (data) => {
        if (data) {
          this.cacheState = data;
          this.statsDataSource.data = data.instanceCacheStates;
          this.fetchStatsTableColumns();
        }
      }
    });
    this.cacheService.getCommands(this.domain, this.env, this.applicationId, this.cacheName).subscribe({
      next: (data) => {
        if (data) {
          this.cacheCommands = data;
          this.commandsDataSource.data = data.cacheCommands.reverse();
        }
      }
    });
  }

  fetchStatsTableColumns(): void {
    if (this.cacheState.columns) {
      this.statsColumns = this.cacheState.columns.map(col => {
        const listColumn = new ListColumn();
        listColumn.name = col;
        return listColumn;
      });
      const filteredColumns =
        ['timestamp', 'cacheGets', 'cacheHits', 'cacheMisses', 'cacheHitPercentage', 'cacheMissPercentage'];
      this.statsDisplayedColumns =
        this.statsColumns.filter(
          c => filteredColumns.indexOf(c.name) === -1).map(c => c.name);
      this.statsDisplayedColumns.unshift('Gets / Hits / Misses');
      this.statsDisplayedColumns.unshift('Last data received at');
      this.statsDisplayedColumns.unshift('Instance');
    }
  }


  breadCrumbStatusManager() {
    const currentBreadCrumbItem = {
      title: 'Cache',
      link: '/cache',
      icon: 'view_list',
      action: '1',
      data: {}
    };

    if (this.breadCrumbService.breadcrumb.length > 1) {
      this.breadCrumbService.reset(currentBreadCrumbItem.title);
    }

    this.breadCrumbService.emitChangeItem(currentBreadCrumbItem);
  }

  ngAfterViewInit() {
  }

  ngOnDestroy() {
    this.reset();
  }

  reset() {
    this.statsDataSource = new MatTableDataSource();
    this.commandsDataSource = new MatTableDataSource();
  }

  displayStatsElement(element, displayedColumns) {

    const statistics = element.cacheMetrics;
    let cacheMetric: CacheMetric;

    if (displayedColumns === 'Instance') {
      return element.instanceId;
    }

    if (displayedColumns === 'avgGetT') {
      cacheMetric = statistics.filter(value => {
        return value.name === 'averageGetTime';
      });
    }

    if (displayedColumns === 'avgPutT') {
      cacheMetric = statistics.filter(value => {
        return value.name === 'averagePutTime';
      });
    }

    if (displayedColumns === 'avgRemoveT') {
      cacheMetric = statistics.filter(value => {
        return value.name === 'averageRemoveTime';
      });
    }

    if (displayedColumns === 'cacheRemove') {
      cacheMetric = statistics.filter(value => {
        return value.name === 'cacheRemovals';
      });
    }

    if (displayedColumns === 'cacheEvict') {
      cacheMetric = statistics.filter(value => {
        return value.name === 'cacheEvictions';
      });
    }

    if (displayedColumns === 'Last data received at') {

      if (!statistics.some(item => item.name === 'timestamp')) {
        return '-';
      }

      const timestamp = statistics.filter(value => {
        return value.name === 'timestamp';
      });

      return this.datepipe.transform(new Date(timestamp[0].value), 'yyyy-MM-dd HH:mm:ss');
    }

    if (!cacheMetric) {
      cacheMetric = statistics.filter(value => {
        return value.name === displayedColumns;
      });
    }
    let value = this.decimalPipe.transform(cacheMetric[0].value, '1.0-0', 'fr-FR');
    if (displayedColumns === 'avgGetT' || displayedColumns === 'avgPutT' || displayedColumns === 'avgRemoveT') {
      value += ' ms';
    }
    return value;
  }

  displayCommandsElement(element, displayedColumns) {
    if (displayedColumns === 'dateTime' && element[displayedColumns] !== null && element[displayedColumns] !== undefined) {
      return this.datepipe.transform(new Date(element[displayedColumns]), 'yyyy-MM-dd HH:mm:ss');
    }
    const field = element[displayedColumns];
    if (typeof field === 'object' && field !== null && field !== undefined) {
      return field.map(f => {
        const date = this.datepipe.transform(new Date(f.dateTime), 'yyyy-MM-dd HH:mm:ss');
        return f.instanceId + ' : ' + f.result + ' at ' + date + '<br>';
      });
    }
    return field ? field : '';
  }

  private initBreadCrumb() {
    const currentBreadCrumbItem = {
      title: `Cache ${this.cacheName}`,
      link: `/services/cache/${this.domain}/${this.env}/${this.applicationId}/${this.cacheName}`,
      icon: 'view_list',
      action: '1',
      data: {}
    };

    this.breadCrumbService.reset(currentBreadCrumbItem.title);
    this.breadCrumbService.emitChangeItem(currentBreadCrumbItem);
  }

  reloadComponent() {
    const currentUrl = this.router.url;
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    this.router.onSameUrlNavigation = 'reload';
    this.router.navigate([currentUrl]);
  }
}
