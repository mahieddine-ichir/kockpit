import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'frNumber'
})
export class LocaleNumberDirective implements PipeTransform {

  transform(val: number): string {
    if (val !== undefined && val !== null) {
      return val.toLocaleString('fr-FR');
    } else {
      return '';
    }
  }
}
