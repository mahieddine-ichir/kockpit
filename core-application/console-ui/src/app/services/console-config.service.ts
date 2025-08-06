import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {ConsoleConfig, ConsoleServiceConfig} from '../model/application';
import {Observable, of} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ConsoleConfigService {

  private _consoleConfig: ConsoleConfig;
  private _availableMenuRoutes: string[];

  constructor(private http: HttpClient) {
  }

  initialize(): Observable<ConsoleConfig> {
    // Already loaded?
    if (this._consoleConfig) {
      return of(this._consoleConfig);
    }
    return this.internalLoad();
  }

  refresh(): Observable<ConsoleConfig> {
    return this.internalLoad();
  }

  private internalLoad() {
    const that = this;
    return new Observable<ConsoleConfig>(observer => {
      this.http.get<ConsoleConfig>(`${environment.backend}/api/console/config`)
        .subscribe({
          next(value) {
            that._processConsoleConfig(value);
            observer.next(value);
          },
          error(err) {
            observer.error(err);
          },
          complete() {
            observer.complete();
          }
        })
    });
  }

  private _processConsoleConfig(consoleConfig: ConsoleConfig) {
    this._consoleConfig = consoleConfig;
    this._availableMenuRoutes = this._consoleConfig.consoleServiceMenus
      .flatMap(value => value.menuItems)
      .map(value => value.route);
    console.log('menuRoutes: ', this._availableMenuRoutes);
  }

  public getConsoleConfigForService(serviceId: string): ConsoleServiceConfig {
    if (this._consoleConfig == null) {
      return null;
    }
    return this._consoleConfig.consoleServiceConfigs.find(value => value.serviceId === serviceId);
  }

  get availableMenuRoutes(): string[] {
    return this._availableMenuRoutes;
  }
}
