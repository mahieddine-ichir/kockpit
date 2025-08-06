import {Component, ComponentFactoryResolver, OnDestroy, OnInit} from '@angular/core';

@Component({
  selector: 'fury-toolbar-search',
  templateUrl: './toolbar-search.component.html',
  styleUrls: ['./toolbar-search.component.scss']
})
export class ToolbarSearchComponent implements OnInit {
  values:["DISPO PROCESS","DISPO SYSTEM"];
  value:"DISPO PROCESS";
  isOpen: boolean;



  constructor() {
  }

  ngOnInit() {
  }

  open() {
    this.isOpen = true;
  }

  close() {
    this.isOpen = false;
  }

}
