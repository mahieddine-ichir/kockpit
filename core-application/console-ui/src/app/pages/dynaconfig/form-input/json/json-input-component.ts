import { Component, Input, OnInit } from '@angular/core';
import {
  PropertyValue
} from '../../dynaconfig.model';
import { JsonPipe } from '@angular/common';
import { FormControl, Validators } from '@angular/forms';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

@Component({
  selector: 'json-input-component',
  templateUrl: './json-input-component.html',
  styleUrls: ['./json-input-component.scss']
})
export class JsonInputComponent implements OnInit {

  jsonValidityControl: FormControl = new FormControl('', this.jsonValidator());
  propertyValue: PropertyValue;

  @Input() set property(property: PropertyValue) {
    this.propertyValue = property;
    if (property.value != null) {
      this.jsonValidityControl.setValue(this.jsonPipe.transform(this.parseJsonValue(property.value)));
    }
  }

  @Input() set isEditable(isEditable: boolean) {
    if (isEditable) {
      this.jsonValidityControl.enable()
    } else {
      this.jsonValidityControl.disable()
    }
  }

  constructor(private jsonPipe: JsonPipe) {
  }

  ngOnInit(): void {
  }

  updateProperty(updatedValue) {
    this.propertyValue.value = JSON.stringify(JSON.parse(updatedValue));
  }

  parseJsonValue(jsonValue: string) {
    try {
      return JSON.parse(jsonValue);
    } catch (e) {
      return null;
    }
  }

  jsonValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const error: ValidationErrors = { jsonInvalid: true };

      try {
        JSON.parse(control.value);
      } catch (e) {
        control.setErrors(error);
        return error;
      }

      control.setErrors(null);
      return null;
    };
  }
}