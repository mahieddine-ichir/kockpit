import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { A2CardComponent } from './card.component';

@NgModule({
  imports: [
    CommonModule,
  ],
  declarations: [A2CardComponent],
  exports: [A2CardComponent]
})
export class CardModule {
}
