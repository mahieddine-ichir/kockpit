import {
  Component,
  OnInit,
  Input,
  ViewEncapsulation,
  AfterViewChecked} from '@angular/core';
import {KafkaMessage} from './kafka-message';

const xmlFormat = require('xml-formatter');

@Component({
  selector: 'wcp-kafka-message',
  templateUrl: './kafka-message.component.html',
  styleUrls: ['./kafka-message.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class KafkaMessageComponent implements OnInit, AfterViewChecked {

  @Input()
  kafkaMessages: KafkaMessage[];

  consumedMessages: KafkaMessage[] = [];
  producedMessages: KafkaMessage[] = [];
  forwardedMessages: KafkaMessage[] = [];

  selectedIndex: number;

  producedActive = false;
  consumedActive = false;
  forwardedActive = false;

  ngOnInit() {
    this.kafkaMessages.forEach(kafkaMsg => {
      kafkaMsg.payload = this.checkType(kafkaMsg);
    });
    this.filterMessagesBySource(this.kafkaMessages);
    this.activeTab();
  }

  ngAfterViewChecked() {
    window.dispatchEvent(new Event('resize'));
  }

  filterMessagesBySource(kafkaMessages) {
    kafkaMessages.forEach(kafkaMsg => {
      if (kafkaMsg.source === 'PRODUCE') {
        this.producedMessages.push(kafkaMsg);
      } else if (kafkaMsg.source === 'CONSUME') {
        this.consumedMessages.push(kafkaMsg);
      } else if (kafkaMsg.source === 'FORWARD') {
        this.forwardedMessages.push(kafkaMsg);
      }
    });
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

  checkType(kafkaMsg) {
    if (this.isValidJson(kafkaMsg.payload)) {
      kafkaMsg.isJson = true;
      return JSON.parse(kafkaMsg.payload);
    } else if (this.isValidXML(kafkaMsg.payload)) {
      kafkaMsg.isXml = true;
      return xmlFormat(kafkaMsg.payload, {collapseContent: true});
    } else {
      kafkaMsg.isString = true;
      return kafkaMsg.payload;
    }
  }

  activeTab() {
    if (this.consumedMessages.length !== 0) {
      this.consumedActive = true;
      this.selectedIndex = 2;
    }
    if (this.forwardedMessages.length !== 0) {
      this.forwardedActive = true;
      this.selectedIndex = 1;
    }
    if (this.producedMessages.length !== 0) {
      this.producedActive = true;
      this.selectedIndex = 0;
    }
  }
}
