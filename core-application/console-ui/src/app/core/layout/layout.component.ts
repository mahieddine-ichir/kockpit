import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {BreadCrumbService} from "../../kiss-components/breadcrumb/breadcrumb-service";
import {SidenavService} from "../sidenav/sidenav.service";
import {Router} from "@angular/router";
import {animate, state, style, transition, trigger} from "@angular/animations";
import {SidenavState} from "../sidenav/sidenav-state.enum";
import {Observable} from "rxjs";

@Component({
  selector: 'fury-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
  encapsulation: ViewEncapsulation.None,
  animations: [
    trigger('sidenavState', [
      state(SidenavState.Collapsed, style({
        flexBasis: '0',
        width: '70px'
      })),
      state(SidenavState.CollapsedHover, style({
        width: '{{menuWidth}}'
      }), {params: {menuWidth: '270px'}}
      ),
      state(SidenavState.Expanded, style({
        minWidth: '230px',
        position: 'relative',
        width: '{{menuWidth}}'
      }), {params: {menuWidth: '270px'}}
      ),
      transition(`${SidenavState.Expanded} => ${SidenavState.CollapsedHover}`, [
        style({ position: 'initial' }),
        animate('300ms cubic-bezier(.35, 0, .25, 1)')
      ]),
      transition(`${SidenavState.Expanded} => ${SidenavState.Collapsed}`, [
        style({ position: 'initial' }),
        animate('300ms cubic-bezier(.35, 0, .25, 1)')
      ]),
      transition(`${SidenavState.CollapsedHover} => ${SidenavState.Collapsed}`, [
        animate('300ms cubic-bezier(.35, 0, .25, 1)')
      ]),
      transition(`${SidenavState.Collapsed} => ${SidenavState.CollapsedHover}`,
        animate('300ms cubic-bezier(.35, 0, .25, 1)')
      )
    ])
  ]
})
export class LayoutComponent implements OnInit {

  sidenavState$: Observable<SidenavState>;
  menuWidth: string;

  constructor(
      public breadCrumbService: BreadCrumbService,
      private router: Router,
      private sidenavService: SidenavService
      ) {
  }

  ngOnInit() {
    this.sidenavService._env$.subscribe(() => {
      this.sidenavState$ = this.sidenavService.sidenavState$;
      const menuWidthKey = this.sidenavService.fetchKeyBasedOnDomainEnv() + 'menuWidthSaved';
      const menuWidthFromStorage = localStorage.getItem(menuWidthKey);
      this.menuWidth = menuWidthFromStorage !== null ? menuWidthFromStorage : '270px';
    });

      /*
    this.sideNavService.configureMenu()
        .subscribe(apps => {
          console.log("navigate to audit/"+apps[0].name);
          this.router.navigate(['audit/'+apps[0].name]);
        });

       */
  }

  onActivate(e, scrollContainer) {
    scrollContainer.scrollTop = 0;
  }

  onResizeEnd(event) {
    const menuWidthKey = this.sidenavService.fetchKeyBasedOnDomainEnv() + 'menuWidthSaved';
    this.menuWidth = event.info.width < 230 ? '230px' : `${event.info.width}px`;
    localStorage.setItem(menuWidthKey, this.menuWidth);
  }

}
