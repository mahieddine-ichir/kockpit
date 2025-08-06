import {Component, EventEmitter, Input, OnInit, Output, ChangeDetectorRef, OnChanges, SimpleChanges} from '@angular/core';
import {Criterion, SelectedCriterion} from './search/types/Criterion';
import * as _ from 'lodash';
import {Subject} from 'rxjs';
import {StoredSearchService} from '../stored-search.service';
import {SelectedOperation} from './search/types/SelectedOperation';
import {BackEndOperation} from './search/types/BackEndOperation';
import {RequestService} from '../request.service';
import {AuditRequestPage, SearchQuery} from '../request.model';
import {ActivatedRoute} from '@angular/router';


@Component({
  selector: 'wcc-search-zone-component',
  templateUrl: './search-zone.component.html',
  styleUrls: ['./search-zone.component.scss']
})
export class SearchZoneComponent implements OnInit, OnChanges {

  @Output()
  onBeginSearch = new EventEmitter<void>();

  @Output()
  onEndSearch = new EventEmitter<AuditRequestPage>();

  @Input()
  searchMap: Map<string, string>;

  @Input()
  auditViewName: string;

  @Input()
  domain: string;

  @Input()
  env: string;

  @Input()
  size: number;

  @Input()
  from: number;

  @Input()
  searchCriteria: Criterion[];

  selectedItem: Criterion;

  isBuilding: Boolean;
  isEditMode: Boolean;

  isLoading = true;
  isError = false;

  selectedCriteria: SelectedCriterion[];
  selectedCriteriaKey: string;

  editSelectedCriterion: Subject<SelectedCriterion> = new Subject<SelectedCriterion>();

  constructor(private storedSearchService: StoredSearchService,
              private requestService: RequestService,
              private route: ActivatedRoute,
              private cdr: ChangeDetectorRef
  ) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['from'] !== undefined) {
      this.from = changes['from']['currentValue'] as number;
      this.search();
    } else if (changes['size'] !== undefined) {
      this.size = changes['size']['currentValue'] as number;
      this.search();
    }
  }

  ngOnInit() {

    this.route.params.subscribe(value => {
      // TODO check why input param not auto updated from parent change
      this.auditViewName = this.route.snapshot.params['auditViewName'];
      this.domain = this.route.snapshot.params['domain'];
      this.env = this.route.snapshot.params['env'];

      this.selectedCriteriaKey = 'selected-criteria-' + this.auditViewName;
      this.selectedCriteria = this.load(this.selectedCriteriaKey) || [];
      this.search();
      this.storedSearchService.selectedCriteriaLoadedChanged$.subscribe(_value => this.selectedCriteria = _value);
    });
  }

  selectCriteria($event: Criterion) {
    this.selectedItem = $event;
    this.isBuilding = true;
  }

  closeCriteriaBuilder() {
    this.isBuilding = false;
    this.isEditMode = false;
    this.selectedItem = null;
  }

  updateSelectedCriteria($event: SelectedCriterion) {
    if (this.isEditMode) {
      const editedCriteria = this.selectedCriteria.filter(x => x.name === $event.name)[0];

      if (editedCriteria) {
        editedCriteria.operation = $event.operation;
      }
    } else {
      this.selectedCriteria.push($event);
    }

    this.closeCriteriaBuilder();
  }

  onDeleteSelectedCriterion($event: SelectedCriterion) {
    this.selectedCriteria = this.selectedCriteria
      .filter(obj => obj !== $event);
  }

  onEditSelectedCriterion($event: SelectedCriterion) {
    this.isBuilding = true;
    this.isEditMode = true;

    this.selectedItem = this.searchCriteria.filter(x => x.name === $event.name)[0];
    this.cdr.detectChanges();

    this.editSelectedCriterion.next($event);
  }

  search() {
    this.isError = false;
    this.isLoading = true;
    this.onBeginSearch.emit();

    const query = this.buildSearchCriteria();
    if (query) {
      this.requestService.searchV2(this.domain, this.env, this.auditViewName, query, this.from, this.size)
        .subscribe(value => {
          this.onEndSearch.emit(value);
          this.isLoading = false;
          this.store(this.selectedCriteriaKey, this.selectedCriteria);
        }, () => {
          this.isLoading = false;
          this.isError = true;
          _.delay(() => this.isError = false, 3000);
        });
    }
  }

  private buildSearchCriteria(): SearchQuery[] {
    if (! this.selectedCriteria) {
      return null;
    }
    return this.selectedCriteria.map(selectedCriteria => {
        return SelectedCriterion.toSearchQuery(selectedCriteria);
      });
  }

  store(key: string, value: any) {
    window.localStorage.setItem(key, JSON.stringify(value));
  }

  load(key: string) {
    return JSON.parse(window.localStorage.getItem(key));
  }
}
