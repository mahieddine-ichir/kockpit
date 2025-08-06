import {AfterViewInit, Component, ElementRef, Input, OnDestroy, OnInit, Renderer2, ViewChild} from '@angular/core';
import {fadeOutAnimation} from '../../core/common/route.animation';
import {FormBuilder, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AuditRequest, AuditRequestPage} from './request.model';
import {RequestService} from './request.service';
import {BreadCrumbService} from '../../kiss-components/breadcrumb/breadcrumb-service';
import {CopyService} from '../../services/copy.service';
import {MatTable, MatTableDataSource} from '@angular/material/table';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, Sort} from '@angular/material/sort';
import {MatDialog} from '@angular/material/dialog';
import {ListColumn} from '../../core/common/list/list-column.model';
import {ConsoleConfigService} from '../../services/console-config.service';


@Component({
  selector: 'wcc-fury-all-in-one-table',
  templateUrl: './request.component.html',
  styleUrls: ['./request.component.scss'],
  animations: [fadeOutAnimation],
  host: { '[@fadeOutAnimation]': 'true' }
})

export class RequestComponent implements OnInit, AfterViewInit, OnDestroy {

  searchMap: Map<string, string> = new Map();
  @Input() mdDatepicker: Date;

  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  @ViewChild(MatSort, { static: true }) sort: MatSort;
  @ViewChild(MatTable, {read: ElementRef}) private matTableRef: ElementRef;

  pageLength = 0;
  pageSize = 50;
  pageFrom = 0;
  pageIndex = 0;

  dataSource: MatTableDataSource<AuditRequest> | null;
  columns: ListColumn[];
  displayedColumns: string[] = [];
  searchCriteria = [];

  traceId: string;
  resultStatus: string[];
  startDate: Date;
  endDate: Date;
  limit: string;
  status: string;
  type: string;
  origin: string;

  auditViewName: string;
  domain: string;
  env: string;

  breadCrumbTitle = '';
  resize = false;

  options: FormGroup;

  types: string[] = ['Error', 'Warning'];
  hideColumns: string[] = ['result'];

  table: HTMLElement;
  localStorageKeyForSizingColumn: string;

  constructor(private dialog: MatDialog,
    private fb: FormBuilder,
    private router: Router,
    private requestService: RequestService,
    public breadCrumbService: BreadCrumbService,
    private route: ActivatedRoute,
    private consoleConfigService: ConsoleConfigService, private renderer: Renderer2, private el: ElementRef
  ) {

    this.options = fb.group({
      hideRequired: false,
      floatLabel: 'auto',
    });
  }

  ngOnInit() {

    this.auditViewName = this.route.snapshot.params['auditViewName'];
    this.table = document.querySelector('.mat-table') as HTMLElement;
    this.domain = this.route.snapshot.params['domain'];
    this.env = this.route.snapshot.params['env'];

    this.route.params.subscribe(params => {

      this.auditViewName = this.route.snapshot.params['auditViewName'];
      this.domain = this.route.snapshot.params['domain'];
      this.env = this.route.snapshot.params['env'];
      this.localStorageKeyForSizingColumn = 'audit-' + this.auditViewName;

      const consoleConfigForService = this.consoleConfigService.getConsoleConfigForService('audit');
      const auditView = consoleConfigForService.config.auditViews.find((av) => {
        return av.name === this.auditViewName
          && av.env === this.env;
        // TODO add av.domain
      });

      // Columns
      this.columns = auditView.resultColumns.map(col => {
        const listColumn = new ListColumn();
        listColumn.name = col.label;
        listColumn.property = col.name;
        listColumn.displayFn = col.renderer;
        listColumn.dashboard = col.dashboard;
        return listColumn;
      });

      // Add an action column (for select row click action)
      const actionColumn = new ListColumn();
      actionColumn.property = 'details';
      actionColumn.name = '#';
      actionColumn.displayFn = 'details';
      actionColumn.resizable = false;

      this.columns.push(actionColumn);

      // columns to display
      this.displayedColumns = this.columns
        .filter(col => this.hideColumns.some(c => c !== col.property))
        .map(col => col.property);

      this.searchCriteria = auditView.searchMetadatas;
      this.breadCrumbTitle = params.app ? params.app : 'Request';
      this.breadCrumbStatusManager();
      const that = this;
      setTimeout(function () { that.sizingColumnFromLocalStorage(); }, 200);
    });

    this.dataSource = new MatTableDataSource();
    this.dataSource.filterPredicate = (data: AuditRequest, filter: string) => {
      const extensions = data['indexedKeyValues'];
      return extensions.find(el => el.value && el.value.trim().toLowerCase().indexOf(filter) !== -1);
    };
    /*
    this.dataSource.sortingDataAccessor = (item, property) => {
      console.log(`sortingAccessor, property: ${property} - ${item[property]}`);
      return item[property];
    };
     */

    this.reset();
  }

  breadCrumbStatusManager() {
    const currentBreadCrumbItem = {
      title: this.breadCrumbTitle,
      link: `/services/audit/${this.domain}/${this.env}/${this.auditViewName}`,
      icon: 'view_list',
      action: '1',
      data: {}
    };

    this.breadCrumbService.reset(currentBreadCrumbItem.title);
    this.breadCrumbService.emitChangeItem(currentBreadCrumbItem);
  }

