import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {AuthenticationService} from './authentication-service';
import {Observable, tap} from 'rxjs';
import {Router} from '@angular/router';
import {Injectable} from '@angular/core';

@Injectable()
export class AuthenticationErrorInterceptor implements HttpInterceptor {

  constructor(private authenticationService: AuthenticationService,
              private router: Router) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      tap({
        next: httpEvent => {},
        error: err => {
          if (err instanceof HttpErrorResponse) {
            if (err.status === 401) {
              this.authenticationService.logout();
              this.router.navigate(['/login']);
            } else if (err.status === 403) {
              this.authenticationService.logout();
              // redirect to the login route
              // FIXME - should we must navigate (or not) to Access denied msg?
              this.router.navigate(['/login']);
            }
          }
        }
      })
    );
  }
}
