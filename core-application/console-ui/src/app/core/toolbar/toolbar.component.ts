import {Component, Input, OnInit} from '@angular/core';
import {HeaderService} from './header.service';
import {MatSidenav} from "@angular/material/sidenav";

@Component({
  selector: 'fury-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss']
})
export class ToolbarComponent implements OnInit {
  projects;
  project;
  versions;
  version;
  @Input('quickpanel') quickpanel: MatSidenav;

  constructor(private _headerService: HeaderService) {
    this.projects = [{code: 'DISPO PROCESS', label: 'DISPO PROCESS'}, {code: 'DISPO SYSTEM', label: 'DISPO SYSTEM'}];
    this.project = {code: 'DISPO PROCESS', label: 'DISPO PROCESS'};
    // this.versions = [{code: '1.2.4 - 1.2.3', label: '1.2.4 - 1.2.3'}, {code: '1.3.4 - 1.3.5', label: '1.3.4 - 1.3.5'}];
    // this.version = {code: '1.2.4 - 1.2.3', label: '1.2.4 - 1.2.3'};
    this.versions = [{code: 'V1', label: 'V1'}, {code: 'V2', label: 'V2'}, {code: 'V3', label: 'V3'}, {code: 'V4', label: 'V4'}, {code: 'V5', label: 'V5'}];
    this.version = {code: 'V1', label: 'V1'};
  }

  ngOnInit() {
  }

  get headerService(): HeaderService {
    return this._headerService;
  }
}
