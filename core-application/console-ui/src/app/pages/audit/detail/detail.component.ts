import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {RequestService} from '../request.service';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Execution} from '../request.model';
import {BreadCrumbService} from '../../../kiss-components/breadcrumb/breadcrumb-service';
import {HttpExchange} from '../builtin/http-exchange/http.exchange';
import {Builtin} from '../builtin/builtin.model';
import {SqsMessage} from '../builtin/sqs-message/sqs-message';
import {KinesisMessage} from '../builtin/kinesis-message/kinesis-message';
import {KafkaMessage} from '../builtin/kafka-message/kafka-message';
import {Subject} from 'rxjs';
import {HttpEventType} from '@angular/common/http';
import {saveAs} from 'file-saver';
import {MatTabChangeEvent} from '@angular/material/tabs';

@Component({
  selector: 'audit-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class DetailComponent implements OnInit {
  loading = false;

  detail: any = {};
  executions: Execution[];
  focusFlowExecutionsTab: Subject<void> = new Subject<void>();

  keyValueExtensions: any[];
  httpExchangeAuditsExtension: HttpExchange[];
  sqsServiceAuditsExtension: SqsMessage[];
  kinesisServiceAuditsExtension: KinesisMessage[];
  httpRequestResponseContentExtension: Builtin;
  kafkaAuditsExtension: KafkaMessage[];

  isSqsAuditPresent: boolean;
  isKinesisAuditPresent: boolean;
  isHttpExchangeAuditPresent: boolean;
  isKafkaAuditPresent: boolean;
  isFlowExecutionsAuditPresent: boolean;
  isRequestResponsePresent: boolean;

  maxReportSize = 5; // 5ko
  reportTooBig = false;
  selectedIndex = 0;
  indexTextLabel = 'Details';

  private domain: string;
  private env: string;
  private auditViewName: string;

  constructor(private router: Router,
              private activatedRoute: ActivatedRoute,
              private toastr: ToastrService,
              private requestService: RequestService,
              private _breadCrumbService: BreadCrumbService) {
  }

  public ngOnInit() {
    this.loading = true;
    this.auditViewName = this.activatedRoute.snapshot.params['auditViewName'] || 'all';
    this.domain = this.activatedRoute.snapshot.params['domain'];
    this.env = this.activatedRoute.snapshot.params['env'];

    this.activatedRoute.params.subscribe(params => {
      let id = params['id'];
      if (id.indexOf('%2F') > -1) {
        id = id.replace(/%2F/g, '/');
      }
      this._breadCrumbService.emitChangeItem({
        title: `Details ${id}`,
        link: `/audit/${this.auditViewName}/detail/${encodeURIComponent(id)}`,
        icon: 'view_list',
        action: '1',
        data: {}
      });

      this.requestService.auditById(this.domain, this.env, this.auditViewName, id).subscribe(event => {
        switch (event.type) {
          case HttpEventType.DownloadProgress:
            const reportSize = event.loaded / 8 / 1000; //bytes -> octets -> ko
            if (reportSize > this.maxReportSize) {
              this.reportTooBig = true;
            }
            break;

          case HttpEventType.Response:
            this.detail = event.body;

            this.loadKEngine();
            this.loadKeyValueExtensionValue();
            this.loadExchanges();
            this.loadHttpRequestResponseExtension();
            this.loadSQSAuditsExtension();
            this.loadKinesisAuditsExtension();
            this.loadkafkaAuditsExtension();
        }
      }, error => {
        this.toastr.error('Error loading request details for id: ' + id);
        this.router.navigate(['/audit']);
      }, () => {
        this.loading = false;
      });
    });
  }

  onTabChanged(event: MatTabChangeEvent) {
    this.indexTextLabel = event.tab.textLabel;
    this.selectedIndex = event.index;
  }

  loadHttpRequestResponseExtension(): void {
    if (!this.detail) {
      return;
    }
    this.httpRequestResponseContentExtension = this.detail.audits
      .filter(audit => audit && audit.type === 'builtin.web')
      .flatMap(audit => audit.events)[0];
    this.isRequestResponsePresent =
      (this.httpRequestResponseContentExtension && this.httpRequestResponseContentExtension.httpAuditedRequest) ? true : false;
  }

  loadKeyValueExtensionValue(): void {
    if (!this.detail) {
      return;
    }
    this.keyValueExtensions = this.detail.indexedKeyValues;
  }

  private loadKEngine() {
    if (!this.detail) {
      return;
    }
    this.executions = this.detail.audits
      .filter(ext => ext && ext.type === 'kengine.flows')
      .flatMap(audit => audit.events)
      .sort((a, b) => a.position - b.position)
      .map(ext => {
        ext.executionEDTDTO.executionName = ext.executionName;
        ext.executionEDTDTO.startTime = ext.startTime;
        ext.executionEDTDTO.endTime = ext.endTime;
        return ext.executionEDTDTO;
      });
    this.isFlowExecutionsAuditPresent = this.executions.length > 0;
  }

  private loadExchanges() {
    if (!this.detail) {
      return;
    }
    this.loadHttpExchangeAuditsExtension();
  }

  get breadCrumbService(): BreadCrumbService {
    return this._breadCrumbService;
  }

  refresh(e) {
    // Only for flow executions tab
    if (e !== 3) {
      return;
    }
    this.focusFlowExecutionsTab.next();
  }

  private loadkafkaAuditsExtension() {
    if (!this.detail) {
      return;
    }
    this.kafkaAuditsExtension = this.detail.audits
      .filter(ext => ext && ext.type === 'builtin.kafka')
      .flatMap(ext => ext.events);
    this.isKafkaAuditPresent = this.kafkaAuditsExtension.length > 0;
  }

  private loadKinesisAuditsExtension() {
    this.kinesisServiceAuditsExtension = this.detail.audits
      .filter(ext => ext && ext.type === 'builtin.kinesis-service')
      .flatMap(ext => ext.events);
    this.isKinesisAuditPresent = this.kinesisServiceAuditsExtension.length > 0;
  }

  private loadSQSAuditsExtension() {
    this.sqsServiceAuditsExtension = this.detail.audits
      .filter(ext => ext && ext.type === 'builtin.sqs-service')
      .flatMap(ext => ext.events);
    this.isSqsAuditPresent = this.sqsServiceAuditsExtension.length > 0;
  }

  private loadHttpExchangeAuditsExtension() {
    this.httpExchangeAuditsExtension = this.detail.audits
      .filter(ext => ext && ext.type === 'builtin.httpexchanges')
      .flatMap(ext => ext.events);
    this.isHttpExchangeAuditPresent = this.httpExchangeAuditsExtension.length > 0;
  }

  downloadJsonReport() {
    const jsonData = this.detail;
    const downloadOptions = {type: 'JSON'};
    const fileName = 'download_report_' + this.auditViewName + '.json';
    return saveAs(
      new Blob([JSON.stringify(jsonData, null, 2)], downloadOptions), fileName
    );
  }
}
