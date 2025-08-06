import {AfterViewInit, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {ListColumn} from "../../../core/common/list/list-column.model";
import {BreadCrumbService} from '../../../kiss-components/breadcrumb/breadcrumb-service';
import {fadeOutAnimation} from '../../../core/common/route.animation';
import {SelectionModel} from '@angular/cdk/collections';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {Message} from './../queues.model';
import {QueuesService} from './../queues.service';
import {ActivatedRoute, Router} from '@angular/router';
import {Queue} from '../../../pages/queue/queues.model';
import {ToastrService} from "ngx-toastr";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {ConsoleConfigService} from '../../../services/console-config.service';
import {ConfirmationDialog} from "../../../core/confirm-dialog/confirmation-dialog.component";

@Component({
  selector: 'fury-queue-messages-table',
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.scss'],
  animations: [fadeOutAnimation],
  host: {'[@fadeOutAnimation]': 'true'}
})
export class MessageComponent implements OnInit, OnDestroy, AfterViewInit {
  isLoading = true;
  dataSource: MatTableDataSource<Message>;
  columns: ListColumn[] = [];
  selectedRows = new SelectionModel<Message>(true, []);

  messageStatusCheckBox = [];

  nbMessagesByStatus: Map<string, number> = new Map();
  dynamoMessageCount: number = 0;

  selectedQueue: Queue;
  queueMessages: Message[] = [];

  applicationId: string;
  domain: string;
  env: string;
  queueName: string;

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild(MatSort, {static: true}) sort: MatSort;

  constructor(
    public dialog: MatDialog,
    public breadCrumbService: BreadCrumbService,
    private toastr: ToastrService,
    private route: ActivatedRoute,
    private queuesService: QueuesService,
    private consoleConfigService: ConsoleConfigService,
    private router: Router
  ) {
  }

  ngOnDestroy(): void {
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
    this.dataSource.sortingDataAccessor = (item, property) => {
      switch (property) {
        case 'receivedDate':
          return new Date(item.sentTimestamp);
        default:
          return item[property];
      }
    };
  }

  ngOnInit(): void {
    this.dataSource = new MatTableDataSource();
    this.dataSource.filterPredicate = (data: Message, filter: string) => JSON.stringify(data).toLowerCase().includes(filter);

    // Listen changes
    this.route.params.subscribe(params => {
      this.messageStatusCheckBox = this.queuesService.getMessageStatusEnum();
      this.fetchRouteParams();
      this.fetchTableColumns();
      this.fetchQueuesMessages();
    });
  }


  fetchRouteParams() {
    this.applicationId = this.route.snapshot.params['applicationId'];
    this.domain = this.route.snapshot.params['domain'];
    this.env = this.route.snapshot.params['env'];
    this.queueName = this.route.snapshot.params['queueName'];

  }

  /**
   * Handle messages table
   **/
  fetchQueuesMessages(refreshMessageCount: boolean = true): void {
    this.selectedRows.clear();
    this.isLoading = true;
    if (this.selectedQueue != null) {

      let selectedStatus = this.messageStatusCheckBox.filter(s => s.checked).map(s => s.name);

      this.queuesService.getMessages(this.domain, this.env, this.applicationId, this.selectedQueue.dlq, selectedStatus, null).subscribe({
        next: (data) => {
          this.dataSource.data = data.messages;
          this.nbMessagesByStatus = data.nbMessagesByStatus;
          this.isLoading = false;
          if (refreshMessageCount) {
            this.queuesService.getMessagesCount(this.domain, this.env, this.applicationId, this.selectedQueue.dlq).subscribe(
              {
                next: (info) => {
                  this.dynamoMessageCount = info.totalCount;
                },
                error: (error) => {
                  this.toastr.error('An error occurred while retrieving total message count from queue ' + this.selectedQueue.dlq);
                  console.log(error);
                }
              });
          }
        },
        error: (error) => {
          this.toastr.error('An error occurred while retrieving message(s) from queue ' + this.selectedQueue.dlq);
          console.log(error);
          this.isLoading = false;
        }
      });
    }
  }

  fetchTableColumns(): void {
    const consoleConfigForService = this.consoleConfigService.getConsoleConfigForService('sqsdlq');
    this.selectedQueue = consoleConfigForService.config.sqsDlqSettingsDtos.find(sqsDlqSettings => sqsDlqSettings.name == this.queueName);
    if (this.selectedQueue != null) {
      this.columns = this.selectedQueue.resultColumns.map(col => {
        const listColumn = new ListColumn();
        listColumn.name = col.label;
        listColumn.property = col.name;
        return listColumn;
      });
    }
  }

  retrieveDisplayedColumns(): string[] {
    let displayedColumns = this.columns.map(c => c.property);
    displayedColumns.unshift("comment");
    displayedColumns.unshift("status");
    displayedColumns.unshift("receivedDate");
    displayedColumns.unshift("select");
    displayedColumns.push("actions");
    return displayedColumns;
  }

  onFilterChange(event: String) {
    this.dataSource.filter = event.toLowerCase();
  }

  retrieveLabelStatus(status: string) {
    let label = this.messageStatusCheckBox.filter(s => s.name === status).map(s => s.label);
    if (label) {
      return label;
    } else {
      return status;
    }
  }

  retrievePropertyValue(element: Message, property: string) {
    // Get value from message keys
    if (element[property] != null) {
      return element[property];
    }
    // Get value from message payload
    const payloadValue = this.getValueFromMessagePayload(property, element.body);
    if (payloadValue != null) {
      return payloadValue;
    }
    // Get value from message attributes
    return this.getValueFromMessageAttributes(property, element);
  }

  getValueFromMessagePayload(property: string, payload: string) {
    let jsonMessage = this.messageContentAsJSON(payload)

    if (jsonMessage != null) {
      // is nested property key ?
      if (property.includes(".")) {
        let keys = property.split(".");
        return (jsonMessage[keys[0]] != null) ? jsonMessage[keys[0]][keys[1]] : null;
      } else {
        return jsonMessage[property];
      }
    } else {
      return null;
    }
  }

  getValueFromMessageAttributes(property: string, message: Message) {
    const attributesJsonArray = message.attributes;
    if (attributesJsonArray != null) {
      return attributesJsonArray.find( attribute => attribute.name.toLowerCase() === property.toLowerCase())?.value;
    } else {
      return null;
    }
  }


  messageContentAsJSON(message) {
    try {
      return JSON.parse(message);
    } catch (e) {
      return null;
    }
  }

  /**
   * Handle actions button
   **/

  openMessageDetail(message: Message) {
    this.router.navigate([message.id], {relativeTo: this.route});
  }

  /** */


  private openDialog(message: string, okLabel: string, cancelLabel: string, callbackFunction: any) {
    let config = {
      data: {
        message: message,
        buttonText: {
          ok: okLabel,
          cancel: cancelLabel
        }
      }
    }
    const ref = this.dialog.open(ConfirmationDialog, config);
    ref.afterClosed().subscribe(callbackFunction);
  }


  openDeleteConfirmationDialog() {
    this.openDialog('Are you sure you want to permanently remove ' + this.selectedRows.selected.length + ' message(s) from DLQ ' + this.selectedQueue.dlq + ' ?',
      'Yes, delete !',
      'No', (ok: boolean) => {
        if (ok) {
          this.deleteMessages(this.selectedRows.selected, this.selectedQueue);
        }
      });
  }

  openDeleteAllConfirmationDialog() {
    this.openDialog('Are you sure you want to permanently remove all the ' + this.dynamoMessageCount + ' message(s) from DLQ ?',
      'Yes, delete !', 'No', (deleteConfirmed: boolean) => {
        if (deleteConfirmed) {
          this.deleteAllMessages(this.selectedQueue);
        }
      });
  }

  openReplayConfirmationDialog() {
    this.openDialog('Are you sure you want to send ' + this.selectedRows.selected.length + ' message(s) to SQS queue ' + this.selectedQueue.name + ' ?',
      'Yes !', 'No', (sendConfirmed: boolean) => {
        if (sendConfirmed) {
          this.sendMessages(this.selectedRows.selected);
        }
      });
  }

  openReplayAllConfirmationDialog() {
    this.openDialog('Are you sure you want to send All ' + this.dynamoMessageCount + ' message(s) to SQS queue ' + this.selectedQueue.name + ' ?',
      'Yes !',
      'No', (sendConfirmed: boolean) => {
        if (sendConfirmed) {
          this.sendAllMessages();
        }
      });
  }

  deleteAllMessages(queue: Queue) {
    this.queuesService.deleteAllMessages(this.domain, this.env, this.applicationId, this.selectedQueue.dlq).subscribe(() => {
      this.fetchQueuesMessages();
      this.toastr.success('All message(s) have been deleted successfully in queue ' + queue.name);
    }, error => {
      this.toastr.error('An error occurred while deleting all message(s) in queue ' + queue.name);
      console.log(error);
    });
  }

  deleteMessages(messages: Message[], queue: Queue) {
    const messageIds = messages.map((message) => message.id);
    this.queuesService.deleteMessages(this.domain, this.env, this.applicationId, this.selectedQueue.dlq, messageIds).subscribe(() => {
      this.fetchQueuesMessages();
      this.toastr.success(messageIds.length + ' message(s) have been deleted successfully in queue ' + queue.name);
    }, error => {
      this.toastr.error('An error occurred while deleting ' + messageIds.length + ' message(s) in queue ' + queue.name);
      console.log(error);
    });
  }


  sendMessages(messages: Message[]) {
    const retries = {retries: []};

    messages.forEach(m => {
      retries.retries.push({
        parentId: m.id,
        retry: {
          groupId: m.groupId,
          body: m.body,
          attributes: m.attributes,
          sentTimestamp: new Date().getTime(),
          receiveTime: null,
          status: 'Sent'
        }
      })
    })

    this.queuesService.retryMessages(this.domain, this.env, this.applicationId, this.selectedQueue.dlq, retries).subscribe(({
      next: () => {
        this.toastr.success(`${retries.retries.length} message(s) have been sent successfully to queue ${this.queueName}`);
        this.fetchQueuesMessages();
      },
      error: (error) => {
        this.toastr.error(`An error occurred while sending ${retries.retries.length} message(s) to queue ${this.queueName}`);
        console.log(error);
      }
    }));
  }


  sendAllMessages() {
    this.queuesService.retryAllMessages(this.domain, this.env, this.applicationId, this.selectedQueue.dlq).subscribe(({
      next: () => {
        this.toastr.success(`All message(s) have been sent successfully to queue ${this.queueName}`);
        this.fetchQueuesMessages();
      },
      error: (error) => {
        this.toastr.error(`An error occurred while sending all message(s) to queue ${this.queueName}`);
        console.log(error);
      }
    }));
  }

  /**
   * Handle rows selection checkbox
   **/
  getNumberOfSelectedItems(): number {
    return this.selectedRows.selected.length;
  }

  isAllSelected() {
    const numRows = this.dataSource.filteredData.length;
    return this.getNumberOfSelectedItems() === numRows;
  }

  masterToggle() {
    this.isAllSelected() ?
      this.selectedRows.clear() :
      this.dataSource.filteredData.forEach(row => this.selectedRows.select(row));
  }


}
