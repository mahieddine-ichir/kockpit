import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Criterion, SelectedCriterion} from "../types/Criterion";
import {Subject} from "rxjs";
import {Operand} from "../types/Operand";
import {BackEndOperation} from "../types/BackEndOperation";

@Component({
  selector: 'wcp-search-inputs',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './search-inputs.component.html',
  styles: [``]
})
export class SearchInputsComponent implements OnInit {
  private operand: Operand;
  private _criterion: Criterion;
  @Input() editSelectedCriteria: Subject<SelectedCriterion>;
  @Input() index: number;
  @Input() selectedOperator: BackEndOperation;
  @Output() onChange: EventEmitter<any> = new EventEmitter<any>();


  @Input() set criterion(criterion: Criterion) {
    //triggered when criterion change
    this._criterion = criterion;
    this.initInput();
  }

  get criterion(): Criterion {
    return this._criterion;
  }

  updateOperand(operand: Operand) {
    this.operand = operand;
    this.onChange.emit(operand);
  }

  private initInput() {
    this.operand = new Operand(null, null, this._criterion.type.name);
    if (this.operand && this._criterion.type.name === 'DATE') {
      let date = new Date();
      date.setHours(0, 0, 0);
      this.operand.value = date.toISOString();
    }
    this.onChange.emit(this.operand);
  }

  ngOnInit() {
    this.initInput();
    this.editSelectedCriteria.subscribe(data => {
      //Used to copy actual filter (work but we have to click twice)
      this.operand = Object.assign({}, data.operation.operand);
    });
  }

  isBetweenOperator() {
    return this.selectedOperator && (this.selectedOperator.name === 'BETWEEN' || this.selectedOperator.name === 'NOT_BETWEEN');
  }

}
