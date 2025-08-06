import {Component, Input} from '@angular/core';
import {Item} from './item';
import {BreadCrumbService} from './breadcrumb-service';

@Component({
  selector: 'breadcrumb',
  templateUrl: 'breadcrumb.component.html',
  styleUrls: ['breadcrumb.component.scss']
})

export class BreadcrumbComponent {
  @Input() menu: Item[] = [];
  @Input() separator: string = '/';
  @Input() style: string;

  constructor(private _breadCrumbService: BreadCrumbService) {
    this._breadCrumbService.changeEmittedItem$.subscribe(
      item => {
        this.menu = this._breadCrumbService.applyItem(item);
      }
    );
  }


  ngOnInit() {


  }

  public getClasses() {
    return {
      'custom-1': this.style === 'custom1',
      'custom-2': this.style === 'custom2'
    };
  }

  get breadCrumbService(): BreadCrumbService {
    return this._breadCrumbService;
  }
}
