import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {UserNotification} from './usernotification.model';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class UsernotificationService {

  constructor(private http: HttpClient) {
  }

  getUserNotification(): Observable<any> {
    const parameter = new HttpParams();
    return this.http.get<UserNotification>(`${environment.backend}/api/services/notification/all`, {params: parameter});
  }

  getNumberOfNewNotification(timestamp: number): Observable<any> {
    const parameter = new HttpParams();
    return this.http.get<UserNotification>(`${environment.backend}/api/services/notification/number_new_notif/${timestamp}`);
  }
}
