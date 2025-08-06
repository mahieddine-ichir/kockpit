import { UserNotification } from './usernotification.model';
import {Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {FormBuilder} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TopologyService} from '../topology/topology.service';
import {BreadCrumbService} from '../../kiss-components/breadcrumb/breadcrumb-service';
import {DatePipe} from '@angular/common';
import {UsernotificationService} from './usernotification.service';
import {MatPaginator} from '@angular/material/paginator';
import {MatTableDataSource} from '@angular/material/table';

@Component({
  selector: 'wcc-usernotification',
  templateUrl: './usernotification.component.html',
  styleUrls: ['./usernotification.component.scss'],
  providers: [DatePipe]
})

export class UsernotificationComponent implements OnInit {

  displayedColumns: string[] = ['timestamp', 'level', 'domain', 'service', 'app', 'description'];
  usernotification: MatTableDataSource<UserNotification>;

  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  pageSize = 25;

  constructor(private dialog: MatDialog,
              private fb: FormBuilder,
              private router: Router,
              private topologyService: TopologyService,
              private usernotificationService: UsernotificationService,
              public breadCrumbService: BreadCrumbService,
              private route: ActivatedRoute,
  ) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.fetchData();
      this.initBreadCrumb();
    });
  }

  fetchData(): void {
    this.usernotificationService.getUserNotification().subscribe({
      next: (data) => {
        this.usernotification = new MatTableDataSource<UserNotification>(data);
        this.usernotification.paginator = this.paginator;
        this.paginator.page.subscribe((event) => this.pageSize = event.pageSize);

        localStorage.setItem('verifiedNotifDate', '' + new Date().getTime());
      },
      error: (error) => {
        console.log(error);
      }
    });
  }

  onFilterChange(value) {
    if (!this.usernotification) {
      return;
    }
    value = value.trim();
    value = value.toLowerCase();
    this.usernotification.filter = value;
  }

  private initBreadCrumb() {
    this.breadCrumbService.emitChangeItem({
      title: `Notifications`,
      link: `/notification`,
      icon: 'view_list',
      action: '1',
      data: {}
    });
  }

}
