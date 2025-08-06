import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {BreadcrumbComponent} from './breadcrumb.component';
import {MaterialModule} from '../../core/common/material-components.module';
import {RouterModule} from '@angular/router';

@NgModule({
  imports: [
    CommonModule,
    MaterialModule,
    RouterModule
  ],
  declarations: [BreadcrumbComponent],
  exports: [BreadcrumbComponent]
})
export class BreadcrumbModule {
}
