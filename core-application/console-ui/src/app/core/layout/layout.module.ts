import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { BackdropModule } from '../common/backdrop/backdrop.module';
import { MaterialModule } from '../common/material-components.module';
import { MediaQueryService } from '../common/mediareplay/media-replay.service';
import { FooterModule } from '../footer/footer.module';
import { QuickpanelModule } from '../quickpanel/quickpanel.module';
import { SidenavModule } from '../sidenav/sidenav.module';
import { ToolbarModule } from '../toolbar/toolbar.module';
import { LayoutComponent } from './layout.component';
import { BreadcrumbModule } from "../../kiss-components/breadcrumb/breadcrumb.module";
import {NgxResizableModule} from '@3dgenomes/ngx-resizable';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    MaterialModule,
    // Core
    ToolbarModule,
    QuickpanelModule,
    SidenavModule,
    FooterModule,
    BackdropModule,
    BreadcrumbModule,
    NgxResizableModule
  ],
  declarations: [LayoutComponent],
  providers: [MediaQueryService]
})
export class LayoutModule {
}
