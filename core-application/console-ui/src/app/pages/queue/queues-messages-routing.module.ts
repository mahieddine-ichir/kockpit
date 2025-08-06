import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MessageComponent } from './list-messages/messages.component';
import {DetailComponent} from "./detail-message/detail.component";

const routes: Routes = [
  {
    path: '',
    component: MessageComponent
  },{
    path: ':id',
    component: DetailComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class QueuesMessagesRoutingModule {
}
