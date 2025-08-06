import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {MaterialModule} from '../../../core/common/material-components.module';
import {LoadingRoutingModule} from './loading-routing.module';
import {LoadingComponent} from './loading.component';

@NgModule({
  imports: [
    CommonModule,
    LoadingRoutingModule,
    MaterialModule,
    ReactiveFormsModule,
  ],
  entryComponents: [],
  declarations: [LoadingComponent]
})
export class LoadingModule {
}
