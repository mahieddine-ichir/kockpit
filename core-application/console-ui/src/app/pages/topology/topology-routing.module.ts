import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TopologyComponent} from './topology.component';

const routes: Routes = [
  {
    path: '',
    component: TopologyComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TopologyRoutingModule {
}
