import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpParams} from '@angular/common/http';
import {AuditRequestPage, SearchQuery} from './request.model';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class RequestService {
  constructor(private http: HttpClient) {
  }

  private static getServiceUrl(domain: string, env: string) {
    return `${environment.backend}/api/services/${domain}/${env}/audit/requests`;
  }

  auditById(domain: string, env: string, auditViewName: string, id: string): Observable<any> {
    const serviceUrl = RequestService.getServiceUrl(domain, env);
    return this.http.get<any>(`${serviceUrl}/${auditViewName}/${id}`, {observe: 'events', reportProgress: true});
  }

  search(domain: string, env: string, auditViewName: string, query: SearchQuery[], size: number): Observable<any> {
    const serviceUrl = RequestService.getServiceUrl(domain, env);
    return this.http.post<any[]>(`${serviceUrl}/${auditViewName}/_search`, query, {
      params: new HttpParams().set('size', `${size}`)
    });
  }

  searchV2(domain: string, env: string, auditViewName: string, query: SearchQuery[], from: number = 0, size: number = 5): Observable<AuditRequestPage> {
    const serviceUrl = RequestService.getServiceUrl(domain, env);
    const params = new HttpParams()
      .set('from', from)
      .set('size', size);
    return this.http.post<AuditRequestPage>(`${serviceUrl}/${auditViewName}/v2/_search`, query, {
      params: params
    });
  }
}