  ngAfterViewInit() {
    //this.dataSource.sort = this.sort;
    this.sizingColumnFromLocalStorage();
  }

  onFilterChange(value) {
    if (!this.dataSource) {
      return;
    }
    this.dataSource.filter = value.trim().toLowerCase();
  }

  ngOnDestroy() {
    this.reset();
  }

  reset() {
    this.endDate = null;
    this.limit = null;
    this.resultStatus = null;
    this.startDate = null;
    this.endDate = null;
    this.traceId = null;
  }

  copy(val, event) {
    CopyService.copy(val, event);
  }

  select(row) {
    const path = '/services/audit/' + this.domain + '/' + this.env + '/' + this.auditViewName + '/detail/' + row.requestId;
    this.router.navigate([path]).then();
  }

  onBeginSearch() {
    this.dataSource.data = [];
  }

  onEndSearch($event: AuditRequestPage) {
    this.dataSource.data = $event.items;
    this.pageFrom = $event.from;
    this.pageSize = $event.size;
    this.pageLength = $event.totalSize;
  }

  displayName(col: ListColumn): string {
    return col.name;
  }

  resultColor(rowElement) {
    switch (rowElement) {
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

  httpStatusColor(rowElement) {
    if (rowElement < 400) {
      return 'green';
    }
    if (rowElement < 500) {
      return 'orange';
    }
    return 'red';
  }

  getId(row, key) {
    const val = this.getIndexedKeyValue(row, key);
    if (val != null && val.length > 0 && val !== 'null') {
      return '*' + val.replace(/\S(?=\S{6})/g, '');
    }
    return '';
  }

  getIndexedKeyValue(row, key) {
    const extensions = row['indexedKeyValues'];
    const field = extensions.find(el => el && el.key === key);
    return field ? field.value : '';
  }

  getResult(row, key) {
    const extensions = row['indexedKeyValues'];
    const fields = extensions.filter(el => el && el.key === key);
    if (fields.length === 1) {
      return fields[0].value;
    }
    if (fields.length > 1) {
      if (fields.find(el => el && ( el.value === 'ERROR' || el.value === 'BADREQUEST'))) {
        return 'ERROR';
      }
      if (fields.find(el => el && el.value === 'WARNING')) {
        return 'WARNING';
      }
      if (fields.find(el => el && el.value === 'VALID')) {
        return 'VALID';
      }
    }
    return '';
  }

  copyMessage(row, key: string) {
    const selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = this.getIndexedKeyValue(row, key);
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
  }

  sizingColumnFromLocalStorage() {
    if (localStorage.getItem(this.localStorageKeyForSizingColumn) == null) {
      const map = new Map<number, number>();
      localStorage.setItem(this.localStorageKeyForSizingColumn, JSON.stringify(map));
    } else {
      const retrievedObject = localStorage.getItem(this.localStorageKeyForSizingColumn);
      const columnsWidth = new Map(Object.entries(JSON.parse(retrievedObject)));

      const htmlTableRowElement = document.querySelector('.mat-table').querySelector('tr');
      if (htmlTableRowElement) {
        const header = htmlTableRowElement.querySelectorAll('th');
        columnsWidth.forEach((values, key) => {
          this.renderer.setStyle(header.item(Number(key)), 'width', `${values}px`);
        });
      }
    }
  }

  gotoDashboard(row: AuditRequest, col: ListColumn) {
    const key = col.property;
    let value = row[key];
    if (value === undefined) {
      const extensions = row['indexedKeyValues'];
      value = extensions.find(el => el && el.key === key).value;
    }
    if (value) {
      const queryParams = [];
      queryParams['key'] = key;
      queryParams['value'] = value;
      queryParams['auditViewName'] = this.auditViewName;
      queryParams['env'] = this.env;
      this.router.navigate(['/services/audit/dashboard'], {
        queryParams: queryParams
      }).then();
    }
  }

  onPageEvent(event: PageEvent) {
    this.pageSize = event?.pageSize;
    this.pageFrom = event?.pageIndex * this.pageSize;
    this.pageLength = event?.length;
  }

  sortData(sort: Sort) {
    if (sort.active === 'details') {
      return;
    }
    this.dataSource.data = this.dataSource.data.sort((a, b) => {
      if (a.hasOwnProperty(sort.active)) {
        return this.compare(a[sort.active], b[sort.active], sort.direction === 'asc');
      } else {
          return this.compare(this.getIndexedKeyValue(a, sort.active), this.getIndexedKeyValue(b, sort.active),
            sort.direction === 'asc');
      }
    });
  }

  compare(a: number | string, b: number | string, isAsc: boolean) {
    if (typeof a === 'string' && typeof b === 'string') {
      return (a.toLowerCase().localeCompare(b.toLowerCase()) ? -1 : 1) * (isAsc ? 1 : -1);
    }
    if (typeof a === 'number') {
      return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
    }
  }
}
