import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {BreadcrumbsModule} from '../../core/breadcrumbs/breadcrumbs.module';
import {BreadcrumbModule} from '../../kiss-components/breadcrumb/breadcrumb.module';
import {ListModule} from '../../core/common/list/list.module';
import {MaterialModule} from '../../core/common/material-components.module';
import {PageModule} from '../../core/common/page/page.module';
import {FuryCardModule} from '../../core/common/card/card.module';
import {RouterModule} from '@angular/router';
import {BreadCrumbService} from '../../kiss-components/breadcrumb/breadcrumb-service';
import {NgxJsonViewerModule} from 'ngx-json-viewer';
import {VisnetworkModule} from '../../core/visnetwork/visnetwork.module';
import {MatChipsModule} from '@angular/material/chips';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {ClipboardModule} from '@angular/cdk/clipboard';
import {MatExpansionModule} from '@angular/material/expansion';
import {CacheComponent} from './cache.component';
import {CacheService} from './cache.service';
import {CacheRoutingModule} from './cache-routing.module';


@NgModule({
  imports: [
    BreadcrumbModule,
    BreadcrumbsModule,
    CommonModule,
    FormsModule,
    FuryCardModule,
    ListModule,
    MatChipsModule,
    MaterialModule,
    MatFormFieldModule,
    MatSelectModule,
    NgxJsonViewerModule,
    PageModule,
    ReactiveFormsModule,
    CacheRoutingModule,
    RouterModule,
    VisnetworkModule,
    ClipboardModule,
    MatExpansionModule
  ],
  declarations: [CacheComponent],
  exports: [CacheComponent],
  providers: [CacheService, BreadCrumbService]
})
export class CacheModule {
}
