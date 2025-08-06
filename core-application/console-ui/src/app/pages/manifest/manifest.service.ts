import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Manifest} from './manifest.model';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ManifestService {

  constructor(private http: HttpClient) {
  }

  getManifest(): Observable<any> {
    const parameter = new HttpParams();
    return this.http.get<Manifest[]>(`${environment.backend}/api/console/manifests`, {params: parameter});
  }

  postManifestFile(fileToUpload: File[]): Observable<any> {
    const formData: FormData = new FormData();
    const parameter = new HttpParams();
    for (const file of fileToUpload) {
      formData.append('file', file, file.name);
    }
    return this.http.post(`${environment.backend}/api/console/manifests`, formData, {params: parameter});
  }

  postDeleteManifestInMemory(fileName: string) {
    const parameter = new HttpParams();
    return this.http.post(`${environment.backend}/api/console/manifests/delete`, fileName, {params: parameter});
  }
}
