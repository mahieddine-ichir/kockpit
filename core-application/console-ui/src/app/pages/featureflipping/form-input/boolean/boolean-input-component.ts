import {Component, Input, OnInit} from '@angular/core';
import {KeyValue} from '../../featureflipping.model';

@Component({
  selector: 'boolean-input-component',
  templateUrl: './boolean-input-component.html',
  styleUrls: ['./boolean-input-component.scss']
})
export class BooleanInputComponent implements OnInit {

  @Input() property: KeyValue;
  @Input() isEditable: boolean;

  ngOnInit(): void {
    console.log(this.property)
  }

  isChecked() {
    return this.property.value === 'true' ? true : false
  }


  updateValue(event) {
    if (event.checked) {
      this.property.value = "true"
    } else {
      this.property.value = "false"
    }
  }
}
