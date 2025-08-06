import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'wcc-headers',
  templateUrl: './headers.component.html',
  styleUrls: ['./headers.component.scss']
})
export class HeadersComponent implements OnInit {

  @Input()
  headers: Map<string, string>;

  @Input()
  title: string;

  constructor() { }

  ngOnInit() {
  }

}
