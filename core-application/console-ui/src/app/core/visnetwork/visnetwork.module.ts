import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {VisnetworkDirective} from "./visnetwork.directive";

@NgModule({
  declarations: [VisnetworkDirective],
  exports: [VisnetworkDirective],
  imports: [
    CommonModule
  ]
})
export class VisnetworkModule { }
