import {Injectable} from '@angular/core';
import {CanActivate, Router} from '@angular/router';
import {AuthenticationService} from './authentication-service';

@Injectable()
export class AuthGuard implements CanActivate {

  constructor(private router: Router,
              private authenticationService: AuthenticationService
  ) {
  }

  canActivate() {
    // return true;
    if (this.authenticationService.isAuthenticated()) {
      console.log('Already authenticated!');
      return true;
    }
    // not logged in so redirect to login page
    return false;
  }
}
