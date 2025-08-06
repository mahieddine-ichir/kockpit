import {Component, OnInit, ViewChild} from '@angular/core';
import {DatePipe, formatDate} from '@angular/common';
import {Manifest} from './manifest.model';
import {MatPaginator} from '@angular/material/paginator';
import {MatDialog} from '@angular/material/dialog';
import {FormBuilder} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TopologyService} from '../topology/topology.service';
import {BreadCrumbService} from '../../kiss-components/breadcrumb/breadcrumb-service';
import {ManifestService} from './manifest.service';
import {MatTableDataSource} from '@angular/material/table';
import {MatSort} from '@angular/material/sort';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'wcc-manifest',
  templateUrl: './manifest.component.html',
  styleUrls: ['./manifest.component.scss'],
  providers: [DatePipe],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: 'auto'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class ManifestComponent implements OnInit {
  displayedColumns: string[] = ['lastModificationDate', 'name', 'source', 'actions'];
  displayedDetailColumns: string[] = ['domainEnv', 'groups', 'application', 'label', 'services'];
  manifestData = new MatTableDataSource<Manifest>([]);
  expandDetail: MatTableDataSource<Manifest>;
  fileToUpload: File[] = [];

  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  @ViewChild(MatSort, {static: true}) sort: MatSort;
  pageSize = 25;

  constructor(private dialog: MatDialog,
              private fb: FormBuilder,
              private router: Router,
              private topologyService: TopologyService,
              private manifestService: ManifestService,
              public breadCrumbService: BreadCrumbService,
              private route: ActivatedRoute,
              private toastr: ToastrService
  ) {
  }

  ngOnInit(): void {
    this.route.params.subscribe( params => {
      this.fetchData();
      this.initBreadCrumb();
    });
    this.manifestData.filterPredicate = (data, filter) => {
      filter = filter.toLowerCase().trim();
      return this.filterLastModificationDate(data, filter) ||
        this.filterData(data, filter);
    };
  }

  fetchData(): void {
    this.manifestService.getManifest().subscribe({
      next: (data) => {
        const dataGroupByName = data.reduce((result, dataName) => (result[dataName.name] ? result[dataName.name]
          .push(dataName) : result[dataName.name] = [dataName], result), {});
        const dataGroup = Object.keys(dataGroupByName)
          .map(manifest => ({
            name: manifest,
            apps: dataGroupByName[manifest],
            date: dataGroupByName[manifest][0].lastModificationDate,
            source: dataGroupByName[manifest][0].source
          }));
        this.manifestData.data = dataGroup;
        this.expandDetail = new MatTableDataSource<Manifest>(dataGroup);
        this.manifestData.paginator = this.paginator;
        this.manifestData.sort = this.sort;
        this.paginator.page.subscribe((event) => this.pageSize = event.pageSize);
      },
      error: (error) => {
        console.log(error);
      }
    });
  }

  handleFileInput(event) {
    for (const file of event.target.files) {
      this.fileToUpload.push(file);
    }
  }

  removeFile(index) {
    this.fileToUpload.splice(index, 1);
  }

  uploadFile() {
    if (this.fileToUpload !== null) {
      this.manifestService.postManifestFile(this.fileToUpload).subscribe(data => {
        this.toastr.success('File uploaded', 'File manifest');
        window.location.reload();
      }, error => {
        this.toastr.error(`File failed to upload, invalid manifest`, 'File manifest');
      });
    }
  }

  displayUploadManifest() {
    if ((window.location.href.indexOf('wcplatform-dev') > -1) || (window.location.href.indexOf('localhost') > -1)) {
      return true;
    } else {
      return false;
    }
  }

  onFilterChange(value) {
    if (!this.manifestData) {
      return;
    }
    value = value.trim();
    value = value.toLowerCase();
    this.manifestData.filter = value;
  }

  download(manifest: Manifest) {
    const jsonFile = this.jsonManifestConverter(manifest);
    const a = document.createElement('a');
    const blob = new Blob([jsonFile], {type: 'text/json'});
    const url = window.URL.createObjectURL(blob);

    a.href = url;
    a.download = manifest.name.valueOf();
    a.click();
    window.URL.revokeObjectURL(url);
    a.remove();
  }

  jsonManifestConverter(manifest: Manifest) {
    const copyManifest = JSON.parse(JSON.stringify(manifest));
    copyManifest.apps.forEach((data, index) => {
      copyManifest.apps[index] = {
        domain: data.domain,
        env: data.env,
        groups: data.groups,
        applications: [{id: data.id, label: data.label, subDomain: data.subDomain, services: data.services}]
      };
    });

    const jsonFile = JSON.stringify(copyManifest.apps, null, 2);
    return jsonFile.substring(1, jsonFile.length - 1);
  }

  filterData(data: Manifest, filter: string) {
    return JSON.stringify(data.apps, (key, value) => {
      if ( key === 'services' || key === 'lastModificationDate') {
        return undefined;
      }
      return value;
    }).toLowerCase().includes(filter);
  }

  filterLastModificationDate(data: Manifest, filter: string) {
    if (data.date !== null) {
      const formattedDate = formatDate(data.date, 'dd/MM/yy hh:mm:ss', 'fr');
      return formattedDate.toString().includes(filter);
    }
  }

  deleteManifestInMemory(manifest: Manifest) {
    this.manifestService.postDeleteManifestInMemory(manifest.name.valueOf()).subscribe(data => {
      this.toastr.success('Manifest file in memory deleted', 'File manifest');
      window.location.reload();
    }, error => {
      this.toastr.error(`Manifest file failed to delete`, 'File manifest');
    });
  }

  private initBreadCrumb() {
    this.breadCrumbService.emitChangeItem({
      title: `Manifests`,
      link: `/manifest`,
      icon: 'view_list',
      action: '1',
      data: {}
    });
  }
}
