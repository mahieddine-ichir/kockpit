import { Component, Input, OnInit } from '@angular/core';
import {
    PropertyValue
} from '../../dynaconfig.model';

@Component({
    selector: 'string-input-component',
    templateUrl: './string-input-component.html',
    styleUrls: ['./string-input-component.scss']
})
export class StringInputComponent implements OnInit {

    @Input() property: PropertyValue;
    @Input() isEditable: boolean;

    ngOnInit(): void {
    }
}