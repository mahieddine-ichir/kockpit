import React, { useState } from 'react';
import { ChevronDown, ChevronRight, ArrowRight, ArrowLeft, Timer, Network, Copy, FileJson } from 'lucide-react';
import TruncateWithTooltip from "../../components/TruncateWithTooltip.jsx";
import CopyButton from "../../components/CopyButton.jsx";

function tryFormatJson(str){
    try {
        const obj = JSON.parse(str);
        return JSON.stringify(obj,null,2)
    } catch (err) {
        console.log(err);
        return str
    }
}

const getMethodColor = (method) => {
    const colors = {
        GET: 'bg-blue-100 text-blue-800 border-blue-300',
        POST: 'bg-green-100 text-green-800 border-green-300',
        PUT: 'bg-orange-100 text-orange-800 border-orange-300',
        DELETE: 'bg-red-100 text-red-800 border-red-300',
        PATCH: 'bg-purple-100 text-purple-800 border-purple-300',
    };
    return colors[method] || 'bg-gray-100 text-gray-800 border-gray-300';
};

const getStatusColor = (status) => {
    if (status >= 200 && status < 300) return 'bg-green-100 text-green-800 border-green-300';
    if (status >= 300 && status < 400) return 'bg-blue-100 text-blue-800 border-blue-300';
    if (status >= 400 && status < 500) return 'bg-yellow-100 text-yellow-800 border-yellow-300';
    if (status >= 500) return 'bg-red-100 text-red-800 border-red-300';
    return 'bg-gray-100 text-gray-800 border-gray-300';
};

const getDurationColor = (duration) => {
    if (duration < 100) return 'text-green-600 bg-green-50 border-green-200';
    if (duration < 500) return 'text-blue-600 bg-blue-50 border-blue-200';
    if (duration < 1000) return 'text-orange-600 bg-orange-50 border-orange-200';
    return 'text-red-600 bg-red-50 border-red-200';
};

const highlightJson = (json) => {
    return json
        .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, (match) => {
            let cls = 'text-purple-400'; // number
            if (/^"/.test(match)) {
                if (/:$/.test(match)) {
                    cls = 'text-blue-400 font-semibold'; // key
                } else {
                    cls = 'text-green-400'; // string
                }
            } else if (/true|false/.test(match)) {
                cls = 'text-orange-400 font-semibold'; // boolean
            } else if (/null/.test(match)) {
                cls = 'text-red-400 font-semibold'; // null
            }
            return `<span class="${cls}">${match}</span>`;
        });
};



