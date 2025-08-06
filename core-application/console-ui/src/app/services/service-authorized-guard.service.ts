import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, CanActivateChild, Router, RouterStateSnapshot} from '@angular/router';
import {AuthenticationService} from './authentication-service';
import {ToastrService} from 'ngx-toastr';
import {ConsoleConfigService} from './console-config.service';

@Injectable()
export class ServiceAuthorizedGuard implements CanActivate, CanActivateChild {

  constructor(private router: Router,
              private authenticationService: AuthenticationService,
              private consoleConfigService: ConsoleConfigService,
              private toastr: ToastrService) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    const url = '/services/' + route.url.join('/');
    return this.authorize(url);
  }

  canActivateChild(childRoute: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    return this.authorize(state.url);
  }

  private authorize(url: string) {
    console.log(url);
    const anyFound = this.consoleConfigService.availableMenuRoutes.filter(value => url.startsWith(value));
    if (anyFound.length > 0) {
      // User is authorized to access this service endpoint
      return true;
    }

    // User is not authorized to access admin page. Notify and redirect to dashboard
    this.router.navigate(['/']).then();
    this.toastr.error('Unauthorized URL: ' + url);

    return false;
  }
}
