import {Component, Input, OnInit} from '@angular/core';
import {ToastrService} from "ngx-toastr";

@Component({
  selector: 'wcc-copy',
  templateUrl: './copy.component.html',
  styleUrls: ['./copy.component.scss']
})
export class CopyComponent implements OnInit {

  @Input()
  shouldCopy: boolean;

  @Input()
  value: any;

  constructor(private toastr: ToastrService,
  ) {
  }

  ngOnInit(): void {
  }

  copyMessage() {
    navigator.clipboard.writeText(this.value).then(() => {
      this.toastr.info(`Copied to clipboard`);
    }).catch(error => {
      console.error(error);
      this.toastr.error(`Failed to copy to clipboard`);
    });
  }
}
