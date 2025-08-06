import {Component, OnInit} from '@angular/core';
import {environment} from '../environments/environment';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthenticationService} from './services/authentication-service';

@Component({
  selector: 'fury-root',
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {

  private readonly origin:string;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private authenticationService: AuthenticationService) {
    // Special case to init something special depending env
    if (environment.initFn) {
      environment.initFn();
    }

    // Dev mode (could change backend url via url parameter)
    if (!environment.production) {
      const params = this.parseQueryString(null);
      console.log('ng init app module', environment.backend, params);
      if (params['apiroot']) {
        environment.backend = params['apiroot'] + '/admin';
      }
      console.log('ng init app module', environment.backend);
    }

    // Special case for auto domain discovery
    if (environment.autoDomainDiscovery) {
      const appHost = window.location.host.replace('consoleui', 'console');
      environment.backend = `https://${appHost}/ihm_api`;
    }

    this.origin = this.router.url;
    let lastIndexOf = window.location.href.lastIndexOf('#');
    if (lastIndexOf > 0) {
      this.origin = window.location.href.substring(lastIndexOf + 1);
    }
  }

  ngOnInit(): void {
    // Force manually move to home page
    // Must be delay to execute after init / core angular app loading
    setTimeout( () => {
      this.router.navigate([''], { queryParams: {origin: this.origin}});
    }, 100);
  }

  parseQueryString(queryString: string): Map<string, string> {
    // if the query string is NULL
    if (queryString == null) {
      const strings = window.location.href.split('?');
      if (strings.length > 1) {
        queryString = window.location.href.split('?')[1];
      } else {
        queryString = '';
      }
    }

    const params = new Map<string, string>();

    const queries = queryString.split("&");

    queries.forEach((indexQuery: string) => {
      const indexPair = indexQuery.split("=");

      const queryKey = decodeURIComponent(indexPair[0]);
      params[queryKey] = decodeURIComponent(indexPair.length > 1 ? indexPair[1] : '');
    });

    return params;
  }
}
