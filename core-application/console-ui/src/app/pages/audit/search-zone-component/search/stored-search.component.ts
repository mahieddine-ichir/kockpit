import {Component, EventEmitter, Input, Output} from '@angular/core';
import {StoredSearch} from "./types/StoredSearch";
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {MatChipInputEvent} from '@angular/material/chips';
import {StoredSearchService} from "../../stored-search.service";
import {SelectedCriterion} from "./types/Criterion";

@Component({
  selector: 'stored-request-component',
  template: `
      
  `,
  styles: [`
    mat-form-field, mat-chip-list, .mat-form-field-infix, .mat-form-field-flex, .mat-form-field-wrapper {
      width: 100%;
    }
    span {
      color: #3c96ff;
    }

  `]
})
export class StoredSearchComponent {

  visible = true;
  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];

  storedSearchs: StoredSearch[];

  @Input() selectedCriteria:SelectedCriterion[];

  constructor(private service: StoredSearchService) {
  }

  ngOnInit() {
    this.service.storedSearchs$.subscribe(value => this.storedSearchs=value);
    this.service.loadStoredSearchs();
  }

  remove(request: StoredSearch) {
    this.service.removeStoredSearch(request.name);
  }

  load(request: StoredSearch) {
    this.service.loadStoredSearchSelectedCriteria(request.name)
  }


  add(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;
    if ((value || '').trim()) {
      this.service.addStoredSearch(value,this.selectedCriteria);
    }
    // Reset the input value
    if (input) {
      input.value = '';
    }
  }
}
