import {NgModule, Optional, SkipSelf} from '@angular/core';
import {LayoutModule} from './layout/layout.module';
import {MatIconRegistry} from "@angular/material/icon";

@NgModule({
  imports: [
    // Displays Loading Bar when a Route Request or HTTP Request is pending
    // PendingInterceptorModule,

    // Layout Module (Sidenav, Toolbar, Quickpanel, Content)
    LayoutModule
  ],
  providers: [
    MatIconRegistry
  ],
  declarations: []
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error(
        'CoreModule is already loaded. Import it in the AppModule only.');
    }
  }
}
