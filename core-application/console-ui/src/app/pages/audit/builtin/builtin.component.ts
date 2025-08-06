import {Component, Input} from '@angular/core';
import {Builtin} from './builtin.model';

@Component({
  selector: 'wcc-builtin',
  templateUrl: './builtin.component.html',
  styleUrls: ['./builtin.component.scss']
})
export class BuiltinComponent {

  @Input()
  builtin: Builtin;

  asJson(builtin: Builtin): string {
    if (builtin) {
      return JSON.stringify(builtin);
    } else {
      return 'empty';
    }
  }
}
