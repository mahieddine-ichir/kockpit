import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MaterialModule } from '../../../core/common/material-components.module';
import { LogoutRoutingModule } from './logout-routing.module';
import { LogoutComponent } from './logout.component';

@NgModule({
  imports: [
    CommonModule,
    LogoutRoutingModule,
    MaterialModule,
    ReactiveFormsModule
  ],
  declarations: [LogoutComponent]
})
export class LogoutModule {
}
