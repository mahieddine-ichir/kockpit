import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'wcc-time',
  templateUrl: './time.component.html',
  styleUrls: ['./time.component.scss']
})
export class TimeComponent implements OnInit {

  @Input()
  private start: Date;

  @Input()
  private end: Date;

  time: number;

  constructor() { }

  ngOnInit() {
    this.time = new Date(this.end).getTime() - new Date(this.start).getTime();
  }

}
