import {Injectable} from '@angular/core';


@Injectable({
  providedIn: 'root',
})


export class FilterService {


  filterJson(obj: any, key: string) {
    let result = {};
    result = this.readData(obj, key);
    return result;
  }
  readData(obj: any, keyFilter: string) {
    const result = {};
    Object.keys(obj).forEach(key => {

      if (obj[key] != null) {

        if (key.toLowerCase().indexOf(keyFilter.toLowerCase()) > -1) {
          result[key] = obj[key];
          return;
        }

        if (typeof obj[key] == 'string' && obj[key]) {
          const val: string = obj[key];
          if (val.toLowerCase().indexOf(keyFilter.toLowerCase()) > -1) {
            result[key] = val;
          }
        }



        if (typeof obj[key] == 'number' && obj[key]) {
          const val: string = '' + obj[key];
          if (val.toLowerCase().indexOf(keyFilter) > -1) {
            result[key] = val;
          }
        }

        if (typeof obj[key] == 'object' && !Array.isArray(obj[key])) {
          let obje = {};
          obje = this.readData(obj[key], keyFilter);
          if (Object.keys(obje).length > 0) {
            result[key] = obje;
          }
        }

        if (typeof obj[key] == 'boolean') {
          const val: string = '' + obj[key];
          if (val.toLowerCase().indexOf(keyFilter.toLowerCase()) > -1) {
            result[key] = obj[key];
          }
        }

        if (Array.isArray(obj[key])) {
          const tab: any[] = obj[key];
          const arr = [];
          let obje = {};
          let i = 0;
          tab.forEach(val => {
            if (typeof val == 'object') {
              obje = this.readData(obj[key][i++], keyFilter);
            }

            if (typeof val == 'string' || typeof val == 'number') {
              val = '' + val;
              if (val.toLowerCase().indexOf(keyFilter.toLowerCase()) > -1) {
                obje = val;
              }
            }

            if (Object.keys(obje).length > 0) {
              arr.push(obje);
            }
          });

          if (arr.length > 0) {
            result[key] = arr;
          }

        }
      }
    });
    return result;
  }

}
