export interface HttpExchange {
    startTime: Date;
    endTime: Date;
    httpAuditedRequest: HttpRequest;
    httpAuditedResponse: HttpResponse;
    panelOpenState: boolean;
}

export interface HttpRequest {
    headers: any;
    method: string;
    body: string;
    uri: string;
    params: any;
}

export interface HttpResponse {
    headers: any;
    payload: string;
    status: number;
    body: string;
}
