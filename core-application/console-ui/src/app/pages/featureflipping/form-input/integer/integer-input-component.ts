import {Component, Input, OnInit} from '@angular/core';
import {KeyValue} from '../../featureflipping.model';

@Component({
  selector: 'integer-input-component',
  templateUrl: './integer-input-component.html',
  styleUrls: ['./integer-input-component.scss']
})
export class IntegerInputComponent implements OnInit {

  @Input() property: KeyValue;
  @Input() isEditable: boolean;

  ngOnInit(): void {
  }

  numberOnly(event): boolean {
    const charCode = (event.which) ? event.which : event.keyCode;
    console.log(charCode)
    if (charCode >= 48 && charCode <= 57) {
      return true;
    }
    return false;
  }
}
