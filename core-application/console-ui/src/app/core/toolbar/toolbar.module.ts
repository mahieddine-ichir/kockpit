import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {FuryCardModule} from '../common/card/card.module';
import {ClickOutsideModule} from '../common/click-outside/click-outside.module';
import {MaterialModule} from '../common/material-components.module';
import {ScrollbarModule} from '../common/scrollbar/scrollbar.module';
import {ToolbarFullscreenToggleComponent} from './toolbar-fullscreen-toggle/toolbar-fullscreen-toggle.component';
import {ToolbarNotificationsComponent} from './toolbar-notifications/toolbar-notifications.component';
import {ToolbarQuickpanelToggleComponent} from './toolbar-quickpanel-toggle/toolbar-quickpanel-toggle.component';
import {ToolbarSearchBarComponent} from './toolbar-search-bar/toolbar-search-bar.component';
import {ToolbarSearchComponent} from './toolbar-search/toolbar-search.component';
import {ToolbarEnvironmentComponent} from './toolbar-environment/toolbar-environment.component';
import {ToolbarSidenavMobileToggleComponent} from './toolbar-sidenav-mobile-toggle/toolbar-sidenav-mobile-toggle.component';
import {ToolbarUserButtonComponent} from './toolbar-user-button/toolbar-user-button.component';
import {ToolbarComponent} from './toolbar.component';
import {MatChipsModule} from '@angular/material/chips';
import {MatBadgeModule} from '@angular/material/badge';
import {ToolbarManifestComponent} from './toolbar-manifest/toolbar-manifest.component';

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        RouterModule,
        ScrollbarModule,
        FormsModule,
        ReactiveFormsModule,
        ClickOutsideModule,
        FuryCardModule,
        MatChipsModule,
        MatBadgeModule,
    ],
  declarations: [
    ToolbarComponent,
    ToolbarUserButtonComponent,
    ToolbarNotificationsComponent,
    ToolbarSearchComponent,
    ToolbarSearchBarComponent,
    ToolbarQuickpanelToggleComponent,
    ToolbarFullscreenToggleComponent,
    ToolbarFullscreenToggleComponent,
    ToolbarSidenavMobileToggleComponent,
    ToolbarEnvironmentComponent,
    ToolbarManifestComponent,
    // KissAutocompleteComponent
  ],
  exports: [ToolbarComponent],
  providers: []
})
export class ToolbarModule {
}
