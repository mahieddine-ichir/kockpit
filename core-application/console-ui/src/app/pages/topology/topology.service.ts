import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs';
import {TopologyModel} from './topology.model';

@Injectable({
  providedIn: 'root',
})
export class TopologyService {

  private readonly serviceUrl: string;

  constructor(private http: HttpClient) {
    this.serviceUrl = `${environment.backend}/api/console/applications`
  }

  topology(domain:string, env:string):Observable<TopologyModel[]> {
    return this.http.get<any>(`${this.serviceUrl}/${domain}/${env}`);
  }

  // auditById(cacheName:string, id: string): Observable<any> {
  //   return this.http.get<any>(`${this.serviceUrl}/${cacheName}/${id}`);
  // }

}
