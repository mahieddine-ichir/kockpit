import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { fadeOutAnimation } from '../../../core/common/route.animation';
import {AuthenticationService} from '../../../services/authentication-service';

@Component({
  selector: 'fury-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.scss'],
  host: {
    '[@fadeOutAnimation]': 'true'
  },
  animations: [fadeOutAnimation]
})
export class LogoutComponent implements OnInit {

  constructor(private router: Router,
              private authenticationService: AuthenticationService
  ) { }

  ngOnInit() {
    this.authenticationService.logout();
  }

  login(e){
  /* Call /logout (intercept by terraform-aws-authentification-lambda-at-edge) to refresh cognito context
     and then to be redirect to home page (force re authentication since we have no token) == LOGIN*/
    window.location.href = `logout?t=${Math.random()}`;
    // Skip other actions ...
    e.preventDefault();
  }

}
