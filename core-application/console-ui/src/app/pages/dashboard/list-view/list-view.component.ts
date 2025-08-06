import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {DashboardSearchRequest} from "../dashboard.model";
import {ListColumn} from "../../../core/common/list/list-column.model";
import {BreadCrumbService} from "../../../kiss-components/breadcrumb/breadcrumb-service";

@Component({
  selector: 'wcp-list-view',
  templateUrl: './list-view.component.html',
  styleUrls: ['./list-view.component.scss']
})
export class ListViewComponent implements OnInit, OnChanges {

  @Input() dataSource: MatTableDataSource<DashboardSearchRequest> | null;
  @Input() auditDashboardConfiguration: any;
  columns: ListColumn[];
  displayedColumns: string[] = [];

  constructor(
    public breadCrumbService: BreadCrumbService,
  ) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.dataSource && changes.dataSource.currentValue) {
      this.dataSource = changes.dataSource.currentValue;
    }
    if (changes.auditDashboardConfiguration && changes.auditDashboardConfiguration.currentValue) {
      this.auditDashboardConfiguration = changes.auditDashboardConfiguration.currentValue;
      this.loadConfiguration();
    }
  }

  ngOnInit(): void {
    this.loadConfiguration();
  }

  private loadConfiguration() {
    if (this.auditDashboardConfiguration) {
      this.columns = this.auditDashboardConfiguration.map(col => {
        const listColumn = new ListColumn();
        listColumn.name = col.label;
        listColumn.property = col.name;
        listColumn.displayFn = col.renderer;
        return listColumn;
      });
      this.displayedColumns = this.columns.map(col => col.property);
    }
  }
}
