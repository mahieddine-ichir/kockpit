import React, { useMemo, useState } from 'react';
import { FileJson, FileCode2, FileText, WrapText } from 'lucide-react';
import CopyButton from '../../../components/CopyButton.jsx';
import { describeBody, formatBytes } from './exchangeModel.js';

// Beyond this size the JSON is shown without highlighting: tokenizing it would stall the page
const MAX_HIGHLIGHTED = 200 * 1024;

const JSON_TOKEN = /("(?:\\u[a-fA-F0-9]{4}|\\[^u]|[^\\"])*")(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?/g;

// Keys, strings, numbers and literals told apart by weight and style as well as color
const highlightJson = (text) => {
    const parts = [];
    let last = 0;
    let match;
    JSON_TOKEN.lastIndex = 0;
    while ((match = JSON_TOKEN.exec(text)) !== null) {
        if (match.index > last) parts.push(text.slice(last, match.index));
        const [token, string, colon, literal] = match;
        let className;
        if (string && colon) className = 'text-sky-300 font-semibold';
        else if (string) className = 'text-emerald-300';
        else if (literal) className = 'text-violet-300 italic';
        else className = 'text-amber-300';
        parts.push(<span key={match.index} className={className}>{string && colon ? string : token}</span>);
        if (string && colon) parts.push(colon);
        last = match.index + token.length;
    }
    if (last < text.length) parts.push(text.slice(last));
    return parts;
};

const KINDS = {
    json: { label: 'JSON', icon: FileJson },
    xml: { label: 'XML / HTML', icon: FileCode2 },
    text: { label: 'Text', icon: FileText },
};

const BodyViewer = ({ title, body, contentType }) => {
    const [wrap, setWrap] = useState(true);
    const { kind, text } = useMemo(() => describeBody(body, contentType), [body, contentType]);
    const content = useMemo(
        () => (kind === 'json' && text.length <= MAX_HIGHLIGHTED ? highlightJson(text) : text),
        [kind, text]);

    if (kind === 'empty') {
        return (
            <section>
                <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-500">{title}</h4>
                <p className="mt-1.5 text-sm italic text-gray-400">No {title.toLowerCase()}</p>
            </section>
        );
    }

    const { label, icon: Icon } = KINDS[kind];
    return (
        <section>
            <div className="flex flex-wrap items-center gap-2">
                <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-500">{title}</h4>
                <span className="inline-flex items-center gap-1 rounded border border-gray-300 bg-white px-1.5 py-0.5 text-[10px] font-semibold text-gray-700">
                    <Icon className="w-3 h-3" />{label}
                </span>
                <span className="text-[11px] text-gray-500 tabular-nums">{formatBytes(text)}</span>
                <span className="ml-auto flex items-center">
                    <button
                        type="button"
                        onClick={() => setWrap(w => !w)}
                        aria-pressed={wrap}
                        title={wrap ? 'Lines wrap - click for one line each' : 'One line each - click to wrap'}
                        className={`p-1 rounded focus:outline-none focus:ring-2 focus:ring-blue-400 ${wrap ? 'bg-gray-200 text-gray-800' : 'text-gray-500 hover:bg-gray-200'}`}
                    >
                        <WrapText className="w-4 h-4" />
                    </button>
                    <CopyButton value={typeof body === 'string' ? body : text} />
                </span>
            </div>
            <pre className={`mt-1.5 max-h-[28rem] overflow-auto rounded-lg bg-slate-900 p-3 text-xs leading-relaxed text-slate-100 ${wrap ? 'whitespace-pre-wrap break-all' : 'whitespace-pre'}`}>
                {content}
            </pre>
        </section>
    );
};

export default BodyViewer;
