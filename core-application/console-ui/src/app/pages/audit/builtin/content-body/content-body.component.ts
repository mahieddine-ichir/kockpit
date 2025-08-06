import {AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';

@Component({
  selector: 'wcc-content-body',
  templateUrl: './content-body.component.html',
  styleUrls: ['./content-body.component.scss']
})
export class ContentBodyComponent implements OnInit, AfterViewInit {

  @Input()
  content: string;

  @Input()
  contentType: string;

  @Input()
  title: string;

  @ViewChild("content", {static: true})
  element: ElementRef;

  constructor() { }

  ngOnInit() {
  }

  ngAfterViewInit() {
    if (!this.content) {
      this.element.nativeElement.innerHTML = "<i>empty</i>";
      return;
    }
    if ("application/json" === this.contentType) {
      this.element.nativeElement.innerText = this.content;
    } else {
      this.element.nativeElement.innerText = this.content;
    }
  }
}
