import {Directive, Input, OnInit, TemplateRef, ViewContainerRef} from '@angular/core';
import {AuthenticationService} from '../../../services/authentication-service';


@Directive({selector: '[wccAuthorizations]'})
export class WccAuthorizationDirective implements OnInit {
  @Input('wccAuthorizations') authorizations: string;

  constructor(private templateRef: TemplateRef<any>,
              private viewContainer: ViewContainerRef,
              private authenticationService: AuthenticationService) {
  }

  ngOnInit():void {
    // no roles (null)
    if (!this.authorizations) {
      if (!this.authenticationService.isAuthenticated()) {
        this.viewContainer.clear();
      } else {
        this.viewContainer.createEmbeddedView(this.templateRef);
      }
      return;
    }

    if (!this.authenticationService.isAuthenticated()) {
      this.viewContainer.clear();
    } else if (this.authenticationService.hasRole(this.authorizations)) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      return;
    }
  }
}
