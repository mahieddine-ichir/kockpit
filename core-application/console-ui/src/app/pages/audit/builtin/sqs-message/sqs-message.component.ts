import {Component, Input} from '@angular/core';
import {SqsMessage} from './sqs-message';

@Component({
  selector: 'wcc-sqs-message',
  templateUrl: './sqs-message.component.html',
  styleUrls: ['./sqs-message.component.scss']
})
export class SqsMessageComponent {

  @Input()
  sqsMessage: SqsMessage[];
}
