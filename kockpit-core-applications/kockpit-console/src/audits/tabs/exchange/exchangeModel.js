// Reading the builtin.web audit: one event per HTTP exchange the request went through

export const parseExchanges = (request) => {
    const web = request.audits?.find(audit => audit.type === 'builtin.web');
    const events = Array.isArray(web?.events) ? web.events : web?.events ? [web.events] : [];
    return events.map((event, index) => {
        const data = typeof event === 'string' ? JSON.parse(event) : event;
        return {
            key: `x${index}`,
            request: data.httpAuditedRequest || {},
            response: data.httpAuditedResponse || null,
            startTime: data.startTime,
            endTime: data.endTime,
            duration: data.startTime != null && data.endTime != null ? data.endTime - data.startTime : null,
            traceId: data.traceId,
            traceIdHeaderName: data.traceIdHeaderName,
            origin: data.origin,
        };
    });
};

// The outgoing calls (builtin.httpexchanges): one event per call the service made, in start order
export const parseOutgoingCalls = (request) =>
    (request.audits || [])
        .filter(audit => audit.type === 'builtin.httpexchanges')
        .flatMap(audit => Array.isArray(audit.events) ? audit.events : audit.events ? [audit.events] : [])
        .map(event => (typeof event === 'string' ? JSON.parse(event) : event))
        .map((data, index) => ({
            key: `c${index}`,
            request: data.httpAuditedRequest || {},
            response: data.httpAuditedResponse || null,
            startTime: data.startTime,
            duration: data.startTime != null && data.endTime != null ? data.endTime - data.startTime : null,
        }))
        .sort((a, b) => (a.startTime ?? Infinity) - (b.startTime ?? Infinity) || 0);

// "https://api.acme.com/v1/x?y=1" -> { host: "api.acme.com", path: "/v1/x?y=1" }; relative URIs have no host
export const splitUri = (uri) => {
    try {
        const url = new URL(uri);
        return { host: url.host, path: `${url.pathname}${url.search}` };
    } catch {
        return { host: null, path: uri || '/' };
    }
};

// A call failed when it got no response, or a 4xx/5xx one
export const callFailed = (call) => !call.response || !call.response.status || Number(call.response.status) >= 400;

// Headers are { name: [values] } (or a JSON string of it): flattened to [name, value] pairs
export const headerEntries = (headers) => {
    if (!headers) return [];
    const parsed = typeof headers === 'string' ? JSON.parse(headers) : headers;
    return Object.entries(parsed).map(([name, value]) => [name, Array.isArray(value) ? value.join(', ') : String(value ?? '')]);
};

export const headerValue = (headers, name) =>
    headerEntries(headers).find(([key]) => key.toLowerCase() === name.toLowerCase())?.[1];

// Values hidden until asked for: they are credentials more often than not
const SENSITIVE_HEADERS = ['authorization', 'proxy-authorization', 'cookie', 'set-cookie', 'x-api-key'];
export const isSensitiveHeader = (name) => SENSITIVE_HEADERS.includes(name.toLowerCase());

const REASONS = {
    200: 'OK', 201: 'Created', 202: 'Accepted', 204: 'No Content',
    301: 'Moved Permanently', 302: 'Found', 304: 'Not Modified', 307: 'Temporary Redirect', 308: 'Permanent Redirect',
    400: 'Bad Request', 401: 'Unauthorized', 403: 'Forbidden', 404: 'Not Found', 405: 'Method Not Allowed',
    409: 'Conflict', 412: 'Precondition Failed', 415: 'Unsupported Media Type', 422: 'Unprocessable Entity', 429: 'Too Many Requests',
    500: 'Internal Server Error', 501: 'Not Implemented', 502: 'Bad Gateway', 503: 'Service Unavailable', 504: 'Gateway Timeout',
};

// The status family, named - the views pair its color with this label and an icon
export const statusClass = (status) => {
    const code = Number(status);
    if (!code) return { family: 'unknown', label: 'No status' };
    const reason = REASONS[code];
    if (code < 300) return { family: 'success', label: reason || 'Success' };
    if (code < 400) return { family: 'redirect', label: reason || 'Redirect' };
    if (code < 500) return { family: 'client', label: reason || 'Client error' };
    return { family: 'server', label: reason || 'Server error' };
};

// What the body is, for its badge and for choosing how to show it
export const describeBody = (body, contentType) => {
    if (body == null || body === '') return { kind: 'empty', text: '' };
    if (typeof body !== 'string') return { kind: 'json', text: JSON.stringify(body, null, 2) };
    const trimmed = body.trim();
    if (/json/i.test(contentType || '') || trimmed.startsWith('{') || trimmed.startsWith('[')) {
        try {
            return { kind: 'json', text: JSON.stringify(JSON.parse(trimmed), null, 2) };
        } catch {
            // not valid JSON after all: shown as text
        }
    }
    if (/xml|html/i.test(contentType || '') || trimmed.startsWith('<')) return { kind: 'xml', text: body };
    return { kind: 'text', text: body };
};

export const formatBytes = (text) => {
    const bytes = new Blob([text]).size;
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
};
