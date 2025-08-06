import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {BreadCrumbService} from '../../kiss-components/breadcrumb/breadcrumb-service';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {DynaconfigService} from './dynaconfig.service';
import {TopologyService} from '../topology/topology.service';
import {DatePipe} from '@angular/common';

import {
  CommandIssued,
  DialogData,
  DynaConfig,
  ExecutionModeEnum,
  PropertyChange,
  PropertyInstance
} from './dynaconfig.model';
import {Observable, of, withLatestFrom} from 'rxjs';
import {map, startWith} from 'rxjs/operators';
import {ConsoleConfigService} from "../../services/console-config.service";
import {ToastrService} from "ngx-toastr";

@Component({
  templateUrl: './dynaconfig.component.html',
  styleUrls: ['./dynaconfig.component.scss'],
  providers: [DatePipe]
/*
  encapsulation: ViewEncapsulation.None
*/
})

export class DynaconfigComponent implements OnInit {
  private applicationId: string;
  domain: string;
  env: string;

  formGroup: FormGroup;

  dynaconfig: DynaConfig;
  executionMode: ExecutionModeEnum;
  editable: boolean;
  options: string[];

  propertyChanges: PropertyChange[];
  propertyChanges$: Observable<PropertyChange[]>;
  filteredPropertyChanges$: Observable<PropertyChange[]>;

  issuedCommands: CommandIssued[];
  issuedCommands$: Observable<CommandIssued[]>;
  filteredIssuedCommands$: Observable<CommandIssued[]>;

  constructor(private dialog: MatDialog,
              private fb: FormBuilder,
              private router: Router,
              private topologyService: TopologyService,
              private dynaconfigService: DynaconfigService,
              public breadCrumbService: BreadCrumbService,
              private consoleConfigService: ConsoleConfigService,
              private route: ActivatedRoute,
              private formBuilder: FormBuilder,
              private datepipe: DatePipe,
              private toast: ToastrService
  ) {
    this.formGroup = formBuilder.group({filterPropertyChange: [''], filterIssuedCommands: ['']});
  }

  ngOnInit(): void {
    // Listen changes
    this.route.params.subscribe(params => {
      this.fetchRouteParams();
      this.fetchExecutionMode();
      this.fetchData();
      this.initBreadCrumb();
    });
  }

  ngAfterViewInit() {
  }

  ngOnDestroy() {
  }

  fetchRouteParams() {
    this.applicationId = this.route.snapshot.params['applicationId'];
    this.domain = this.route.snapshot.params['domain'];
    this.env = this.route.snapshot.params['env'];
  }

  fetchExecutionMode() {
    const dynaConfigSettingsMap = this.consoleConfigService.getConsoleConfigForService("dynaconfig")
      .config.dynaConfigSettingsMap;
    const taskName = `${this.domain}-${this.env}-${this.applicationId}`;
    this.executionMode = dynaConfigSettingsMap[taskName]?.executionMode;
  }

  fetchData(): void {
    this.dynaconfigService.getDynaConfig(this.domain, this.env, this.applicationId).subscribe({
      next: (data) => {
        this.dynaconfig = data;
        this.propertyChanges = this.dynaconfig.changes;

        // Quick fix to execute processing in backgroup
        setTimeout(() => {
          this.setPropertyChangesMessages();
          this.propertyChanges$ = this.getPropertyChanges();

          this.filteredPropertyChanges$ = this.formGroup.get('filterPropertyChange').valueChanges.pipe(
            startWith(''),
            withLatestFrom(this.propertyChanges$),
            map(([val, logs]) =>
              !val ? logs : logs.filter((x) => x.logMessage.toLowerCase().includes(val.toLowerCase()))
            )
          );

          this.issuedCommands = this.dynaconfig.issuedCommands;
          this.setIssuedCommandMessages();
          this.issuedCommands$ = this.getIssuedCommands();

          this.filteredIssuedCommands$ = this.formGroup.get('filterIssuedCommands').valueChanges.pipe(
            startWith(''),
            withLatestFrom(this.issuedCommands$),
            map(([val, logs]) =>
              !val ? logs : logs.filter((x) => x.logMessage.toLowerCase().includes(val.toLowerCase()))
            )
          );
        }, 100);

      },
      error: (error) => {
        this.toast.error(error, 'Error loading dynaconfig data');
      }
    });
  }

  flushDynaconfigHistory(): void {
    this.dynaconfigService.flushHistory(this.domain, this.env, this.applicationId).subscribe({
      next: () => {
        this.fetchData();
      },
      error: (error) => {
        this.toast.error(error, 'Error flushing dynaconfig history');
      }
    })
  }

