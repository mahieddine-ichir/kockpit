import {Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {ToastrService} from 'ngx-toastr';


export class LoggedUser {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  avatar: string;
}

@Injectable()
export class AuthenticationService {
  private accessToken: string;
  private idToken: string;
  private loggedUser: LoggedUser;

  constructor(
      private http: HttpClient,
      private toastr: ToastrService
  ) {
    this.accessToken = this._getTokenFromCookie('accessToken');
    this.idToken = this._getTokenFromCookie('idToken');
  }

  /**
   * Extract value of the authentication token from the cookies.
   * @return {String} Extracted access token. Null if not found.
   */
  _getTokenFromCookie(tokenType:string) {
    return document.cookie
      .split(';')
      .map(c => c.trim())
      .filter(cookie => {
        return cookie.indexOf(`.${tokenType}=`) > 0;
      })
      .map(cookie => {
        return cookie.split('=')[1];
      })[0] || null;
  }

  private loadCurrentUser(): Observable<LoggedUser> {
    const that = this;
    return new Observable<LoggedUser>(subscriber => {
      this.http.get<LoggedUser>(`${environment.backend}/me`, {
        headers: {'Authorization': `Bearer ${this.idToken}`}
      }).subscribe({
        next(value) {
          that.loggedUser = value;
          subscriber.next(value);
        },
        error(err) {
          subscriber.error(err);
        },
        complete() {
          subscriber.complete();
        }
      });
    });
  }

  logout(): boolean {
    // clear token remove user from local storage to log user out
    this.accessToken = null;
    this.loggedUser = null;
    return true;
  }

  private deleteAllCookies() {
    document.cookie.split(';')
      .forEach(function(c) { document.cookie = c.replace(/^ +/, '')
        .replace(/=.*/, '=;expires=' + new Date().toUTCString() + ';path=/'); });
  }

  isAuthenticated(): boolean {
    return this.loggedUser != null;
  }

  getAccessToken(): string {
    return this.accessToken;
  }

  getAuthorizationHeader(): string {
    return `Bearer ${this.idToken}`;
  }

  public getLoggedUser(): Observable<LoggedUser> {
    this.accessToken = this._getTokenFromCookie('accessToken');
    this.idToken = this._getTokenFromCookie('idToken');
    if (this.loggedUser !== undefined) {
      return of(this.loggedUser);
    }

    return this.loadCurrentUser();
  }

  public getCurrentUserRoles(): string[] {
    if (!this.loggedUser) {
      return [];
    }
    return this.loggedUser.roles;
  }

  public hasRole(authorizations: string): boolean {
    const roles = authorizations.split(',');
    for (const role of roles) {
      const currentUserRoles = this.getCurrentUserRoles();
      if (currentUserRoles.includes(role) || currentUserRoles.includes(`ROLE_${role}`)) {
        return true;
      }
    }
    return false;
  }

  redirectToLogin() {
    this.toastr.error('Login, user data error. Please force refresh/reload your browser.', 'User not logged');
  }
}


