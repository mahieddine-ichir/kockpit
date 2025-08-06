import {Injectable} from '@angular/core';
import {Subject} from 'rxjs';
import {Item} from './item';

@Injectable()
export class BreadCrumbService {

  public emitChangeSourceItem = new Subject<Item>();
  public changeEmittedItem$ = this.emitChangeSourceItem.asObservable();
  public currentBreadcrumb: Item = new Item();
  private resetOn: string;

  private _breadcrumb: Item[] = [];

  get breadcrumb(): Item[] {
    return this._breadcrumb;
  }

  public emitChangeItem(item: Item): void {
    this.emitChangeSourceItem.next(item);
    this.applyItem(item);
  }

  public reset(title: string) {
    this.resetOn = title;
  }

  public backItem(item: Item): void {
    item.action = '-1';
    this.emitChangeItem(item);
  }

  public resetItem(title: string): void {
    this.emitChangeSourceItem.next(null);
  }

  //FIXME YYO j'ai enlevé la verif sur le title...
  public getItemData(): Item {
    if (this.currentBreadcrumb && this.currentBreadcrumb.action == '1') {
      return this.currentBreadcrumb;
    }
    return null;
  }

  public applyItem(item: Item): Item[] {
    if (this.resetOn != null) {
      this._breadcrumb = [];
      this.currentBreadcrumb = item;
      this._breadcrumb.push(item);
    } else if (item.action === '-1') {
      item.action = '1';
      while (item.title !== this.currentBreadcrumb.title && this._breadcrumb.length > 0) {
        this._breadcrumb.pop();
        this.currentBreadcrumb = this._breadcrumb[this._breadcrumb.length - 1];
      }
    } else {
      if (!this._breadcrumb.some(e => e.title === item.title && e.link === item.link)) {
        this.currentBreadcrumb = item;
        this._breadcrumb.push(item);
      }
    }
    this.resetOn = null;
    return this._breadcrumb;
  }

  /**
   * to change item title dynamically
   * @param {string} newTitle
   */
  public updateItemTitle(newTitle: string): void {
    this.currentBreadcrumb.title = newTitle;
  }
}
