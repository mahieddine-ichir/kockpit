import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {FeatureflippingComponent} from './featureflipping.component';

const routes: Routes = [
  {
    path: '',
    component: FeatureflippingComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FeatureflippingRoutingModule {
}
