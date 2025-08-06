import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { LIST_FADE_ANIMATION } from '../../common/list.animation';
import {UsernotificationService} from '../../../pages/usernotification/usernotification.service';
import {ActivatedRoute} from '@angular/router';
import {interval} from 'rxjs';

@Component({
  selector: 'fury-toolbar-notifications',
  templateUrl: './toolbar-notifications.component.html',
  styleUrls: ['./toolbar-notifications.component.scss'],
  animations: [...LIST_FADE_ANIMATION],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ToolbarNotificationsComponent implements OnInit {
  secondsCounter = interval(10000);
  nbNewNotif: number;

  constructor(
    private usernotificationService: UsernotificationService,
    private route: ActivatedRoute, private cdr: ChangeDetectorRef
  ) {
  }

  ngOnInit() {
    if (localStorage.getItem('verifiedNotifDate') == null) {
      localStorage.setItem('verifiedNotifDate', '' + -1);
    }
    this.fetchData();
    this.fetchDataEvery30sec();
  }

  fetchDataEvery30sec() {
    this.secondsCounter.subscribe(() => {
        this.usernotificationService.getNumberOfNewNotification(Number(localStorage.getItem('verifiedNotifDate'))).subscribe({
          next: (data) => {
            this.nbNewNotif = data;
            this.cdr.detectChanges();
          },
          error: (error) => {
            console.log(error);
          }
        });
      }
    );
  }

  fetchData(): void {
    this.usernotificationService.getNumberOfNewNotification(Number(localStorage.getItem('verifiedNotifDate'))).subscribe({
      next: (data) => {
        this.nbNewNotif = data;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.log(error);
      }
    });
  }

  isVisible() {
    if (this.nbNewNotif === null || this.nbNewNotif <= 0 || this.nbNewNotif === undefined) {
      return false;
    }
    return true;
  }

  onClick() {
    this.nbNewNotif = 0;
    localStorage.setItem('verifiedNotifDate', '' + new Date().getTime());
  }
}
