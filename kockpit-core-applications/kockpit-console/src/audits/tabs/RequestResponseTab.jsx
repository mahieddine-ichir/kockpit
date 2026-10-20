import React, { useMemo, useState } from 'react';
import {
    AlertCircle, AlertTriangle, ArrowDownLeft, ArrowUpRight, CheckCircle, CornerUpRight, Fingerprint,
    HelpCircle, Network, Timer, User, XCircle,
} from 'lucide-react';
import CopyButton from '../../components/CopyButton.jsx';
import BodyViewer from './exchange/BodyViewer.jsx';
import HeadersList from './exchange/HeadersList.jsx';
import { headerValue, parseExchanges, statusClass } from './exchange/exchangeModel.js';
import { FAILED_BORDER } from './rules/failedStyle.js';

// The HTTP exchange in the Rules tab's style: a dark banner summing it up (method, URI, status,
// duration, who and which trace), then the request and the response side by side. The status
// reads from its label and icon, not only its color.

const METHOD_TONES = {
    GET: 'bg-sky-400 text-slate-950',
    POST: 'bg-emerald-400 text-slate-950',
    PUT: 'bg-amber-400 text-slate-950',
    PATCH: 'bg-violet-400 text-slate-950',
    DELETE: 'bg-rose-400 text-slate-950',
};

const STATUS_STYLES = {
    success: { chip: 'bg-emerald-400 text-slate-950', band: 'bg-emerald-100 text-emerald-950', border: 'border-emerald-200', icon: CheckCircle },
    redirect: { chip: 'bg-sky-400 text-slate-950', band: 'bg-sky-100 text-sky-950', border: 'border-sky-200', icon: CornerUpRight },
    client: { chip: 'bg-amber-400 text-slate-950', band: 'bg-amber-100 text-amber-950', border: 'border-amber-300', icon: AlertTriangle },
    // a server error is a failure: same dashed border as failed rules
    server: { chip: 'bg-red-500 text-white', band: 'bg-red-100 text-red-950', border: FAILED_BORDER, icon: XCircle },
    unknown: { chip: 'bg-slate-500 text-white', band: 'bg-gray-100 text-gray-900', border: 'border-gray-200', icon: HelpCircle },
};

const MethodChip = ({ method }) => (
    <span className={`shrink-0 rounded px-2 py-0.5 font-mono text-xs font-bold tracking-wide ${METHOD_TONES[method] || 'bg-slate-300 text-slate-950'}`}>
        {method || 'HTTP'}
    </span>
);

const StatusChip = ({ status }) => {
    const { family, label } = statusClass(status);
    const { chip, icon: Icon } = STATUS_STYLES[family];
    return (
        <span className={`shrink-0 inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-bold ${chip}`}>
            <Icon className="w-3.5 h-3.5" />
            {status ?? '—'} <span className="font-semibold">{label}</span>
        </span>
    );
};

const Banner = ({ exchange }) => {
    const { request, response, duration, traceId, traceIdHeaderName, origin } = exchange;
    return (
        <div className="rounded-xl bg-slate-800 px-4 py-3 text-white shadow-sm space-y-2">
            <div className="flex flex-wrap items-center gap-x-3 gap-y-2">
                <Network className="w-4 h-4 shrink-0 text-sky-300" />
                <span className="shrink-0 text-[10px] font-semibold uppercase tracking-wider text-slate-400">Exchange</span>
                <MethodChip method={request.method} />
                <span className="flex items-center min-w-0 flex-1">
                    <span className="truncate font-mono text-sm font-semibold" title={request.uri}>{request.uri || '/'}</span>
                    <span className="[&_button:hover]:bg-white/10 [&_svg]:text-slate-300"><CopyButton value={request.uri} /></span>
                </span>
                <StatusChip status={response?.status} />
                {duration != null && (
                    <span className="shrink-0 inline-flex items-center gap-1 text-xs font-semibold text-slate-200 tabular-nums">
                        <Timer className="w-3.5 h-3.5 text-slate-400" />{duration} ms
                    </span>
                )}
            </div>
            {(request.principal || traceId || origin) && (
                <div className="flex flex-wrap items-center gap-x-5 gap-y-1 text-xs text-slate-300">
                    {request.principal && (
                        <span className="inline-flex items-center gap-1.5"><User className="w-3.5 h-3.5 text-slate-400" />{request.principal}</span>
                    )}
                    {traceId && (
                        <span className="inline-flex items-center gap-1.5 min-w-0" title={traceIdHeaderName ? `Header ${traceIdHeaderName}` : undefined}>
                            <Fingerprint className="w-3.5 h-3.5 text-slate-400" />
                            <span className="text-slate-400">{traceIdHeaderName || 'trace'}</span>
                            <span className="font-mono truncate">{traceId}</span>
                        </span>
                    )}
                    {origin && <span className="text-slate-400">from <span className="text-slate-200">{origin}</span></span>}
                </div>
            )}
        </div>
    );
};

