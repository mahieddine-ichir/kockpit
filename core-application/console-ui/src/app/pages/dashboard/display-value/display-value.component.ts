import {Component, Input, OnInit} from "@angular/core";
import {ListColumn} from "../../../core/common/list/list-column.model";
import {DashboardSearchRequest, DashboardTimelineProperty} from "../dashboard.model";
import * as _ from "lodash";
import {formatDate} from "@angular/common";

@Component({
  selector: 'wcp-display-value',
  templateUrl: './display-value.component.html',
  styleUrls: ['./display-value.component.scss']
})
export class DisplayValueComponent implements OnInit {

  @Input()
  column: ListColumn | DashboardTimelineProperty;
  @Input()
  row: DashboardSearchRequest;

  constructor() {
  }

  ngOnInit(): void {
  }

  fetchValue(row: DashboardSearchRequest, column: ListColumn | DashboardTimelineProperty, ignoreDisplayFn: boolean = false): string | null {
    const properties = (column instanceof DashboardTimelineProperty)
      ? column.properties
      : [column.property];

    if (!properties || properties.length === 0) {
      return null;
    }

    let val = properties
      .map(property => this.getIndexedKeyValue(row, property))
      .find(value => !_.isEmpty(value));  // Retourne la première valeur non vide

    if (_.isEmpty(val)) {
      return null;
    }

    if (ignoreDisplayFn) {
      return val;
    }
    switch (column.displayFn) {
      case 'substring':
        return val.substring(0, 10).concat('*');
      case 'date':
        return formatDate(val, 'dd-MM-yyyy HH:mm:ss', 'fr');
      default:
        return val;
    }
  }

  private getIndexedKeyValue(row: DashboardSearchRequest, key: string): string {
    const rowElement: string = row[key];
    return _.isEmpty(rowElement) ? this.fetchFromIndexedKeyValue(row, key) : rowElement;
  }

  private fetchFromIndexedKeyValue(row: DashboardSearchRequest, propertyKey: string): string {
    const indexedKeyValues = row.indexedKeyValues;
    return _.chain(indexedKeyValues)
      .find(indexKeyValue => indexKeyValue && indexKeyValue.key === propertyKey)
      .thru(field => (field ? field.value : null))
      .value();
  }

  httpStatusColor(rowElement: string) {
    const status = _.toNumber(rowElement);
    if (status < 400) {
      return 'green';
    }
    if (status < 500) {
      return 'orange';
    }
    return 'red';
  }

}
