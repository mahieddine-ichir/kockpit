import React, { useState } from 'react';
import TruncateWithTooltip from "../../components/TruncateWithTooltip.jsx";
import {ChevronDownIcon, ChevronRightIcon} from "@heroicons/react/24/outline/index.js";
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



function HttpExchangeTab({ request }) {
    let exchangesAudits = request?.audits?.filter(audit => audit.type === 'builtin.httpexchanges');
    let exchanges = [];
    exchangesAudits?.forEach(audit => {
        audit?.events?.forEach(exchange => {
            exchanges.push(JSON.parse(exchange));
        })
    });

    const [openRows, setOpenRows] = useState([]);

    const toggleRow= idx=> {
        setOpenRows(prevState =>
            prevState.includes(idx)? prevState.filter(i=> i!==idx):[...prevState, idx]
        )
    }

    return (
        <div className="overflow-hidden rounded-lg border border-gray-200 shadow-sm">
            <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                <tr>
                    <th className="w-10 px-3 py-3"></th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Method</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">URL</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Duration</th>
                </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                {
                    exchanges.map((ex, idx) => {
                        return (
                            <React.Fragment key={idx}>
                                <tr className={`hover:bg-gray-50 transition-colors ${openRows.includes(idx) ? 'bg-gray-50' : ''}`}>
                                    <td className="px-3 py-4 text-center">
                                        <button
                                            onClick={() =>toggleRow(idx)}
                                            className="p-1 rounded-md hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-gray-300"
                                            aria-expanded={openRows.includes(idx)}
                                            aria-label={openRows.includes(idx) ? 'collapse' : 'expande'}
                                        >
                                            {openRows.includes(idx) ? (
                                                <ChevronDownIcon className="h-4 w-4 text-gray-600" />
                                            ) : (
                                                <ChevronRightIcon className="h-4 w-4 text-gray-600" />
                                            )}
                                        </button>
                                    </td>
                                    <td className="px-4 py-2 text-sm text-gray-900">
                                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                                        ex['httpAuditedRequest'].method === 'GET'
                                            ? 'bg-blue-100 text-blue-800'
                                            : ex['httpAuditedRequest'].method === 'POST'
                                                ? 'bg-green-100 text-green-800'
                                                : 'bg-gray-100 text-gray-800'
                                    }`}>
                                            {ex['httpAuditedRequest'].method}
                                        </span>
                                    </td>
                                    <td className="px-4 py-2 text-sm text-gray-900">
                                        <TruncateWithTooltip text={ex['httpAuditedRequest'].uri} maxLength={60}/>
                                        <CopyButton value={ex['httpAuditedRequest'].uri}/>
                                    </td>
                                    <td className="px-4 py-4 text-sm font-mono">
                                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                                            ex['httpAuditedResponse'].status >= 200 && ex['httpAuditedResponse'].status < 300
                                                ? 'bg-green-100 text-green-800'
                                                : ex['httpAuditedResponse'].status >= 400
                                                    ? 'bg-red-100 text-red-800'
                                                    : 'bg-yellow-100 text-yellow-800'
                                        }`}>
                                            {ex['httpAuditedResponse'].status}
                                        </span>
                                    </td>
                                    <td className="px-4 py-2 text-sm text-gray-900">{ex.endTime - ex.startTime} ms</td>
                                </tr>
                                {openRows.includes(idx) &&(
                                    <tr>
                                        <td colSpan={5} className="px-6 py-4 bg-gray-50 border-t">
                                            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                                                <div className="space-y-3">
                                                    <div className="flex items-center justify-between">
                                                        <h3 className="text-sm font-medium text-gray-900 flex items-center">
                                                            Request Headers
                                                            <CopyButton
                                                                value={JSON.stringify(ex['httpAuditedRequest'].headers, null, 2)}
                                                                className="ml-2"
                                                            />
                                                        </h3>
                                                    </div>
                                                    <div className="text-xs bg-gray-50 font-mono text-gray-800 p-3 rounded-md border border-gray-200 overflow-x-auto max-h-64">
                                                        {Object.entries(ex['httpAuditedRequest'].headers || {}).map(([key, value]) => (
                                                            <div key={key} className="py-1 border-b border-gray-100 last:border-b-0">
                                                                <span className="font-semibold">{key}:</span> {value}
                                                            </div>
                                                        ))}
                                                        {Object.keys(ex['httpAuditedRequest'].headers || {}).length === 0 && (
                                                            <span className="text-gray-400">No headers</span>
                                                        )}
                                                    </div>
                                                    <div className="flex items-center justify-between">
                                                        <h3 className="text-sm font-medium text-gray-900 flex items-center">
                                                            Request Body
                                                            <CopyButton
                                                                value={ex['httpAuditedRequest'].body}
                                                                className="ml-2"
                                                            />
                                                        </h3>
                                                    </div>
                                                    <pre className="text-xs bg-gray-50 font-mono text-gray-800 p-3 rounded-md border border-gray-200 overflow-x-auto max-h-64">
                                                                {ex['httpAuditedRequest'].body ? (
                                                                    tryFormatJson(ex['httpAuditedRequest'].body)
                                                                ) : (
                                                                    <span className="text-gray-400">Empty request body</span>
                                                                )}
                                            </pre>
                                                </div>
                                                <div className="space-y-3">
                                                    <div className="flex items-center justify-between">
                                                        <h3 className="text-sm font-medium text-gray-900 flex items-center">
                                                            Respoonse Headers
                                                            <CopyButton
                                                                value={JSON.stringify(ex['httpAuditedResponse'].headers, null, 2)}
                                                                className="ml-2"
                                                            />
                                                        </h3>
                                                    </div>
                                                    <div className="text-xs bg-gray-50 font-mono text-gray-800 p-3 rounded-md border border-gray-200 overflow-x-auto max-h-64">
                                                        {Object.entries(ex['httpAuditedResponse'].headers || {}).map(([key, value]) => (
                                                            <div key={key} className="py-1 border-b border-gray-100 last:border-b-0">
                                                                <span className="font-semibold">{key}:</span> {value}
                                                            </div>
                                                        ))}
                                                        {Object.keys(ex['httpAuditedResponse'].headers || {}).length === 0 && (
                                                            <span className="text-gray-400">No headers</span>
                                                        )}
                                                    </div>
                                                    <div className="flex items-center justify-between">
                                                        <h3 className="text-sm font-medium text-gray-900 flex items-center">
                                                            Response Body
                                                            <CopyButton
                                                                value={ex['httpAuditedResponse'].body}
                                                                className="ml-2"
                                                            />
                                                        </h3>
                                                    </div>
                                                    <pre className="text-xs bg-gray-50 font-mono text-gray-800 p-3 rounded-md border border-gray-200 overflow-x-auto max-h-64">
                                                                {ex['httpAuditedResponse'].payload ? (
                                                                    tryFormatJson(ex['httpAuditedResponse'].payload)
                                                                ) : (
                                                                    <span className="text-gray-400">Empty response body</span>
                                                                )}
                                            </pre>
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
    );
}

export default HttpExchangeTab;
