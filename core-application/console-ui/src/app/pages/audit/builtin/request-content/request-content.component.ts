import {Component, Input, OnInit} from '@angular/core';
import {HttpExchange} from '../http-exchange/http.exchange';

@Component({
  selector: 'wcc-request-content',
  templateUrl: './request-content.component.html',
  styleUrls: ['./request-content.component.scss']
})
export class RequestContentComponent implements OnInit {

  @Input()
  httpExchange: HttpExchange;

  @Input()
  title: string;

  @Input()
  duration: any;

  constructor() {
  }

  ngOnInit() {
    this.httpExchange.panelOpenState = true;
  }
}
