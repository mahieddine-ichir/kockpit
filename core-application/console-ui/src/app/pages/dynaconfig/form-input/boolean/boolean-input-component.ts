import { Component, Input, OnInit } from '@angular/core';
import {
  PropertyValue
} from '../../dynaconfig.model';

@Component({
  selector: 'boolean-input-component',
  templateUrl: './boolean-input-component.html',
  styleUrls: ['./boolean-input-component.scss']
})
export class BooleanInputComponent implements OnInit {

  @Input() property: PropertyValue;
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