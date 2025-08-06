import { NgModule } from '@angular/core';
import { LocaleNumberDirective } from './localeNumber.directive';

@NgModule({
  declarations: [LocaleNumberDirective],
  exports: [LocaleNumberDirective]
})
export class LocaleNumberModule {
}
