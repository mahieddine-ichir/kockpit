import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'wcc-http-result',
  templateUrl: './http-result.component.html',
  styleUrls: ['./http-result.component.scss']
})
export class HttpResultComponent implements OnInit {

  @Input()
  value: string;

  constructor() { }

  ngOnInit() {
  }

  isSuccess(value: string) {
    return value == 'SUCCESS' ||
        value === 'VALID';
  }

  isError(value: string) {
    return value === 'BADREQUEST' ||
        value === 'ERROR';
  }

  isWarning(value: string) {
    return value === 'WARNING';
  }

  severity(value: string) {
    if (this.isError(value)) {
      return 'ERROR';
    } else if (this.isSuccess(value)) {
      return 'SUCCESS';
    } else if (this.isWarning(value)) {
      return 'WARNING';
    } else {
      return 'none';
    }
  }
}
