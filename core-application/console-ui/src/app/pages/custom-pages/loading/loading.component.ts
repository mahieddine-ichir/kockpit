import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { fadeOutAnimation } from '../../../core/common/route.animation';
import { AuthenticationService } from '../../../services/authentication-service';
import { ToastrService } from 'ngx-toastr';
import { SidenavService } from '../../../core/sidenav/sidenav.service';
import { ConsoleConfigService } from '../../../services/console-config.service';
import { ConsoleConfig } from '../../../model/application';

@Component({
  selector: 'fury-login',
  templateUrl: './loading.component.html',
  styleUrls: ['./loading.component.scss'],
  host: {
    '[@fadeOutAnimation]': 'true'
  },
  animations: [fadeOutAnimation]
})
export class LoadingComponent implements OnInit {

  public isSpinnerVisible: boolean = true;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private authenticationService: AuthenticationService,
              private toastr: ToastrService,
              private consoleConfigService: ConsoleConfigService,
              private sideNav: SidenavService
  ) { }

  ngOnInit() {
    const that = this;
    this.authenticationService.getLoggedUser()
      .subscribe({
        next: value => {
          this.consoleConfigService.initialize().subscribe({
            next(configValue) {
              that.toastr.info(`Console initialized! Let's start ...`);
              that.loadMenu(configValue);
              that.isSpinnerVisible = false;

              const origin = that.route.snapshot.queryParamMap.get("origin");
              that.router.navigate([origin]);
            },
            error(error) {
              this.toastr.error(`Error initializing console ... Error: ${error}`);
            }
          });
        },
        error: err => {
          console.log('Error login: ', err);
          // GO to login
          this.toastr.error(`Error getting user information: ${err}`, 'Not logged');
          this.authenticationService.redirectToLogin();
        }
      });
  }

  private loadMenu(consoleConfig: ConsoleConfig) {
    consoleConfig.consoleServiceMenus.forEach(value => {
      value.menuItems.forEach(item => {
        this.sideNav.addItem({
          name: item.label,
          routeOrFunction: item.route,
          icon: this.iconByServiceType(value.serviceId),
          position: 0,
          pathMatchExact: true,
          appContext: item.id,
          env: item.env,
          domain: item.domain,
          subDomain: item.subDomain,
          appId: item.app,
          serviceId: value.serviceId
        });
      })
    });
  }


  private iconByServiceType(service: String) {
    let icon = "";
    switch (service) {
      case "cache": {
        icon = 'cached';
        break;
      }
      case "sqsdlq": {
        icon = 'delete';
        break;
      }
      case "audit": {
        icon = 'assessment';
        break;
      }
      default: {
        icon = 'settings_applications';
        break;
      }
    }
    return icon;
  }
}
