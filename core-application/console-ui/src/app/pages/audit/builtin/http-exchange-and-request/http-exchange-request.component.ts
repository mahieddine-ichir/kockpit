import {Component, Input, OnInit} from '@angular/core';
import {HttpExchange} from '../http-exchange/http.exchange';
import {MatTabChangeEvent} from '@angular/material/tabs';

const xmlFormat = require('xml-formatter');

@Component({
  selector: 'wcp-http-exchange-request-content',
  templateUrl: './http-exchange-request.component.html',
  styleUrls: ['./http-exchange-request.component.scss']
})
export class HttpExchangeRequestComponent implements OnInit {

  @Input()
  httpExchange: HttpExchange;

  @Input()
  title: string;

  time: number;

  @Input()
  duration: any;

  isXMLRequest = false;
  isXMLResponse = false;

  isJSONRequest = false;
  isJSONResponse = false;

  isStringRequest = false;
  isStringResponse = false;

  selectedIndex = 0;
  indexTextLabel = 'Response';
  responseBody: any;
  requestBody: any;

  ngOnInit() {
    if (this.duration) {
      this.time = this.duration.value;
    } else {
      this.time = Math.abs(new Date(this.httpExchange.endTime).getTime() - new Date(this.httpExchange.startTime).getTime());
    }

    if (this.httpExchange.httpAuditedResponse.body) {
      this.responseBody = this.checkType(this.httpExchange.httpAuditedResponse.body, false);
    } else {
      this.responseBody = this.checkType(this.httpExchange.httpAuditedResponse.payload, false);
    }

    if (this.httpExchange.httpAuditedRequest.body) {
      this.requestBody = this.checkType(this.httpExchange.httpAuditedRequest.body, true);
    }
  }

  openState() {
    this.httpExchange.panelOpenState = true;
    this.selectedIndex = 0;
    this.indexTextLabel = 'Response';
  }

  closeState() {
    this.httpExchange.panelOpenState = false;
  }

  getResponseNumberOfHeaders() {
    const headers = this.httpExchange.httpAuditedResponse.headers;
    if (headers) {
      return Object.keys(headers).length;
    } else {
      return 0;
    }
  }

  getRequestNumberOfHeaders() {
    const headers = this.httpExchange.httpAuditedRequest.headers;
    const params = this.httpExchange.httpAuditedRequest.params;
    let nbParams;
    let nbHeaders;

    if (headers) {
      nbHeaders = Object.keys(headers).length;
    } else {
      nbHeaders = 0;
    }

    if (params) {
      nbParams = Object.keys(params).length;
    } else {
      nbParams = 0;
    }
    return nbHeaders + nbParams;
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

  checkType(httpExchange, isRequest) {
    if (this.isValidJson(httpExchange)) {
      if (isRequest) {
        this.isJSONRequest = true;
      } else {
        this.isJSONResponse = true;
      }
      return JSON.parse(httpExchange);
    } else if (this.isValidXML(httpExchange)) {
      if (isRequest) {
        this.isXMLRequest = true;
      } else {
        this.isXMLResponse = true;
      }
      return xmlFormat(httpExchange, {collapseContent: true});
    } else {
      if (isRequest) {
        this.isStringRequest = true;
      } else {
        this.isStringResponse = true;
      }
      return httpExchange;
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

  isValidXML(str) {
    const domParser = new DOMParser();
    if (domParser.parseFromString(str, 'application/xml').querySelector('parsererror')) {
      return false;
    } else {
      return true;
    }
  }

  onTabChanged(event: MatTabChangeEvent) {
    this.indexTextLabel = event.tab.textLabel;
    this.selectedIndex = event.index;
  }

  copyMessage(value) {
    let val;
    if (typeof value === 'string') {
      val = value;
    } else {
      val = JSON.stringify(value);
    }
    const selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = val;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
  }
}
