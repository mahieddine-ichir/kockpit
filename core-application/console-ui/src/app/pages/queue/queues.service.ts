import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../../environments/environment";
import {Count, Message, MessageRetry, Messages, SendMessagesRetry} from "./queues.model";

@Injectable({
  providedIn: 'root'
})
export class QueuesService {

  constructor(private http: HttpClient) {
  }

  getMessages(domain: string, env: string, applicationId: string, queueName: string, status: string[],lastsortkey:string): Observable<Messages> {
    let parameter = new HttpParams();
    status.forEach(s => parameter = parameter.append('status', s))
    if(lastsortkey != null){
      parameter = parameter.append('lastSortKey',lastsortkey)
    }
        return this.http.get<Messages>(`${environment.backend}/api/services/${domain}/${env}/sqsdlq/${applicationId}/${queueName}/messages`,
      {params: parameter});
  }


  getMessagesCount(domain: string, env: string, applicationId: string, queueName: string): Observable<Count> {
    let parameter = new HttpParams();
    return this.http.get<Count>(`${environment.backend}/api/services/${domain}/${env}/sqsdlq/${applicationId}/${queueName}/count`,
      {params: parameter});

  }



  deleteMessages(domain: string, env: string, applicationId: string, queueName: string, messageIds: string[]): Observable<any> {
    if (!messageIds || messageIds.length <= 0) {
      return new Observable();
    }
    return this.http.delete(`${environment.backend}/api/services/${domain}/${env}/sqsdlq/${applicationId}/${queueName}/messages`, {body: messageIds})

  }

  deleteAllMessages(domain: string, env: string, applicationId: string, queueName: string): Observable<any> {
    return this.http.delete(`${environment.backend}/api/services/${domain}/${env}/sqsdlq/${applicationId}/${queueName}/messages`)
  }


  getMessage(domain: string, env: string, applicationId: string, queueName: string, id: string): Observable<Message> {
    return this.http.get<Message>(`${environment.backend}/api/services/${domain}/${env}/sqsdlq/${applicationId}/${queueName}/${id}/message`);
  }

  updateMessage(domain: string, env: string, applicationId: string, queueName: string, id: string, message: Message): Observable<any> {
    return this.http.put(`${environment.backend}/api/services/${domain}/${env}/sqsdlq/${applicationId}/${queueName}/${id}/message`, message);
  }

  retryMessages(domain: string, env: string, applicationId: string, queueName: string, retries: SendMessagesRetry) : Observable<MessageRetry[]>{
    if (!retries || retries.retries.length <= 0) {
      return new Observable();
    }
    return this.http.post<MessageRetry[]>(`${environment.backend}/api/services/${domain}/${env}/sqsdlq/${applicationId}/${queueName}/retries`, retries);
  }

  retryAllMessages(domain: string, env: string, applicationId: string, queueName: string) {
    return this.http.post(`${environment.backend}/api/services/${domain}/${env}/sqsdlq/${applicationId}/${queueName}/retries`, null);
  }

  getMessageStatusEnum() {
    return [
      {name: 'NEW', checked: true, label: "New"},
      {name: 'ANALYSIS_ONGOING', checked: true, label: "Analysis ongoing"},
      {name: 'RESOLVED', checked: false, label: "Resolved"}
    ]
  }

  getAttributeTypeMap(): Map<string, string> {
    return new Map([
      ["STRING", "String"],
      ["NUMBER", "Number"]
    ]);
  }

}
