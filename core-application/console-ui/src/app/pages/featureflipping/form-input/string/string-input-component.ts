import {Component, Input, OnInit} from '@angular/core';
import {KeyValue} from '../../featureflipping.model';

@Component({
    selector: 'string-input-component',
    templateUrl: './string-input-component.html',
    styleUrls: ['./string-input-component.scss']
})
export class StringInputComponent implements OnInit {

    @Input() property: KeyValue;
    @Input() isEditable: boolean;

    ngOnInit(): void {
    }
}
