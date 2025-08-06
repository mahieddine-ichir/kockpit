import { Injectable } from '@angular/core';
import { saveAs } from 'file-saver';

@Injectable({
  providedIn: 'root'
})
export class ExportCsvService {

  constructor() {}

  formatDate(timestamp) {
    var d = new Date(timestamp);
    return d.getUTCDate() + '/' + d.getMonth() + 1 + '/' + d.getFullYear() + ' ' + d.getHours() + ':' + d.getMinutes() + ':' + d.getSeconds();
  }

  exportCsv(filename, allDesync, ...extraRows) {

    const rows = [["pmid", "timestamp" , "globalMessage", ...extraRows]];

    let csvContent = "";
    allDesync.forEach(data => rows.push([data.pmid, this.formatDate(data.timestamp), data.globalMessage, ...extraRows.map(x => data[x])]))
    csvContent = rows.map(row => row.join(',') + '\r\n').join('');

    var blob = new Blob([csvContent], {type: 'text/csv;charset=utf-8;' })
    saveAs(blob, filename);

  }
}
