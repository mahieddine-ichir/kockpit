import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {FeatureflippingConfig} from "./featureflipping.model";

@Injectable({
  providedIn: 'root',
})
export class FeatureflippingService {
  constructor(private http: HttpClient) {
  }

  getConfig(domain: string, env: string, applicationId: string): Observable<FeatureflippingConfig> {
    const parameter = new HttpParams();
    return this.http.get<FeatureflippingConfig>(`${environment.backend}/api/services/${domain}/${env}/featureflipping/${applicationId}` ,
      {params : parameter});
  }

  updateConfig(domain: string, env: string, applicationId: string, dynamoConfig: FeatureflippingConfig): Observable<any> {
    return this.http.put(`${environment.backend}/api/services/${domain}/${env}/featureflipping/${applicationId}/update`, dynamoConfig);
  }

  updateProperty(domain: string, env: string, applicationId: string, propertyName: String, newValue: String): Observable<any> {
    return this.http.put(`${environment.backend}/api/services/${domain}/${env}/featureflipping/${applicationId}/${propertyName}`, newValue);
  }

  forceReloadInstances(domain: string, env: string, applicationId: string): Observable<any> {
    return this.http.post(`${environment.backend}/api/services/${domain}/${env}/featureflipping/${applicationId}/refresh`, null);
  }

  flushHistory(domain: string, env: string, applicationId: string): Observable<any> {
    return this.http.post(`${environment.backend}/api/services/${domain}/${env}/featureflipping/${applicationId}/flush`, null);
  }

}
