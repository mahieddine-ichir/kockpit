import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs';
import {DashboardPage} from './dashboard.model';

@Injectable({
  providedIn: 'root',
})
export class DashboardService {

  constructor(
    private http: HttpClient) {
  }

  loadData(key: string, value: string, from: number = 0, size: number = 10): Observable<DashboardPage> {
    const params = new HttpParams()
      .set('key', key)
      .set('value', value)
      .set('from', from)
      .set('size', size);

    return this.http.get<DashboardPage>(`${environment.backend}/api/services/dashboard`, {
      params: params
    });
  }
}
