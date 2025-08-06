import {Component, Input, OnChanges} from '@angular/core';
import {HttpExchange} from './http.exchange';
import {UtilsService} from '../utils.service';

@Component({
  selector: 'wcc-http-exchange',
  templateUrl: './http-exchange.component.html',
  styleUrls: ['./http-exchange.component.scss']
})
export class HttpExchangeComponent implements OnChanges {

  @Input()
  httpExchanges: HttpExchange[];

  ngOnChanges() {
    if (this.httpExchanges) {
      this.formatHeaders();
    }
  }

  formatHeaders() {
    for (const http of this.httpExchanges) {
      if (!this.isValidJson(http.httpAuditedRequest.headers)) {
        http.httpAuditedRequest.headers = this.asMap(http.httpAuditedRequest.headers);
      }

      if (!this.isValidJson(http.httpAuditedResponse.headers)) {
        http.httpAuditedResponse.headers = this.asMap(http.httpAuditedResponse.headers);
      }
      http.panelOpenState = false;
    }
  }

  asMap(headers: string): Map<string, string[]> {
    if (typeof headers === 'string') {
      return UtilsService.asMap(headers);
    } else {
      return headers;
    }
  }

  isValidJson(str) {
    try {
      JSON.parse(str);
    } catch (e) {
      return false;
    }
    return true;
  }
}
