import { Component, Input, OnInit } from '@angular/core';
import {
  PropertyValue
} from '../../dynaconfig.model';

@Component({
  selector: 'float-input-component',
  templateUrl: './float-input-component.html',
  styleUrls: ['./float-input-component.scss']
})
export class FloatInputComponent implements OnInit {

  @Input() property: PropertyValue;
  @Input() isEditable: boolean;

  ngOnInit(): void {
  }

  floatOnly(event): boolean {
    const charCode = (event.which) ? event.which : event.keyCode;
    console.log(charCode)
    if ((charCode >= 48 && charCode <= 57) || charCode == 46 || charCode == 44) {
      return true;
    }
    return false;
  }
}