import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot} from '@angular/router';

@Injectable()
export class DeprecatedMarkerGuard implements CanActivate {
  canActivate(route:ActivatedRouteSnapshot, state:RouterStateSnapshot) {
    console.log('Deprecated marked route: ', route, ' and state: ', state);
    return true;
  }
}
