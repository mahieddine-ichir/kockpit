import {Component, Input} from '@angular/core';
import {KinesisMessage} from "./kinesis-message";

@Component({
  selector: 'wcc-kinesis-message',
  templateUrl: './kinesis-message.component.html',
  styleUrls: ['./kinesis-message.component.scss']
})
export class KinesisMessageComponent {

  @Input()
  kinesisMessages: KinesisMessage[];
}
