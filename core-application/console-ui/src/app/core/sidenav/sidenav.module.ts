import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MaterialModule } from '../common/material-components.module';
import { ScrollbarModule } from '../common/scrollbar/scrollbar.module';
import { SidenavItemComponent } from './sidenav-item/sidenav-item.component';
import { SidenavComponent } from './sidenav.component';
import { SidenavService } from './sidenav.service';
import {AuthorizationModule} from '../common/admin/authorization.module';
import { MatExpansionModule } from '@angular/material/expansion';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';

@NgModule({
    imports: [
        CommonModule,
        RouterModule,
        MaterialModule,
        ScrollbarModule,
        AuthorizationModule,
        MatExpansionModule,
        FormsModule,
        ReactiveFormsModule,
        CommonModule,
        MatButtonModule,
        MatTooltipModule,
        RouterModule
    ],
  declarations: [SidenavComponent, SidenavItemComponent],
  exports: [SidenavComponent],
  providers: [SidenavService]
})
export class SidenavModule {
}
