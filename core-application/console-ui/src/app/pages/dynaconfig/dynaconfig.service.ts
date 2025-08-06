import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {DynaConfig} from "./dynaconfig.model";

@Injectable({
  providedIn: 'root',
})
export class DynaconfigService {
  constructor(private http: HttpClient) {
  }

  getDynaConfig(domain: string, env: string, applicationId: string): Observable<DynaConfig> {
    const parameter = new HttpParams();
    return this.http.get<DynaConfig>(`${environment.backend}/api/services/${domain}/${env}/dynaconfig/${applicationId}` ,
      {params : parameter});
  }

  updateDynaConfig(domain: string, env: string, applicationId: string, dynamoConfig: DynaConfig): Observable<any> {
    return this.http.put(`${environment.backend}/api/services/${domain}/${env}/dynaconfig/${applicationId}/update`, dynamoConfig);
  }

  updateProperty(domain: string, env: string, applicationId: string, propertyName: String, newValue: String): Observable<any> {
    return this.http.put(`${environment.backend}/api/services/${domain}/${env}/dynaconfig/${applicationId}/${propertyName}`, newValue);
  }

  forceReloadInstances(domain: string, env: string, applicationId: string): Observable<any> {
    return this.http.post(`${environment.backend}/api/services/${domain}/${env}/dynaconfig/${applicationId}/refresh`, null);
  }

  flushHistory(domain: string, env: string, applicationId: string): Observable<any> {
    return this.http.post(`${environment.backend}/api/services/${domain}/${env}/dynaconfig/${applicationId}/flush`, null);
  }

}
