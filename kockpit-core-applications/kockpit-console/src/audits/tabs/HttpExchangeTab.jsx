import React, { useMemo, useState } from 'react';
import { AlertCircle, ChevronDown, ChevronRight, Network, Search, Timer } from 'lucide-react';
import CopyButton from '../../components/CopyButton.jsx';
import ExchangePanels from './exchange/ExchangePanels.jsx';
import { MethodChip, StatusChip } from './exchange/HttpChips.jsx';
import { callFailed, parseOutgoingCalls, splitUri } from './exchange/exchangeModel.js';
import { FailedIcon } from './rules/RulesCommon.jsx';
import { FAILED_BORDER, hatch } from './rules/failedStyle.js';
import { count } from './rules/rulesModel.js';

// The calls the service made while handling the request, in the Rules tab's style: a dark banner
// with the counts and a filter, then a waterfall - one row per call, its bar placed in the
// request's time window - each row opening the call's request and response. A failed call (no
// response, 4xx/5xx) is dashed, marked with an X and hatched, like failed rules.

// The window the bars are drawn in: the audited request, stretched to any call outside it
const timeWindowOf = (request, calls) => {
    let start = request.start ?? Infinity;
    let end = request.end ?? -Infinity;
    calls.forEach(call => {
        if (call.startTime == null) return;
        start = Math.min(start, call.startTime);
        end = Math.max(end, call.startTime + (call.duration || 0));
    });
    return start === Infinity || end === -Infinity ? null : { start, span: Math.max(end - start, 1) };
};

const CallBar = ({ call, timeWindow, failed }) => {
    if (!timeWindow || call.startTime == null) return <div />;
    const offset = call.startTime - timeWindow.start;
    const left = (offset / timeWindow.span) * 100;
    const width = Math.max(((call.duration || 0) / timeWindow.span) * 100, 0.75);
    return (
        <div className="relative h-2.5 rounded-full bg-gray-200" title={`+${offset} ms → ${offset + (call.duration || 0)} ms`}>
            <div
                className={`absolute inset-y-0 rounded-full ${failed ? 'bg-red-600' : 'bg-sky-500'}`}
                style={{ left: `${Math.min(left, 100 - width)}%`, width: `${width}%`, ...(failed ? hatch(true) : {}) }}
            />
        </div>
    );
};

// fixed status width: the bars line up whatever the status chip says
const ROW_GRID = 'grid grid-cols-[1.25rem_2rem_minmax(0,1fr)_minmax(6rem,22%)_12rem_4.5rem] items-center gap-3';

const CallRow = ({ call, index, timeWindow, open, onToggle }) => {
    const failed = callFailed(call);
    const { host, path } = splitUri(call.request.uri);
    return (
        <li className={`rounded-xl border-2 bg-white shadow-sm ${failed ? `${FAILED_BORDER} bg-red-50/40` : 'border-slate-200'}`}>
            <button
                type="button"
                onClick={onToggle}
                aria-expanded={open}
                className={`w-full ${ROW_GRID} px-3 py-2.5 text-left rounded-[10px] hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-400 ${open ? 'bg-slate-50' : ''}`}
            >
                {open ? <ChevronDown className="w-4 h-4 text-gray-500" /> : <ChevronRight className="w-4 h-4 text-gray-500" />}
                <span className="text-xs font-semibold text-gray-400 tabular-nums">#{index + 1}</span>
                <span className="flex items-center gap-2 min-w-0">
                    {failed && <FailedIcon />}
                    <MethodChip method={call.request.method} />
                    <span className="min-w-0 truncate font-mono text-sm" title={call.request.uri}>
                        {host && <span className="font-semibold text-gray-900">{host}</span>}
                        <span className="text-gray-600">{path}</span>
                    </span>
                </span>
                <CallBar call={call} timeWindow={timeWindow} failed={failed} />
                <span className="flex min-w-0 overflow-hidden">
                    {call.response ? <StatusChip status={call.response.status} /> : <span className="text-xs font-semibold text-red-700">no response</span>}
                </span>
                <span className="text-right text-xs font-semibold text-gray-700 tabular-nums">{call.duration != null ? `${call.duration} ms` : '—'}</span>
            </button>
            {open && (
                <div className="border-t border-gray-200 p-4 space-y-3">
                    <div className="flex items-center gap-1 min-w-0 text-xs text-gray-500">
                        <span className="shrink-0 font-semibold uppercase tracking-wider">URL</span>
                        <span className="min-w-0 break-all font-mono text-gray-800">{call.request.uri}</span>
                        <CopyButton value={call.request.uri} />
                    </div>
                    <ExchangePanels request={call.request} response={call.response} />
                </div>
            )}
        </li>
    );
};