  updateDynaConfig(): void {
    this.dynaconfigService.updateDynaConfig(this.domain, this.env, this.applicationId, this.dynaconfig).subscribe({
      next: (data) => {
        this.setEditable();
        this.fetchData();
      },
      error: (error) => {
        this.toast.error(error, 'Error updating dynaconfig values');
      }
    });
  }

  forceRefreshInstances(): void {
    this.dynaconfigService.forceReloadInstances(this.domain, this.env, this.applicationId).subscribe({
      next: (data) => {
        this.fetchData();
      },
      error: (error) => {
        this.toast.error(error, 'Error refreshing dynaconfig instance states');
      }
    });
  }

  openDialog(domain: String, env: String, applicationId: String, name: String, value: String, instances: PropertyInstance[]): void {
    const dialogRef = this.dialog.open(DynaconfigDialog, {
      data: {domain: domain, env: env, applicationId: applicationId, name: name, value: value, instances: instances}
    });

    dialogRef.afterClosed().subscribe(result => {
      this.fetchData();
    });
  }

  private initBreadCrumb() {
    const currentBreadCrumbItem = {
      title: `DynaConfig ${this.applicationId}`,
      link: `/services/dynaconfig/${this.domain}/${this.env}/${this.applicationId}`,
      icon: 'view_list',
      action: '1',
      data: {}
    };

    this.breadCrumbService.reset(currentBreadCrumbItem.title);
    this.breadCrumbService.emitChangeItem(currentBreadCrumbItem);
  }

  setEditable() {
    this.editable = !this.editable;
  }

  private getPropertyChanges() {
    return of(this.propertyChanges);
  }

  private getIssuedCommands() {
    return of(this.issuedCommands);
  }

  private setPropertyChangesMessages() {
    this.propertyChanges.forEach(
      log => {
        if (log.valueBeforeChange !== null && log.valueBeforeChange !== '') {
          log.logMessage = `${this.timeConverter(log.timestamp)} - ${log?.username ? `[${log.username}] -` : ''} Property {${log.propertyName}} change value from
           ${log.valueBeforeChange} to ${log.valueAfterChange}`;
        } else {
          log.logMessage = `${this.timeConverter(log.timestamp)} - ${log?.username ? `[${log.username}] -` : ''} Property {${log.propertyName}} change value to
           ${log.valueAfterChange}`;
        }
      }
    );
  }

  private setIssuedCommandMessages() {

    this.issuedCommands.forEach(
      log => {
        let result: string;
        result = this.timeConverter(log.timestamp) + '[' + log.type + ']' + '[' + log.requestId + ']' + ' - ';
        if (log.type === 'REQUEST') {
          if (log.applicationInstance !== null && log.applicationInstance !== '') {
            result = result + 'Sending request to ' + log.applicationInstance;
          } else {
            result = result + 'Broadcasting request ';
          }
          result = result + ' to update property ' + log.propertyName + ' value by ' + log.propertyValue + '. Status: ' + log.status;
        } else {
          result = result + log.applicationInstance + ' respond to request to update property ' + '{' + log.propertyName + '}: '
            + log.status + '.';
          if (log.status === 'ERROR') {
            result = result + ' Error: ' + log.message;
          }
        }
        log.logMessage = result;
      }
    );
  }

  private timeConverter(timestamp: number) {
    const a = new Date(timestamp);
    return '[' + this.datepipe.transform(a, 'dd/MM/yyyy HH:mm:ss') + ']';
  }

}

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'dynaconfig-dialog',
  templateUrl: './dynaconfig-dialog.html',
  styleUrls: ['./dynaconfig.component.scss'],
})
// tslint:disable-next-line:component-class-suffix
export class DynaconfigDialog {

  constructor(
    public dialogRef: MatDialogRef<DynaconfigDialog>,
    private dynaconfigService: DynaconfigService,
    private toast: ToastrService,
    @Inject(MAT_DIALOG_DATA) public data: DialogData) {
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  synchronize(): void {
    this.dynaconfigService.updateProperty(this.data.domain, this.data.env, this.data.applicationId, this.data.name, this.data.value)
      .subscribe({
        next: () => {
          this.dialogRef.close();
        },
        error: (error) => {
          this.dialogRef.close();
          this.toast.error(error, `Error updating dynaconfig property: ${this.data.name}`);
        }
      });
  }
}
