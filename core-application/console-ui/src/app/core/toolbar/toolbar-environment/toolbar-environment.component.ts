import { Component } from '@angular/core';
import { SidenavService } from '../../sidenav/sidenav.service';
import { SidenavItem } from '../../sidenav/sidenav-item/sidenav-item.interface';
import { map } from 'rxjs/operators';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { FormControl } from '@angular/forms';

@Component({
  selector: 'fury-toolbar-environment',
  templateUrl: './toolbar-environment.component.html',
  styleUrls: ['./toolbar-environment.component.scss']
})
export class ToolbarEnvironmentComponent {
  items: SidenavItem[] = [];
  selectedEnvironment = new FormControl();

  constructor(private router: Router,
    private activatedRoute: ActivatedRoute,
    private toastr: ToastrService,
    private sidenavService: SidenavService) {
  }

  ngOnInit() {


    this.sidenavService.items$.pipe(
      map((items: SidenavItem[]) => this.sidenavService.sortRecursive(items, 'position'))
    ).subscribe((items) => {
      if (localStorage.getItem('domainSaved') && localStorage.getItem('envSaved') ){

        const domain = localStorage.getItem('domainSaved');
        const env = localStorage.getItem('envSaved');

        this.selectedEnvironment.setValue(domain + '-' + env);
        this.sidenavService.env = { name: env, domain: domain };

      }
      if (items.length > 0) {
        this.items = items;
        if (this.sidenavService.env) {
          this.selectedEnvironment.setValue(this.sidenavService.env.domain + '-' + this.sidenavService.env.name);
        } else {
          this.selectedEnvironment.setValue(items[0].domain + '-' + items[0].env);
          this.sidenavService.env = {name: items[0].env, domain: items[0].domain};
        }
      }
    });
  }

  getEnvironments(domain: String) {
    return new Set(this.items.filter(item => item.domain == domain).map(item => item.env));
  }

  getDomains() {
    return new Set(this.items.map(item => item.domain));
  }

  onSelectedEnvChanged(env, domain) {
    localStorage.setItem('domainSaved', domain);
    localStorage.setItem('envSaved', env);
    let currentEnv = this.sidenavService.env.name;
    this.sidenavService.env = { name: env, domain: domain };
    this.toastr.success("Switching to environment " + domain + "-" + env)
    this.toastr.info("Updating menu items...");
    // Redirect page to selected environment
    if(this.router.url.includes(`/${currentEnv}/`)) {
      this.toastr.info("Redirecting to " + this.router.url.replace(`/${currentEnv}/`, `/${env}/`));
      this.router.navigate(['/']);
    }
  }

  topology() {
    const env = this.sidenavService.env.name;
    const domain = this.sidenavService.env.domain;
    this.router.navigate([`/topology/${domain}/${env}`]);
  }

}
