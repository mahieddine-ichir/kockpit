import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'wcc-properties',
  templateUrl: './properties.component.html',
  styleUrls: ['./properties.component.scss']
})
export class PropertiesComponent implements OnInit {

  @Input()
  properties: Map<any, any>;

  @Input()
  filtered: string[];

  @Input() title: string;

  constructor() { }

  ngOnInit() {
  }

  getDisplayedKeys(): string[] {
    return Object.keys(this.properties)
        .filter(value => this.filtered.indexOf(value) <= -1)
  }
}
