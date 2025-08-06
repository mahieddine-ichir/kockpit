import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'fury-toolbar-manifest',
  templateUrl: './toolbar-manifest.component.html',
  styleUrls: ['./toolbar-manifest.component.scss']
})
export class ToolbarManifestComponent implements OnInit {

  constructor(private router: Router,
              private activatedRouted: ActivatedRoute) {
  }

  ngOnInit() {
  }
}
