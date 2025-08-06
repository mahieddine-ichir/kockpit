import {Component, EventEmitter, Input, Output} from '@angular/core';
import {SelectedCriterion} from "./types/Criterion";
import {Operand} from "./types/Operand";
import { formatDate } from '@angular/common';

@Component({
  selector: 'selected-criteria-component',
  template: `
    <mat-form-field>
      <mat-chip-list #chipList fxLayout="row">
        <mat-chip *ngFor="let criterion of selectedCriteria" [selectable]="selectable"
                  [removable]="removable" (removed)="removeCriterion(criterion)" (click)="editCriterion(criterion)">
          {{criterion.name}} <span>&nbsp;{{criterion.operation.operator.name}}&nbsp;</span>
          {{formatOperandToString(criterion.operation.operand)}}
          <mat-icon matChipRemove *ngIf="removable">cancel</mat-icon>
        </mat-chip>
        <input disabled
               [matChipInputFor]="chipList"
               [matChipInputAddOnBlur]="addOnBlur">
      </mat-chip-list>
    </mat-form-field>
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
export class SelectedCriteriaComponent {

  visible = true;
  selectable = true;
  removable = true;
  addOnBlur = true;

  @Input() selectedCriteria: SelectedCriterion[];
  @Output() onDeleteSelectedCriterion = new EventEmitter<SelectedCriterion>();
  @Output() onEditSelectedCriterion = new EventEmitter<SelectedCriterion>();

  formatOperandToString(operand: Operand) {

    if (operand.type === "DATE" && operand.value && operand.valueTo) {
      return this.formatDateToString(operand.value) + " and " + this.formatDateToString(operand.valueTo);
    } else if (operand.type === "DATE" && operand.value) {
      return this.formatDateToString(operand.value);
    } else if ((operand.value != null && operand.valueTo != null) ) {
      return operand.value + " and " + operand.valueTo;
    } else {
      return operand.value;
    }
  }

  private formatDateToString(date: string) {
    return formatDate(date, 'dd-MM-yyyy HH:mm:ss', "en-US");
  }

  removeCriterion(criterion: SelectedCriterion) {
    this.onDeleteSelectedCriterion.emit(criterion);
  }

  editCriterion(criterion: SelectedCriterion) {
    this.onEditSelectedCriterion.emit(criterion)
  }
}
