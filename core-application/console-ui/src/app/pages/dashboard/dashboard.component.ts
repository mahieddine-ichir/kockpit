import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {BreadCrumbService} from '../../kiss-components/breadcrumb/breadcrumb-service';
import {MatTableDataSource} from '@angular/material/table';
import {DashboardService} from './dashboard.service';
import {DashboardSearchRequest} from './dashboard.model';
import {Location} from '@angular/common';
import {ListColumn} from '../../core/common/list/list-column.model';
import {ConsoleConfigService} from "../../services/console-config.service";
import {PageEvent} from "@angular/material/paginator";

@Component({
  selector: 'wcc-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  dataSource: MatTableDataSource<DashboardSearchRequest> | null;
  columns: ListColumn[];
  auditDashboard: any;
  auditTimeline: any;

  searchKey: string;
  searchValue: string;
  pageIndex = 0;
  pageLength = 0;
  pageFrom = 0;
  pageSize = 25;

  // needed to fetch appropriate config from audit
  auditViewName: string;
  domain: string;
  env: string;

  constructor(
    public breadCrumbService: BreadCrumbService,
    private route: ActivatedRoute,
    private service: DashboardService,
    private location: Location,
    private consoleConfigService: ConsoleConfigService,
  ) {
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.searchKey = params['key'];
      this.searchValue = params['value'];
      this.auditViewName = params['auditViewName'];
      this.env = params['env'];
      this.loadConfiguration();
      this.loadData();
    });
  }

  private loadConfiguration() {
    const audit = this.consoleConfigService.getConsoleConfigForService('audit');
    if (audit && audit.config) {
      const auditView = audit.config.auditViews.find((av) => {
        return av.name === this.auditViewName && av.env === this.env;
      });
      this.auditDashboard = auditView.dashboardColumns;
      this.auditTimeline = auditView.dashboardTimelineConfiguration
    }
  }

  loadData() {
    this.dataSource = new MatTableDataSource<DashboardSearchRequest>();
    if (this.searchKey) {
      this.service.loadData(this.searchKey, this.searchValue, this.pageFrom, this.pageSize)
        .subscribe(value => {
          this.dataSource.data = value.items;
          this.pageLength = value.totalSize;
        });
    }
  }

  onPageEvent(event?: PageEvent) {
    this.pageSize = event?.pageSize;
    this.pageFrom = event?.pageIndex * this.pageSize;
    this.loadConfiguration();
    this.loadData();
  }

  navigateBack() {
    this.location.back();
  }
}
