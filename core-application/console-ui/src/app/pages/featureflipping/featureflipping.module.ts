import {CommonModule, JsonPipe} from '@angular/common';
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
import {FeatureflippingComponent, FeatureflippingDialog} from './featureflipping.component';
import {FeatureflippingService} from './featureflipping.service';
import {FeatureflippingRoutingModule} from './featureflipping-routing.module';
import {BooleanInputComponent} from "./form-input/boolean/boolean-input-component";
import {JsonInputComponent} from "./form-input/json/json-input-component";
import {IntegerInputComponent} from "./form-input/integer/integer-input-component";
import {StringInputComponent} from "./form-input/string/string-input-component";
import {FloatInputComponent} from "./form-input/float/float-input-component";


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
    FeatureflippingRoutingModule,
    RouterModule,
    VisnetworkModule,
    ClipboardModule,
    MatExpansionModule,
  ],
  declarations: [FeatureflippingComponent, FeatureflippingDialog, BooleanInputComponent, JsonInputComponent, IntegerInputComponent, StringInputComponent, FloatInputComponent],
  exports: [FeatureflippingComponent, FeatureflippingDialog, BooleanInputComponent, JsonInputComponent, IntegerInputComponent, StringInputComponent, FloatInputComponent],
  providers: [FeatureflippingService, BreadCrumbService, JsonPipe]
})
export class FeatureflippingModule {
}
