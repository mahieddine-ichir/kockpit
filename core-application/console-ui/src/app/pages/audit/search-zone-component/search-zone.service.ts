import {Injectable} from "@angular/core";
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {Criterion} from "./search/types/Criterion";
import {environment} from "../../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class SearchZoneService {

  constructor(private httpClient: HttpClient) {
  }

  loadMetadata(appName: string): Observable<Criterion[]> {
    console.log(`Loading metadata for ${appName}`)
    return this.httpClient.get<Criterion[]>(environment.backend + "/requests/metadata", {
      params: new HttpParams().set("name", appName)
    });
  }

  uploadMetadata(appName: string, formData: FormData): Observable<Criterion[]> {
    return this.httpClient.post<Criterion[]>(environment.backend + "/requests/metadata", formData, {
      params: new HttpParams().set("name", appName)
    });
  }
}
