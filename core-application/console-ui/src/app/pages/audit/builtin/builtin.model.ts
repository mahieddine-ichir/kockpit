export interface Builtin {
    type: string;
    httpAuditedRequest: HttpRequest;
    httpAuditedResponse: HttpResponse;
}

export interface HttpRequest {
    headers: Map<string,string>;
    method: string;
    body: string;
    uri: string;
}

export interface HttpResponse {
    headers: Map<string,string>;
    body: string;
    status: number;
}
