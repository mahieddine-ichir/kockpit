import {Component, Input} from '@angular/core';

@Component({
  selector: 'wcc-keyvalues',
  templateUrl: './keyvalues.component.html',
  styleUrls: ['./keyvalues.component.scss']
})
export class KeyvaluesComponent {

  @Input()
  keyValues: any[];

  @Input() title: string;

  flatMapKeyValues() {
    if (!this.keyValues) {
      return [];
    }
    return this.keyValues.flatMap(value => {
      if (value.indexedKeyValues) {
        return value.indexedKeyValues;
      }
      return value;
    });
  }
}
