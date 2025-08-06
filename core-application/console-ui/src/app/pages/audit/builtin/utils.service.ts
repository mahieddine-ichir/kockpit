import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UtilsService {

  /**
   * Parse headers in the form "[key: "value", key: "value" ... ]" to a key-value Map of <string, string>
   * @param headerAsString
   */
  static asMap(headerAsString: string): Map<string, Array<string>> {
    const headersArray = headerAsString
      .substring(1, headerAsString.length - 1) // remove starting '[' and ending ']'
      .split('",');


    headersArray.forEach((str, index) => {
      if (index !== headersArray.length - 1) {
        headersArray[index] = headersArray[index] + '"';
      }
    });

    const headersArrayFiltered = headersArray.filter(headers => headers.includes(':'));

    const map = headersArrayFiltered.map(value => {
      const kv = value.split(':\"');
        if (kv[0] !== '') {
          return {
            key: kv[0].trim(),
            value: new Array(kv[1].trim().substring(0, kv[1].length - 1)) // remove starting and ending '"'
          };
        } else {
          return {};
        }
    });

    return new Map(map.map(value => [value.key, value.value]));
  }
}