const Card = ({ icon, title, band, border, aside, children }) => (
    <section className={`min-w-0 rounded-xl border-2 bg-white shadow-sm ${border}`}>
        <div className={`flex flex-wrap items-center gap-2 px-4 py-2.5 rounded-t-[10px] ${band}`}>
            {icon}
            <h3 className="text-xs font-bold uppercase tracking-wider">{title}</h3>
            {aside && <span className="ml-auto flex items-center gap-2 min-w-0">{aside}</span>}
        </div>
        <div className="p-4 space-y-5">{children}</div>
    </section>
);

const QueryParams = ({ params }) => {
    const entries = Object.entries(params || {});
    if (entries.length === 0) return null;
    return (
        <section>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-500">Query parameters</h4>
            <div className="mt-1.5 flex flex-wrap gap-1.5">
                {entries.map(([name, value]) => (
                    <span key={name} className="inline-flex max-w-full items-center overflow-hidden rounded-md border border-sky-200 font-mono text-xs">
                        <span className="bg-sky-100 px-1.5 py-0.5 font-semibold text-sky-900">{name}</span>
                        <span className="truncate px-1.5 py-0.5 text-gray-900">{Array.isArray(value) ? value.join(', ') : String(value)}</span>
                    </span>
                ))}
            </div>
        </section>
    );
};

const ExchangeView = ({ exchange }) => {
    const { request, response } = exchange;
    const status = STATUS_STYLES[statusClass(response?.status).family];
    return (
        <div className="space-y-4">
            <Banner exchange={exchange} />
            <div className="grid grid-cols-1 xl:grid-cols-2 gap-4 items-start">
                <Card
                    icon={<ArrowUpRight className="w-4 h-4 shrink-0" />}
                    title="Request"
                    band="bg-slate-100 text-slate-900"
                    border="border-slate-200"
                    aside={<><MethodChip method={request.method} /><span className="truncate font-mono text-xs" title={request.uri}>{request.uri}</span></>}
                >
                    <QueryParams params={request.params} />
                    <HeadersList headers={request.headers} />
                    <BodyViewer title="Body" body={request.body} contentType={headerValue(request.headers, 'content-type')} />
                </Card>
                {response ? (
                    <Card
                        icon={<ArrowDownLeft className="w-4 h-4 shrink-0" />}
                        title="Response"
                        band={status.band}
                        border={status.border}
                        aside={<StatusChip status={response.status} />}
                    >
                        <HeadersList headers={response.headers} />
                        <BodyViewer title="Payload" body={response.body ?? response.payload} contentType={headerValue(response.headers, 'content-type')} />
                    </Card>
                ) : (
                    <Card icon={<ArrowDownLeft className="w-4 h-4 shrink-0" />} title="Response" band="bg-gray-100 text-gray-900" border="border-gray-200">
                        <p className="text-sm italic text-gray-400">No response recorded</p>
                    </Card>
                )}
            </div>
        </div>
    );
};

const RequestResponseTab = ({ request }) => {
    const { exchanges, parseError } = useMemo(() => {
        try {
            return { exchanges: parseExchanges(request), parseError: null };
        } catch (err) {
            console.error('Failed to parse the HTTP exchange', err);
            return { exchanges: [], parseError: err };
        }
    }, [request]);
    const [index, setIndex] = useState(0);

    if (parseError || exchanges.length === 0) {
        return (
            <div className="flex items-center gap-3 rounded-lg border border-orange-200 bg-orange-50 p-4 text-sm text-orange-900">
                <AlertCircle className="w-5 h-5 shrink-0" />
                {parseError ? `Unable to read the HTTP exchange: ${parseError.message}` : 'No HTTP exchange recorded for this request.'}
            </div>
        );
    }

    const current = exchanges[Math.min(index, exchanges.length - 1)];
    return (
        <div className="space-y-4">
            {exchanges.length > 1 && (
                <div role="radiogroup" aria-label="HTTP exchanges" className="inline-flex flex-wrap rounded-lg border border-gray-200 bg-gray-50 p-0.5 shadow-sm">
                    {exchanges.map((exchange, i) => (
                        <button
                            key={exchange.key}
                            type="button"
                            role="radio"
                            aria-checked={i === index}
                            onClick={() => setIndex(i)}
                            className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-blue-400 ${i === index ? 'bg-white text-blue-700 shadow-sm' : 'text-gray-500 hover:text-gray-800'}`}
                        >
                            #{i + 1} <span className="font-mono">{exchange.request.method}</span> {exchange.response?.status}
                        </button>
                    ))}
                </div>
            )}
            <ExchangeView exchange={current} />
        </div>
    );
};

export default RequestResponseTab;
