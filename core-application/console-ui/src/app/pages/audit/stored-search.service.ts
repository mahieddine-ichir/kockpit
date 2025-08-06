import {Injectable} from '@angular/core';
import {Subject} from 'rxjs';
import {StoredSearch} from "./search-zone-component/search/types/StoredSearch";
import {SelectedCriterion} from "./search-zone-component/search/types/Criterion";
import * as _ from "lodash";

@Injectable({
  providedIn: 'root',
})
export class StoredSearchService {

  constructor() {
  }

  public STORED_SEARCH_KEY = 'stored-searchs';

  private storedSearchs: StoredSearch[] = this.load(this.STORED_SEARCH_KEY) || [];

  private storedSearchsSource = new Subject<StoredSearch[]>();

  private selectedCriteriaLoadedSource= new Subject<SelectedCriterion[]>();

  public selectedCriteriaLoadedChanged$=this.selectedCriteriaLoadedSource.asObservable();

  public storedSearchs$ = this.storedSearchsSource.asObservable();


  public loadStoredSearchs(){
    this.storedSearchs = this.getStoredSearchesFromLocalHistory();
    this.storedSearchsSource.next(this.storedSearchs)
  }

  public addStoredSearch(name: string, criterion: SelectedCriterion[]) {
    if (name && criterion) {
      this.storedSearchs = this.getStoredSearchesFromLocalHistory();
      if (!_(this.storedSearchs).find({'name': name})) {
        this.storedSearchs.push({name: name, selected: criterion});
        this.store(this.STORED_SEARCH_KEY, this.storedSearchs);
        this.storedSearchsSource.next(this.storedSearchs)
      }
    }
  }

  public removeStoredSearch(name: string) {
    if (name) {
      this.storedSearchs = this.getStoredSearchesFromLocalHistory();
      _.remove(this.storedSearchs, function (req) {
        return req.name === name;
      });
      this.store(this.STORED_SEARCH_KEY, this.storedSearchs);
      this.storedSearchsSource.next(this.storedSearchs)
    }
  }

  public loadStoredSearchSelectedCriteria(name: string) {
    let requestToLoad = _(this.storedSearchs).find(['name', name]);
    if(requestToLoad){
      this.selectedCriteriaLoadedSource.next(requestToLoad.selected);
    }
  }

  private getStoredSearchesFromLocalHistory() {
    return this.load(this.STORED_SEARCH_KEY) || [];
  }


  store(key: string, value: any) {
    window.localStorage.setItem(key, JSON.stringify(value));
  }

  load(key: string) {
    return JSON.parse(window.localStorage.getItem(key));
  }


}
