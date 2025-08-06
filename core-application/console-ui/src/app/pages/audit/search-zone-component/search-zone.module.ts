import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {NgxJsonViewerModule} from 'ngx-json-viewer';
import {SearchZoneComponent} from "./search-zone.component";
import {StoredSearchService} from "../stored-search.service";
import {FuryCardModule} from "../../../core/common/card/card.module";
import {StoredSearchComponent} from "./search/stored-search.component";
import {MaterialModule} from "../../../core/common/material-components.module";
import {BuildCriteriaComponent} from "./search/builder/build-criteria-component";
import {SearchInputsComponent} from "./search/builder/search-inputs.component";
import {SelectedCriteriaComponent} from "./search/selected-criteria.component";
import {FlexModule} from "@angular/flex-layout";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatChipsModule} from "@angular/material/chips";
import {SearchZoneService} from "./search-zone.service";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {NgxMatDateFormats, NgxMatDatetimePickerModule, NGX_MAT_DATE_FORMATS} from '@angular-material-components/datetime-picker';
import {NgxMatMomentModule} from '@angular-material-components/moment-adapter';

const CUSTOM_DATE_FORMATS: NgxMatDateFormats = {
  parse: {
    dateInput: "DD-MM-yyyy HH:mm:ss"
  },
  display: {
    dateInput: "DD-MM-yyyy HH:mm:ss",
    monthYearLabel: 'YY',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'YY'
  }
};

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatSelectModule,
    RouterModule,
    NgxJsonViewerModule,
    MatChipsModule,
    FuryCardModule,
    MaterialModule,
    FlexModule,
    FontAwesomeModule,
    NgxMatDatetimePickerModule,
    NgxMatMomentModule,
  ],
  declarations: [SearchZoneComponent, StoredSearchComponent, BuildCriteriaComponent, SearchInputsComponent, SelectedCriteriaComponent],
  exports: [SearchZoneComponent, StoredSearchComponent, BuildCriteriaComponent, SearchInputsComponent, SelectedCriteriaComponent],
  providers: [StoredSearchService, SearchZoneService,
    { provide: NGX_MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }]
})
export class SearchZoneModule {
}
