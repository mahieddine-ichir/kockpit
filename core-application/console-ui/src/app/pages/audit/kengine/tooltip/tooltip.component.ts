import {Component, Input, OnInit} from '@angular/core';
import {animate, style, transition, trigger} from '@angular/animations';

@Component({
  selector: 'wcc-tooltip',
  templateUrl: './tooltip.component.html',
  styleUrls: ['./tooltip.component.scss'],
  animations: [
    trigger('tooltip', [
      transition(':enter', [
        style({ opacity: 0}),
        animate(300, style({ opacity: 1 })),
      ]),
      transition(':leave', [
        animate(300, style({ opacity: 0 })),
      ]),
    ]),
  ],
})
export class TooltipComponent implements OnInit {

  @Input() image = '';
  @Input() text = '';

  constructor() { }

  ngOnInit(): void {
  }

}