const HttpExchangeTab = ({ request }) => {
    const { calls, parseError } = useMemo(() => {
        try {
            return { calls: parseOutgoingCalls(request), parseError: null };
        } catch (err) {
            console.error('Failed to parse the HTTP exchanges', err);
            return { calls: [], parseError: err };
        }
    }, [request]);
    const timeWindow = useMemo(() => timeWindowOf(request, calls), [request, calls]);
    const [open, setOpen] = useState(() => new Set());
    const [filter, setFilter] = useState('');

    if (parseError || calls.length === 0) {
        return parseError ? (
            <div className="flex items-center gap-3 rounded-lg border border-orange-200 bg-orange-50 p-4 text-sm text-orange-900">
                <AlertCircle className="w-5 h-5 shrink-0" />Unable to read the HTTP exchanges: {parseError.message}
            </div>
        ) : (
            <div className="py-12 text-center">
                <Network className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                <p className="text-sm text-gray-500 font-medium">No outgoing HTTP call</p>
                <p className="text-xs text-gray-400 mt-1">The calls the service makes while handling a request appear here</p>
            </div>
        );
    }

    const failed = calls.filter(callFailed).length;
    const totalTime = calls.reduce((sum, call) => sum + (call.duration || 0), 0);
    const needle = filter.trim().toLowerCase();
    const shown = needle
        ? calls.filter(call => `${call.request.method} ${call.request.uri} ${call.response?.status ?? ''}`.toLowerCase().includes(needle))
        : calls;

    const toggle = (key) => setOpen(prev => {
        const next = new Set(prev);
        if (next.has(key)) next.delete(key);
        else next.add(key);
        return next;
    });

    return (
        <div className="space-y-4">
            <div className="flex flex-wrap items-center gap-x-3 gap-y-2 rounded-xl bg-slate-800 px-4 py-3 text-white shadow-sm">
                <Network className="w-4 h-4 shrink-0 text-sky-300" />
                <span className="text-[10px] font-semibold uppercase tracking-wider text-slate-400">Outgoing calls</span>
                <span className="text-sm font-semibold">{count(calls.length, 'call')}</span>
                <span className="inline-flex items-center gap-1 text-xs text-slate-300 tabular-nums" title="Sum of the calls' durations">
                    <Timer className="w-3.5 h-3.5 text-slate-400" />{totalTime} ms in calls
                </span>
                {failed > 0 && (
                    <span className="inline-flex items-center gap-1 rounded-full bg-red-500 px-2 py-0.5 text-xs font-bold text-white">
                        ✕ {failed} failed
                    </span>
                )}
                <label className="ml-auto flex items-center gap-1.5 rounded-md bg-slate-700 px-2 py-1 text-xs focus-within:ring-2 focus-within:ring-sky-300">
                    <Search className="w-3.5 h-3.5 text-slate-400" />
                    <input
                        type="search"
                        value={filter}
                        onChange={(e) => setFilter(e.target.value)}
                        placeholder="Filter method, URL, status"
                        aria-label="Filter calls"
                        className="w-48 bg-transparent text-white placeholder:text-slate-400 outline-none"
                    />
                </label>
            </div>

            {timeWindow && (
                <div className={`${ROW_GRID} px-3 text-[10px] font-medium uppercase tracking-wider text-gray-400`}>
                    <span /><span /><span>Call</span>
                    <span className="flex justify-between normal-case tabular-nums"><span>0 ms</span><span>{timeWindow.span} ms</span></span>
                    <span>Status</span><span className="text-right">Duration</span>
                </div>
            )}

            {shown.length === 0 ? (
                <p className="px-1 text-sm italic text-gray-400">No call matches “{filter}”</p>
            ) : (
                <ol className="space-y-2">
                    {shown.map(call => (
                        <CallRow
                            key={call.key}
                            call={call}
                            index={calls.indexOf(call)}
                            timeWindow={timeWindow}
                            open={open.has(call.key)}
                            onToggle={() => toggle(call.key)}
                        />
                    ))}
                </ol>
            )}
        </div>
    );
};

export default HttpExchangeTab;
