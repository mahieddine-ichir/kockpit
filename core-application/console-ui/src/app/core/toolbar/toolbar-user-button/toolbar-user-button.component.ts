import {Component, OnInit} from '@angular/core';
import {AuthenticationService, LoggedUser} from '../../../services/authentication-service';
import { Router } from '@angular/router';

@Component({
  selector: 'fury-toolbar-user-button',
  templateUrl: './toolbar-user-button.component.html',
  styleUrls: ['./toolbar-user-button.component.scss']
})
export class ToolbarUserButtonComponent implements OnInit {

  isOpen: boolean;
  user = new LoggedUser();

  constructor(private authenticationService: AuthenticationService,
              private router: Router) { }

  ngOnInit() {
    this.authenticationService.getLoggedUser().subscribe(value => {
      this.user = value;
    });
  }

  toggleDropdown() {
    this.isOpen = !this.isOpen;
  }

  onClickOutside() {
    this.isOpen = false;
  }

  logout() {
    this.router.navigate(['/logout']);
  }

}
