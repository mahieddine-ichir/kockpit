import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {fadeOutAnimation} from '../../../core/common/route.animation';
import {AuthenticationService} from '../../../services/authentication-service';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'fury-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  host: {
    '[@fadeOutAnimation]': 'true'
  },
  animations: [fadeOutAnimation]
})
export class LoginComponent implements OnInit {

  visible = false;
  loading: boolean;
  error: string;

  constructor(private router: Router,
              private route: ActivatedRoute,
              private fb: FormBuilder,
              private cd: ChangeDetectorRef,
              private authenticationService: AuthenticationService,
              private toastr: ToastrService
  ) { }

  ngOnInit() {
    if (!this.authenticationService.isAuthenticated()) {
      LoginComponent.forceReloadPageForLogin();
      return;
    }
    this.authenticationService.getLoggedUser()
      .subscribe({next: (v) => {
        this.router.navigate(['']);
      },
        error: (e) => {
        this.toastr.error(`Error getting user information: ${e}`, 'Not logged');
          LoginComponent.forceReloadPageForLogin();
        }});
  }

  private static forceReloadPageForLogin() {
    // Force reload full page (to activate login feature)
    window.location.href = `index.html?t=${Math.random()}`;
  }
}
