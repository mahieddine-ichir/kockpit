import {Component, OnInit} from '@angular/core';
import {Attribute, Message, MessageRetry, Queue} from '../queues.model';
import {ActivatedRoute, Router} from '@angular/router';
import {QueuesService} from '../queues.service';
import {MatDialog} from '@angular/material/dialog';
import {ToastrService} from 'ngx-toastr';
import {ConsoleConfigService} from '../../../services/console-config.service';
import {ConfirmationDialog} from '../../../core/confirm-dialog/confirmation-dialog.component';
import {BreadCrumbService} from '../../../kiss-components/breadcrumb/breadcrumb-service';

@Component({
  templateUrl: 'detail.component.html',
  styleUrls: ['detail.component.scss']
})
export class DetailComponent implements OnInit {
  isLoading = true;
  retryEnable = false;
  message: Message;
  messageRetry: MessageRetry;
  messageToSend: Message;
  applicationId: string;
  domain: string;
  env: string;
  queueName: string;
  id: string;
  messageStatusEnum = this.queuesService.getMessageStatusEnum();
  attributeTypeMap: Map<string, string> = this.queuesService.getAttributeTypeMap();
  selectedQueue: Queue;
  newAttribute: Attribute = {name: '', type: 'STRING', value: ''};
  headElements = ['Sent date', 'Error receive date', 'Group Id', 'Status', 'Details'];

  items = ['Item 1', 'Item 2', 'Item 3', 'Item 4', 'Item 5'];
  expandedIndex = 0;
  parentIdAttribute = 'WCP_RETRY_MESSAGE_PARENT_ID';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private queuesService: QueuesService,
    private dialog: MatDialog,
    private toastr: ToastrService,
    private consoleConfigService: ConsoleConfigService,
    public breadCrumbService: BreadCrumbService) {
  }

  ngOnInit(): void {
    this.fetchRouteParams();
    this.selectedQueue = this.consoleConfigService.getConsoleConfigForService('sqsdlq')
      .config.sqsDlqSettingsDtos.find(sqsDlqSettings => sqsDlqSettings.name == this.queueName);
    this.initBreadCrumb();
    this.fetchMessage();

  }

  fetchRouteParams() {
    this.applicationId = this.route.snapshot.params['applicationId'];
    this.domain = this.route.snapshot.params['domain'];
    this.env = this.route.snapshot.params['env'];
    this.queueName = this.route.snapshot.params['queueName'];
    this.id = this.route.snapshot.params['id'];
  }

  addAttribute() {
    if (!this.messageRetry.attributes) {
      this.messageRetry.attributes = [];
    }

    this.messageRetry.attributes.push({
      name: this.newAttribute.name,
      value: this.newAttribute.value,
      type: this.newAttribute.type
    });

    this.newAttribute = {name: '', type: 'STRING', value: ''};
  }

  deleteAttribute(name) {
    this.messageRetry.attributes.forEach((item, index) => {
      if (item.name === name) {
        this.messageRetry.attributes.splice(index, 1);
      }
    });
  }

  fetchMessage(): void {
    this.isLoading = true;
    this.queuesService.getMessage(this.domain, this.env, this.applicationId, this.selectedQueue.dlq, this.id).subscribe({
      next: (data) => {
        this.handleGetMessageResponse(data);
      },
      error: (error) => {
        this.toastr.error('An error occurred while retrieving message with id ' + this.id + ' from queue ' + this.queueName);
        console.log(error);
      }
    });
  }

  updateMessage() {
    this.queuesService.updateMessage(this.domain, this.env, this.applicationId, this.selectedQueue.dlq, this.id, this.message).subscribe(({
      next: () => {
        console.log('ici');
        this.toastr.success(`Message have been save successfully to queue ${this.queueName}`);
      },
      error: (error) => {
        this.toastr.error(`An error occurred while saving message to queue ${this.queueName}`);
        console.log(error);
      }
    }));
  }

  openSendConfirmationDialog() {
    const ref = this.dialog.open(ConfirmationDialog, {
      data: {
        message: 'Are you sure you want to send this message to SQS queue ' + this.queueName + ' ?',
        buttonText: {
          ok: 'Yes',
          cancel: 'No'
        }
      }
    });

    ref.afterClosed().subscribe((sendConfirmed: boolean) => {
      if (sendConfirmed) {
        this.retryEnable = false;
        this.retry();
      }
    });
  }

  retry() {
    this.messageRetry.status = "Sent";
    this.messageRetry.sentTimestamp = new Date().getTime();
    const retries = {retries: [{parentId: this.id, retry: this.messageRetry}]};

    this.queuesService.retryMessages(this.domain, this.env, this.applicationId, this.selectedQueue.dlq, retries).subscribe(({
      next: (data) => {
        this.toastr.success(`Message have been sent successfully to queue ${this.queueName}`);
        if (this.selectedQueue.deleteWhenReplay) {
         this.router.navigate(['services/sqsdlq/'+this.domain+'/'+this.env+'/'+this.applicationId+'/'+this.queueName]);
        } else {
          this.message.retries.push(data[0])
        }

      },
      error: (error) => {
        this.toastr.error(`An error occurred while sending message to queue ${this.queueName}`);
        this.fetchMessage();
      }
    }));
  }

  handleGetMessageResponse(result: Message): void {
    this.isLoading = false;
    this.message = result;

    const myClonedArray = [];
    this.message.attributes.forEach(val => myClonedArray.push(Object.assign({}, val)));
    this.messageRetry = {
      groupId: this.message.groupId,
      attributes: myClonedArray,
      sentTimestamp: null,
      receiveTime: null,
      body: this.message.body,
      status: ""
    };

    // Try to beautify body (if json)
    try {
      this.message.body = JSON.stringify(JSON.parse(result.body), null, 2);
    } catch (e) {
      console.log('Body is not in JSON format');
    }
  }

  private initBreadCrumb() {
    const currentBreadCrumbItem = {
      title: `Queue ${this.queueName}`,
      link: `/services/sqsdlq/${this.domain}/${this.env}/${this.applicationId}/${this.queueName}`,
      icon: 'view_list',
      action: '1',
      data: {}
    };

    this.breadCrumbService.reset(currentBreadCrumbItem.title);
    this.breadCrumbService.emitChangeItem(currentBreadCrumbItem);
  }


}

