import {UsernotificationComponent} from './usernotification.component';
import {UsernotificationService} from './usernotification.service';
import {NgModule} from '@angular/core';
import {PageModule} from '../../core/common/page/page.module';
import {BreadcrumbModule} from '../../kiss-components/breadcrumb/breadcrumb.module';
import {BreadcrumbsModule} from '../../core/breadcrumbs/breadcrumbs.module';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {FuryCardModule} from '../../core/common/card/card.module';
import {ListModule} from '../../core/common/list/list.module';
import {MatChipsModule} from '@angular/material/chips';
import {MaterialModule} from '../../core/common/material-components.module';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {NgxJsonViewerModule} from 'ngx-json-viewer';
import {DynaconfigRoutingModule} from '../dynaconfig/dynaconfig-routing.module';
import {RouterModule} from '@angular/router';
import {VisnetworkModule} from '../../core/visnetwork/visnetwork.module';
import {ClipboardModule} from '@angular/cdk/clipboard';
import {MatExpansionModule} from '@angular/material/expansion';
import {BreadCrumbService} from '../../kiss-components/breadcrumb/breadcrumb-service';
import {MatTableModule} from '@angular/material/table';

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
        MatTableModule,
        NgxJsonViewerModule,
        PageModule,
        ReactiveFormsModule,
        DynaconfigRoutingModule,
        RouterModule,
        VisnetworkModule,
        ClipboardModule,
        MatExpansionModule,
    ],
  declarations: [UsernotificationComponent],
  exports: [UsernotificationComponent],
  providers: [UsernotificationService, BreadCrumbService]
})

export class UsernotificationModule {
}
