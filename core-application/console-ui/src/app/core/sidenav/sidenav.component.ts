import { ChangeDetectorRef, Component, HostBinding, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { takeUntil, map } from 'rxjs/operators';
import { componentDestroyed } from '../common/component-destroyed';
import { SidenavItem } from './sidenav-item/sidenav-item.interface';
import { SidenavState } from './sidenav-state.enum';
import { SidenavService } from './sidenav.service';
import { ToastrService } from 'ngx-toastr';
import { FormControl } from '@angular/forms';
import { MatSelect } from '@angular/material/select';

@Component({
  selector: 'fury-sidenav',
  templateUrl: './sidenav.component.html',
  styleUrls: ['./sidenav.component.scss']
})
export class SidenavComponent implements OnInit, OnDestroy {

  items$: Observable<SidenavItem[]>;
  itemsByGroup: Record<string, SidenavItem[]>;
  selectedGroup: string;

  sidenavState$: Observable<SidenavState>;

  selectedSubDomain: FormControl;
  subDomains: Set<string>;
  sidenavState: string;
  isCollapsedState: boolean;

  @HostBinding('class')
  get sidenavClasses() {
    return `sidenav ${this.sidenavState}`;
  }
  @ViewChild('matSelectSubDomain')
  matSelectSubDomain: MatSelect;
  isCollapsed: boolean;
  isMobile: boolean;

  constructor(private router: Router,
    private toastr: ToastrService,
    private sidenavService: SidenavService,
    private cd: ChangeDetectorRef) {
  }

  ngOnInit() {
    this.custom();
    this.selectedSubDomain = this.sidenavService.selectedSubDomain;
    this.sidenavService.setSelectedSubDomain();
    this.sidenavState$ = this.sidenavService.sidenavState$;

    this.sidenavService.sidenavState$.subscribe(sidenavState => this.sidenavState = sidenavState);
    this.sidenavService._env$.subscribe(() => {
      this.sidenavService.resetSelectedSubDomain();
      this.retrieveSidenavItems().subscribe((items) => {
        if (!this.selectedGroup) this.selectedGroup = 'service'
        this.subDomains = this.retrieveSubDomains(items);
        this.itemsByGroup = this.groupSidenavItems(items, this.selectedGroup);
      })
    });

    this.sidenavService.sidenavState$.pipe(
      takeUntil(componentDestroyed(this))
    ).subscribe(sidenavState => {
      this.isCollapsedState = sidenavState === SidenavState.Collapsed || sidenavState === SidenavState.CollapsedHover;
      this.isCollapsed = sidenavState === SidenavState.Collapsed;
      this.custom();
      this.cd.markForCheck();
    });
  }

  toggleCollapsed() {
    this.sidenavService.sidenavState = this.sidenavService.sidenavState === SidenavState.Expanded ? SidenavState.Collapsed : SidenavState.Expanded;
  }

  @HostListener('mouseenter')
  @HostListener('touchenter')
  onMouseEnter() {
    if (this.isCollapsedState && !this.isMobile) {
      this.sidenavService.sidenavState = SidenavState.CollapsedHover;
    }
  }

  @HostListener('mouseleave')
  @HostListener('touchleave')
  onMouseLeave() {
    if (this.isCollapsedState && !this.isMobile && !this.matSelectSubDomain.panelOpen) {
      this.sidenavService.sidenavState = SidenavState.Collapsed;
    }
  }

  ngOnDestroy() {
  }

  retrieveSidenavItems(): Observable<SidenavItem[]> {
    return this.sidenavService.getEnvironmentItems().pipe(
      map((items: SidenavItem[]) => {
        return this.sidenavService.sortRecursive(items, 'position');
      })
    );
  }

  retrieveSubDomains(items: SidenavItem[]): Set<string> {
    const subDomains = new Set(items
      .map(item => item.subDomain)
      .filter(item => item != null));
    return subDomains.size === 0 ? null : subDomains;
  }

  groupSidenavItems(items: SidenavItem[], groupItemsBy: string) {
    this.selectedGroup = groupItemsBy;
    const groupBy = <T, K extends keyof any>(list: SidenavItem[], getKey: (item: SidenavItem) => K) =>
      list.reduce((previous, currentItem: SidenavItem) => {
        if (!this.selectedSubDomain.value || this.selectedSubDomain.value === currentItem.subDomain) {
          const group = getKey(currentItem);
          if (!previous[group]) previous[group] = [];
          previous[group].push(currentItem);
        }
        return previous;
      }, {} as Record<K, SidenavItem[]>);

    if(groupItemsBy == "app") {
      return groupBy(items, i => i.appId);
    } else {
      return groupBy(items, i => i.serviceId);
    }
  }

  updateGroupByFilter(groupBy:string) {
    this.retrieveSidenavItems().subscribe((items) => this.itemsByGroup = this.groupSidenavItems(items, groupBy));
  }

  onSelectedSubDomainChanged(subDomain) {
    const subDomainKey = this.sidenavService.fetchKeyBasedOnDomainEnv() + 'subDomainSaved';
    if (subDomain === localStorage.getItem(subDomainKey)) {
      localStorage.removeItem(subDomainKey);
      this.sidenavService.selectedSubDomain.reset();
    } else {
      localStorage.setItem(subDomainKey, subDomain);
      this.router.navigate(['/']);
    }
    this.updateGroupByFilter(this.selectedGroup);
  }

  custom() {
    fetch('./assets/custom.json')
      .then(function (response) {
        return response.json();
      })
      .then(function (data) {
        function appendData(data) {
          const sidenavtitle = document.getElementById('sidenavtitle');
          if(data.sidenavtitle) {
            sidenavtitle.textContent = data.sidenavtitle;
          }
        }
        appendData(data);
      })
      .catch(function(error) {
      });
  }

  hasDashboardRight() {
    return true;
  }
}
