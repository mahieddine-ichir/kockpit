import {environment} from "../../environments/environment";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {HttpClient, HttpParams} from "@angular/common/http";


@Injectable({
  providedIn: 'root',
})
export class TokenService {

  resetPasswordUrl = environment.backend.replace("/admin", "") + "/reset-password";

  constructor(private http: HttpClient) {}

  generateToken(username: string): Observable<string> {
    let params = new HttpParams().set('username', username);
    return this.http.put(this.resetPasswordUrl, {}, {params, responseType: 'text'})
  }

  verifyToken(token: string): Observable<object> {
    let params = new HttpParams().set('token', token);
    return this.http.get(this.resetPasswordUrl, {params})
  }

  setPassword(token: string, password: string): Observable<object> {
    let params = new HttpParams().set('token', token);
    return this.http.post(this.resetPasswordUrl, password, {params});
  }
}
