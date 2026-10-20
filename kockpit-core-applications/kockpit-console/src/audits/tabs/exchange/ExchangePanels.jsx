import React from 'react';
import { ArrowDownLeft, ArrowUpRight } from 'lucide-react';
import BodyViewer from './BodyViewer.jsx';
import HeadersList from './HeadersList.jsx';
import { MethodChip, StatusChip } from './HttpChips.jsx';
import { headerValue, statusClass } from './exchangeModel.js';
import { STATUS_STYLES } from './httpStyles.js';

// One HTTP exchange drawn out: the request and the response side by side, each with a labelled
// band - shared by the Request & Response tab (the incoming call) and the HTTP Exchange tab (the
// outgoing ones). Bodies are shown as text, never as HTML.

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

const ExchangePanels = ({ request, response }) => {
    const status = STATUS_STYLES[statusClass(response?.status).family];
    return (
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
    );
};

export default ExchangePanels;