function HttpExchangeTab({ request }) {
    let exchangesAudits = request?.audits?.filter(audit => audit.type === 'builtin.httpexchanges');
    let exchanges = [];
    exchangesAudits?.forEach(audit => {
        audit?.events?.forEach(exchange => {
            const body = typeof exchange === 'string' ? JSON.parse(exchange) : exchange;
            exchanges.push(body);
        })
    });

    const [openRows, setOpenRows] = useState([]);

    const toggleRow= idx=> {
        setOpenRows(prevState =>
            prevState.includes(idx)? prevState.filter(i=> i!==idx):[...prevState, idx]
        )
    }

    return (
        <div className="bg-white shadow-sm overflow-hidden sm:rounded-lg border border-gray-200">
            {/* Header */}
            <div className="px-6 py-4 border-b border-gray-200 bg-gradient-to-r from-gray-50 to-white">
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <Network className="w-5 h-5 text-gray-600" />
                        <h3 className="text-lg font-semibold text-gray-900">HTTP Exchanges</h3>
                    </div>
                    {exchanges.length > 0 && (
                        <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                            {exchanges.length} {exchanges.length === 1 ? 'exchange' : 'exchanges'}
                        </span>
                    )}
                </div>
            </div>

            {/* Table or Empty State */}
            {exchanges.length > 0 ? (
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="w-10 px-3 py-3"></th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Method</th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">URL</th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Duration</th>
                        </tr>
                        </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {
                        exchanges.map((ex, idx) => {
                        return (
                            <React.Fragment key={idx}>
                                <tr className={`hover:bg-gray-50 transition-colors duration-150 ${openRows.includes(idx) ? 'bg-blue-50/50' : ''}`}>
                                    <td className="px-3 py-4 text-center">
                                        <button
                                            onClick={() =>toggleRow(idx)}
                                            className="p-1.5 rounded-md hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
                                            aria-expanded={openRows.includes(idx)}
                                            aria-label={openRows.includes(idx) ? 'Collapse details' : 'Expand details'}
                                        >
                                            {openRows.includes(idx) ? (
                                                <ChevronDown className="h-4 w-4 text-gray-600" />
                                            ) : (
                                                <ChevronRight className="h-4 w-4 text-gray-600" />
                                            )}
                                        </button>
                                    </td>
                                    <td className="px-4 py-4">
                                        <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-bold border-2 ${getMethodColor(ex['httpAuditedRequest'].method)}`}>
                                            {ex['httpAuditedRequest'].method}
                                        </span>
                                    </td>
                                    <td className="px-4 py-4">
                                        <div className="flex items-center gap-2">
                                            <TruncateWithTooltip text={ex['httpAuditedRequest'].uri} maxLength={60}/>
                                            <CopyButton value={ex['httpAuditedRequest'].uri}/>
                                        </div>
                                    </td>
                                    <td className="px-4 py-4">
                                        <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-bold border-2 ${getStatusColor(ex['httpAuditedResponse'].status)}`}>
                                            {ex['httpAuditedResponse'].status}
                                        </span>
                                    </td>
                                    <td className="px-4 py-4">
                                        <div className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-lg border ${getDurationColor(ex.endTime - ex.startTime)}`}>
                                            <Timer className="w-3.5 h-3.5" />
                                            <span className="text-xs font-bold">{ex.endTime - ex.startTime}ms</span>
                                        </div>
                                    </td>
                                </tr>
                                {openRows.includes(idx) &&(
                                    <tr>
                                        <td colSpan={5} className="bg-gradient-to-r from-blue-50 to-indigo-50">
                                            <div className="p-6">
                                                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                                                    {/* Request Section */}
                                                    <div className="space-y-4">
                                                        <div className="flex items-center gap-2 pb-2 border-b-2 border-blue-300">
                                                            <ArrowRight className="w-5 h-5 text-blue-600" />
                                                            <h3 className="text-base font-bold text-blue-900 uppercase tracking-wide">Request</h3>
                                                        </div>

                                                        {/* Request Headers */}
                                                        <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
                                                            <div className="flex items-center justify-between px-4 py-2 bg-gray-50 border-b border-gray-200">
                                                                <h4 className="text-xs font-semibold text-gray-700 uppercase tracking-wide">Headers</h4>
                                                                <CopyButton
                                                                    value={JSON.stringify(ex['httpAuditedRequest'].headers, null, 2)}
                                                                />
                                                            </div>
                                                            <div className="text-xs font-mono text-gray-800 p-4 max-h-48 overflow-auto">
                                                                {Object.entries(ex['httpAuditedRequest'].headers || {}).map(([key, value]) => (
                                                                    <div key={key} className="py-1.5 border-b border-gray-100 last:border-b-0 flex">
                                                                        <span className="font-bold text-blue-700 min-w-[140px]">{key}:</span>
                                                                        <span className="text-gray-600 flex-1 break-all">{value}</span>
                                                                    </div>
                                                                ))}
                                                                {Object.keys(ex['httpAuditedRequest'].headers || {}).length === 0 && (
                                                                    <span className="text-gray-400 italic">No headers</span>
                                                                )}
                                                            </div>
                                                        </div>

                                                        {/* Request Body */}
                                                        <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
                                                            <div className="flex items-center justify-between px-4 py-2 bg-gray-50 border-b border-gray-200">
                                                                <h4 className="text-xs font-semibold text-gray-700 uppercase tracking-wide">Body</h4>
                                                                <CopyButton
                                                                    value={ex['httpAuditedRequest'].body || ''}
                                                                />
                                                            </div>
                                                            <div className="bg-gray-900 p-4 max-h-64 overflow-auto">
                                                                {ex['httpAuditedRequest'].body ? (
                                                                    <pre
                                                                        className="text-xs font-mono leading-relaxed"
                                                                        dangerouslySetInnerHTML={{
                                                                            __html: highlightJson(tryFormatJson(ex['httpAuditedRequest'].body))
                                                                        }}
                                                                    />
                                                                ) : (
                                                                    <span className="text-gray-400 italic text-xs">Empty request body</span>
                                                                )}
                                                            </div>
                                                        </div>
                                                    </div>

                                                    {/* Response Section */}
                                                    <div className="space-y-4">
                                                        <div className="flex items-center gap-2 pb-2 border-b-2 border-green-300">
                                                            <ArrowLeft className="w-5 h-5 text-green-600" />
                                                            <h3 className="text-base font-bold text-green-900 uppercase tracking-wide">Response</h3>
                                                        </div>

                                                        {/* Response Headers */}
                                                        <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
                                                            <div className="flex items-center justify-between px-4 py-2 bg-gray-50 border-b border-gray-200">
                                                                <h4 className="text-xs font-semibold text-gray-700 uppercase tracking-wide">Headers</h4>
                                                                <CopyButton
                                                                    value={JSON.stringify(ex['httpAuditedResponse'].headers, null, 2)}
                                                                />
                                                            </div>
                                                            <div className="text-xs font-mono text-gray-800 p-4 max-h-48 overflow-auto">
                                                                {Object.entries(ex['httpAuditedResponse'].headers || {}).map(([key, value]) => (
                                                                    <div key={key} className="py-1.5 border-b border-gray-100 last:border-b-0 flex">
                                                                        <span className="font-bold text-green-700 min-w-[140px]">{key}:</span>
                                                                        <span className="text-gray-600 flex-1 break-all">{value}</span>
                                                                    </div>
                                                                ))}
                                                                {Object.keys(ex['httpAuditedResponse'].headers || {}).length === 0 && (
                                                                    <span className="text-gray-400 italic">No headers</span>
                                                                )}
                                                            </div>
                                                        </div>

                                                        {/* Response Body */}
                                                        <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
                                                            <div className="flex items-center justify-between px-4 py-2 bg-gray-50 border-b border-gray-200">
                                                                <h4 className="text-xs font-semibold text-gray-700 uppercase tracking-wide">Body</h4>
                                                                <CopyButton
                                                                    value={ex['httpAuditedResponse'].payload || ''}
                                                                />
                                                            </div>
                                                            <div className="bg-gray-900 p-4 max-h-64 overflow-auto">
                                                                {ex['httpAuditedResponse'].payload ? (
                                                                    <pre
                                                                        className="text-xs font-mono leading-relaxed"
                                                                        dangerouslySetInnerHTML={{
                                                                            __html: highlightJson(tryFormatJson(ex['httpAuditedResponse'].payload))
                                                                        }}
                                                                    />
                                                                ) : (
                                                                    <span className="text-gray-400 italic text-xs">Empty response body</span>
                                                                )}
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                )
                                }
                            </React.Fragment>
                        )
                    })
                    }
                    </tbody>
                </table>
            </div>
            ) : (
                <div className="text-center py-12">
                    <Network className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                    <p className="text-sm text-gray-500 font-medium">No HTTP exchanges available</p>
                    <p className="text-xs text-gray-400 mt-1">HTTP exchange data will appear here when available</p>
                </div>
            )}
        </div>
    );
}

export default HttpExchangeTab;
