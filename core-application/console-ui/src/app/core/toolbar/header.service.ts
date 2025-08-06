import {Injectable} from '@angular/core';

@Injectable()
export class HeaderService {
  public _displayProject:boolean=false;
  public _displayVersion:boolean=false;

  public get displayProject(): boolean {
    return this._displayProject;
  }

  public set displayProject(display: boolean) {
    this._displayProject = display;
  }

  public get displayVersion(): boolean {
    return this._displayVersion;
  }

  public  set displayVersion(display: boolean) {
    this._displayVersion = display;
  }

}
