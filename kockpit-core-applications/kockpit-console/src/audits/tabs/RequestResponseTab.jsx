import React, { useMemo, useState } from 'react';
import { AlertCircle, Fingerprint, Network, Timer, User } from 'lucide-react';
import CopyButton from '../../components/CopyButton.jsx';
import ExchangePanels from './exchange/ExchangePanels.jsx';
import { MethodChip, StatusChip } from './exchange/HttpChips.jsx';
import { parseExchanges } from './exchange/exchangeModel.js';

// The HTTP exchange in the Rules tab's style: a dark banner summing it up (method, URI, status,
// duration, who and which trace), then the request and the response side by side. The status
// reads from its label and icon, not only its color.

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

const ExchangeView = ({ exchange }) => {
    const { request, response } = exchange;
    return (
        <div className="space-y-4">
            <Banner exchange={exchange} />
            <ExchangePanels request={request} response={response} />
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
