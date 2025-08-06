import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {DynaconfigComponent} from './dynaconfig.component';

const routes: Routes = [
  {
    path: '',
    component: DynaconfigComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DynaconfigRoutingModule {
}
