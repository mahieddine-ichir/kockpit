import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {DashboardSearchRequest, DashboardTimelineProperty, ExecutionLagTime} from "../dashboard.model";
import {BreadCrumbService} from "../../../kiss-components/breadcrumb/breadcrumb-service";
import * as _ from 'lodash';

@Component({
  selector: 'wcp-timeline-view',
  templateUrl: './timeline-view.component.html',
  styleUrls: ['./timeline-view.component.scss']
})
export class TimelineViewComponent implements OnInit, OnChanges {

  @Input() dataSource: MatTableDataSource<DashboardSearchRequest> | null;
  @Input() auditTimelineConfiguration: any;

  timelineProperties: DashboardTimelineProperty[] = [];
  timelineDisplayColumns: string[] = [];
  timelineLagConfig: ExecutionLagTime;

  timelineMinTime: number;
  timelineMaxTime: number;

  expandExecutionColumn: boolean = false;

  constructor(
    public breadCrumbService: BreadCrumbService,
  ) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.dataSource && changes.dataSource.currentValue) {
      this.dataSource = changes.dataSource.currentValue;
    }
    if (changes.auditTimelineConfiguration && changes.auditTimelineConfiguration.currentValue) {
      this.auditTimelineConfiguration = changes.auditTimelineConfiguration.currentValue;
      this.loadConfiguration();
    }
  }

  ngOnInit(): void {
    this.loadConfiguration();
    this.computeTime();
  }

  calculateExecutionDuration(row: DashboardSearchRequest): number {
    return new Date(row.end).getTime() - new Date(row.start).getTime();
  }

  calculateExecutionPercentage(row: DashboardSearchRequest, totalStartTime: number, totalEndTime: number): number {
    const executionDuration = this.calculateExecutionDuration(row);
    return (executionDuration / this.totalDuration(totalStartTime, totalEndTime)) * 100;
  }

  calculateExecutionOffsetPercentage(row: DashboardSearchRequest, totalStartTime: number, totalEndTime: number): number {
    const offsetDuration = new Date(row.start).getTime() - totalStartTime;
    return (offsetDuration / this.totalDuration(totalStartTime, totalEndTime)) * 100;
  }

  calculateLagPercentage(row: DashboardSearchRequest, totalStartTime: number, totalEndTime: number): number {
    const lagDuration = new Date(row.start).getTime() - this.lag(row);
    return (lagDuration / this.totalDuration(totalStartTime, totalEndTime)) * 100;
  }

  calculateLagOffsetPercentage(row: DashboardSearchRequest, totalStartTime: number, totalEndTime: number): number {
    const lagTime = this.lag(row);
    const offsetDuration = lagTime - totalStartTime;
    return (offsetDuration / this.totalDuration(totalStartTime, totalEndTime)) * 100;
  }

  calculateLagDuration(row: DashboardSearchRequest): number {
    return new Date(row.start).getTime() - this.lag(row);
  }

  private totalDuration(totalStartTime: number, totalEndTime: number): number {
    return totalEndTime - totalStartTime;
  }

  private loadConfiguration() {
    if (this.auditTimelineConfiguration) {
      const dashboardTimelineConfiguration = this.auditTimelineConfiguration;
      if (dashboardTimelineConfiguration.displayedProperties) {
        this.timelineProperties = dashboardTimelineConfiguration.displayedProperties.map(col => {
          const val = new DashboardTimelineProperty();
          val.name = col.label;
          val.properties = col.properties;
          val.displayFn = col.renderer;
          return val;
        });
        this.timelineDisplayColumns = [...this.timelineProperties.map(col => col.name),
          'execution'];
      }

      if (dashboardTimelineConfiguration.executionLag) {
        const executionLag = dashboardTimelineConfiguration.executionLag;
        if (executionLag) {
          this.timelineLagConfig = {
            property: executionLag.property,
            source: executionLag.source
          };
        }
      }
    }
  }

  private computeTime() {
    this.computeMinTime();
    this.computeMaxTime();
  }

  private readStartTime(data: DashboardSearchRequest) {
    return new Date(data.start).getTime();
  }

  private readEndTime(data: DashboardSearchRequest) {
    return new Date(data.end).getTime();
  }

  private readWithConfig(data: DashboardSearchRequest, config: ExecutionLagTime) {
    if (!config) {
      return null;
    }
    const value = this.fetchFromIndexedKeyValue(data, config.property);
    if (value) {
      return new Date(value).getTime();
    } else {
      return value;
    }
  }

  private computeMaxTime() {
    let max = Number.MIN_VALUE;
    this.dataSource.data.forEach(value => {
      const val = this.readEndTime(value);
      max = Math.max(max, new Date(val).getTime());
    });
    this.timelineMaxTime = max;
  }

  private computeMinTime() {
    let min = Number.MAX_VALUE;
    this.dataSource.data.forEach(row => {
      let val = this.readWithConfig(row, this.timelineLagConfig);
      if (!val) {
        val = this.readStartTime(row);
      }
      min = Math.min(min, new Date(val).getTime());
    });
    this.timelineMinTime = min;
  }

  private fetchFromIndexedKeyValue(row: DashboardSearchRequest, propertyKey: string): string {
    const indexedKeyValues = row.indexedKeyValues;
    return _.chain(indexedKeyValues)
      .find(indexKeyValue => indexKeyValue && indexKeyValue.key === propertyKey)
      .thru(field => (field ? field.value : null))
      .value();
  }

  private lag(row: DashboardSearchRequest): number {
    const start = this.readWithConfig(row, this.timelineLagConfig);
    return new Date(start).getTime();
  }

  hasLag(row: DashboardSearchRequest): boolean {
    return this.timelineLagConfig && this.readWithConfig(row, this.timelineLagConfig) !== null;
  }

  toggleExpandCollapseColumn() {
    this.expandExecutionColumn = !this.expandExecutionColumn;
  }
}
