import {Component, Input, OnInit, ViewEncapsulation} from "@angular/core";
import {KafkaMessage} from "../kafka-message/kafka-message";

@Component({
  selector: 'wcp-kafka-tab',
  templateUrl: './kafka-tab.component.html',
  styleUrls: ['./kafka-tab.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class KafkaTabComponent {

  @Input()
  kafkaMessages: KafkaMessage[];

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
