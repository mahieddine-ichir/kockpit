import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { LoadingIndicatorComponent } from './loading-indicator.component';
import {MatProgressBarModule} from "@angular/material/progress-bar";

@NgModule({
  imports: [
    CommonModule,
    MatProgressBarModule
  ],
  declarations: [LoadingIndicatorComponent],
  exports: [LoadingIndicatorComponent],
})
export class LoadingIndicatorModule {
}
