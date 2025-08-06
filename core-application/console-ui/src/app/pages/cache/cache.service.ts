import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Observable} from "rxjs";
import {CacheCommands, CacheState} from "./cache.model";

@Injectable({
  providedIn: 'root',
})
export class CacheService {


  constructor(private http: HttpClient) {
  }

  getCacheState(domain: string, env: string, applicationId: string, cacheName: string): Observable<CacheState> {
    let parameter = new HttpParams();
    return this.http.get<CacheState>(`${environment.backend}/api/services/${domain}/${env}/cache/${applicationId}/${cacheName}/_state` ,
        {params : parameter});
  }

  getCommands(domain: string, env: string, applicationId: string, cacheName: string): Observable<CacheCommands> {
    let parameter = new HttpParams();
    return this.http.get<CacheCommands>(`${environment.backend}/api/services/${domain}/${env}/cache/${applicationId}/${cacheName}/_commands` ,
        {params : parameter});
  }

  emptyCache(domain: string, env: string, applicationId: string, cacheName: string): Observable<CacheState> {
    let parameter = new HttpParams();
    return this.http.get<CacheState>(`${environment.backend}/api/services/${domain}/${env}/cache/${applicationId}/${cacheName}/_reload` ,
        {params : parameter});
  }

  resetStats(domain: string, env: string, applicationId: string, cacheName: string): Observable<CacheState> {
    let parameter = new HttpParams();
    return this.http.get<CacheState>(`${environment.backend}/api/services/${domain}/${env}/cache/${applicationId}/${cacheName}/_refresh` ,
        {params : parameter});
  }
}
