import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { WccAuthorizationDirective } from './authorization.directive';

@NgModule({
  imports: [
    CommonModule
  ],
  declarations: [WccAuthorizationDirective],
  exports: [WccAuthorizationDirective]
})
export class AuthorizationModule {
}
