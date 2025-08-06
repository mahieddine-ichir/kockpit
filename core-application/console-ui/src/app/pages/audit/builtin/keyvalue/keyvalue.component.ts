import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'wcc-keyvalue',
  templateUrl: './keyvalue.component.html',
  styleUrls: ['./keyvalue.component.scss']
})
export class KeyvalueComponent {

  @Input()
  key: string;

  @Input()
  value: object;

  convert(): string {
    if (this.value instanceof String) {
      return '' + this.value;
    }
    return JSON.stringify(this.value);
  }
}
