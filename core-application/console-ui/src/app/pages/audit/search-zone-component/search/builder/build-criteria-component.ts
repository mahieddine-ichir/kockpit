import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {BackEndOperation} from "../types/BackEndOperation";
import {Criterion, SelectedCriterion} from "../types/Criterion";
import {SelectedOperation} from "../types/SelectedOperation";
import {Subject,} from "rxjs";
import {Operand} from "../types/Operand";

@Component({
  selector: 'build-criteria-component',
  templateUrl: './build-criteria-component.html',
  styleUrls: ['./build-criteria-component.scss']
})
export class BuildCriteriaComponent implements OnInit {

  operand: Operand;
  selectedOperator: BackEndOperation = new BackEndOperation('EQ', null);

  @Input() editSelectedCriteria: Subject<SelectedCriterion>;
  @Input() option: Criterion;

  @Output() onCriteriaBuilderCancel = new EventEmitter<Criterion>();
  @Output() onCriteriaBuilderSubmit = new EventEmitter<SelectedCriterion>();

  closeSearchCriteriaBuilder() {
    this.onCriteriaBuilderCancel.emit(this.option);
  }

  submitSearchCriteriaBuilder() {
    this.onCriteriaBuilderSubmit.emit(new SelectedCriterion(this.option, new SelectedOperation(this.selectedOperator, this.operand)));
  }

  updateSelectedOperator(selectedOperator: BackEndOperation) {
     this.selectedOperator = selectedOperator;
  }

  updateOperandOnInputChange($event) {
    this.operand = $event;
  }

  isChecked(operator: BackEndOperation) {
    if (!this.selectedOperator || !this.option.type.operations.map(x => x.name).includes(this.selectedOperator.name)) {
      this.selectedOperator = this.option.type.operations.filter(x => x.name === 'EQ')[0];
    }
    return this.selectedOperator && this.selectedOperator.name === operator.name;
  }

  ngOnInit(): void {
    this.editSelectedCriteria.subscribe(data => {
      this.selectedOperator = data.operation.operator;
    });
  }
}
