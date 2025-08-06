import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {BreadcrumbsModule} from '../../core/breadcrumbs/breadcrumbs.module';
import {BreadcrumbModule} from '../../kiss-components/breadcrumb/breadcrumb.module';
import {ListModule} from '../../core/common/list/list.module';
import {MaterialModule} from '../../core/common/material-components.module';
import {PageModule} from '../../core/common/page/page.module';
import {RequestRoutingModule} from './request-routing.module';
import {RequestComponent} from './request.component';
import {FuryCardModule} from '../../core/common/card/card.module';
import {RouterModule} from '@angular/router';
import {RequestService} from './request.service';
import {DetailComponent} from './detail/detail.component';
import {BreadCrumbService} from '../../kiss-components/breadcrumb/breadcrumb-service';
import {NgxJsonViewerModule} from 'ngx-json-viewer';
import {StoredSearchService} from './stored-search.service';
import {VisnetworkModule} from '../../core/visnetwork/visnetwork.module';
import {SearchZoneModule} from './search-zone-component/search-zone.module';
import {MatChipsModule} from '@angular/material/chips';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {BuiltinComponent} from './builtin/builtin.component';
import {RequestContentComponent} from './builtin/request-content/request-content.component';
import {HeadersComponent} from './builtin/headers/headers.component';
import {KeyvalueComponent} from './builtin/keyvalue/keyvalue.component';
import {PropertiesComponent} from './builtin/properties/properties.component';
import {KeyvaluesComponent} from './builtin/keyvalues/keyvalues.component';
import {HttpExchangeComponent} from './builtin/http-exchange/http-exchange.component';
import {ContentBodyComponent} from './builtin/content-body/content-body.component';
import {HttpResultComponent} from './ui/http-result/http-result.component';
import {TimeComponent} from './ui/time/time.component';
import {ClipboardModule} from '@angular/cdk/clipboard';
import {MatExpansionModule} from '@angular/material/expansion';
import {SqsMessageComponent} from './builtin/sqs-message/sqs-message.component';
import {KinesisMessageComponent} from './builtin/kinesis-message/kinesis-message.component';
import {ResizeColumnDirective} from './resize-column.directive';
import {KengineComponent} from './kengine/kengine.component';
import {DialogStackTrace, RendererComponent} from './kengine/renderer/renderer.component';
import {TooltipComponent} from './kengine/tooltip/tooltip.component';
import {TooltipDirective} from './kengine/tooltip/tooltip.directive';
import {HttpExchangeRequestComponent} from './builtin/http-exchange-and-request/http-exchange-request.component';
import {KafkaMessageComponent} from './builtin/kafka-message/kafka-message.component';
import {KafkaTabComponent} from './builtin/kafka-tab/kafka-tab.component';
import {MatTreeModule} from "@angular/material/tree";
import {FlowHierarchyComponent} from "./kengine/flow-hierarchy-component/flow-hierarchy.component";

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
    RequestRoutingModule,
    RouterModule,
    SearchZoneModule,
    VisnetworkModule,
    ClipboardModule,
    MatExpansionModule,
    MatTreeModule
  ],
  declarations: [
    RequestComponent,
    DetailComponent,
    BuiltinComponent,
    RequestContentComponent,
    HeadersComponent,
    KeyvalueComponent,
    PropertiesComponent,
    KeyvaluesComponent,
    HttpExchangeComponent,
    ContentBodyComponent,
    HttpResultComponent,
    TimeComponent,
    SqsMessageComponent,
    KinesisMessageComponent,
    KafkaMessageComponent,
    KafkaTabComponent,
    ResizeColumnDirective,
    KengineComponent,
    RendererComponent,
    TooltipComponent,
    TooltipDirective,
    HttpExchangeRequestComponent,
    DialogStackTrace,
    FlowHierarchyComponent
  ],
  exports: [RequestComponent, DetailComponent, ResizeColumnDirective],
  providers: [RequestService, BreadCrumbService, StoredSearchService]
})
export class RequestModule {
}
