
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ListModule } from '../../core/common/list/list.module';
import { MaterialModule } from '../../core/common/material-components.module';
import { PageModule } from '../../core/common/page/page.module';
import { RouterModule } from '@angular/router';
import { NgxJsonViewerModule } from 'ngx-json-viewer';
import { VisnetworkModule } from "../../core/visnetwork/visnetwork.module";
import { MatChipsModule } from "@angular/material/chips";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { MatExpansionModule } from "@angular/material/expansion";
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatInputModule } from '@angular/material/input';
import { MatDialogModule } from '@angular/material/dialog';
import { MessageComponent } from "./list-messages/messages.component";
import { QueuesMessagesRoutingModule } from './queues-messages-routing.module';
import { DetailComponent } from './detail-message/detail.component';
import { ConfirmationDialog } from '../../core/confirm-dialog/confirmation-dialog.component';
import {BreadcrumbModule} from "../../kiss-components/breadcrumb/breadcrumb.module";
import {CdkAccordionModule} from "@angular/cdk/accordion";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ListModule,
        MatChipsModule,
        MaterialModule,
        MatButtonModule,
        MatButtonToggleModule,
        MatCardModule,
        MatCheckboxModule,
        MatInputModule,
        MatFormFieldModule,
        MatDialogModule,
        MatSelectModule,
        NgxJsonViewerModule,
        PageModule,
        ReactiveFormsModule,
        RouterModule,
        VisnetworkModule,
        MatExpansionModule,
        QueuesMessagesRoutingModule,
        BreadcrumbModule,
        CdkAccordionModule
    ],
  declarations: [MessageComponent, DetailComponent, ConfirmationDialog],
  providers: [MessageComponent, DetailComponent , ConfirmationDialog]
})
export class QueuesMessagesModule {
}
