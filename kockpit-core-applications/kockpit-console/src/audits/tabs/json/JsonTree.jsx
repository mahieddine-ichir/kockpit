import React from 'react';
import { ChevronDown, ChevronRight } from 'lucide-react';
import CopyButton from '../../../components/CopyButton.jsx';
import { entriesOf, isContainer, parseEmbedded } from './jsonTreeModel.js';

// Collapsible JSON tree, rendered as React elements (values are text, never HTML). A string that
// holds JSON - audit events often do - opens as a tree of its own, marked "JSON string". Search
// matches are marked; what is open comes from the tab (see JsonTab).

const Mark = ({ text, needle }) => {
    if (!needle) return text;
    const lower = text.toLowerCase();
    const parts = [];
    let from = 0;
    let at;
    while ((at = lower.indexOf(needle, from)) !== -1) {
        if (at > from) parts.push(text.slice(from, at));
        parts.push(<mark key={at} className="rounded-sm bg-yellow-300 text-slate-950">{text.slice(at, at + needle.length)}</mark>);
        from = at + needle.length;
    }
    if (from < text.length) parts.push(text.slice(from));
    return parts;
};

const Primitive = ({ value, needle }) => {
    if (value === null) return <span className="italic text-violet-300">null</span>;
    if (typeof value === 'boolean') return <span className="italic text-violet-300">{String(value)}</span>;
    if (typeof value === 'number') return <span className="text-amber-300"><Mark text={String(value)} needle={needle} /></span>;
    return <span className="text-emerald-300 break-all">"<Mark text={value} needle={needle} />"</span>;
};

const Key = ({ name, needle }) => name == null ? null : (
    <>
        <span className={typeof name === 'number' ? 'text-slate-500' : 'font-semibold text-sky-300'}>
            {typeof name === 'number' ? name : <>"<Mark text={name} needle={needle} />"</>}
        </span>
        <span className="text-slate-500">: </span>
    </>
);

const JsonNode = ({ name, value, path, depth, isOpen, onToggle, needle }) => {
    const embedded = typeof value === 'string' ? parseEmbedded(value) : undefined;
    const container = isContainer(value) ? value : embedded;
    const indent = { paddingLeft: `${depth * 1.1}rem` };

    if (!container) {
        return (
            <div className="py-px pr-2" style={indent}>
                <span className="inline-block w-4" />
                <Key name={name} needle={needle} />
                <Primitive value={value} needle={needle} />
            </div>
        );
    }

    const open = isOpen(path, depth);
    const array = Array.isArray(container);
    const entries = entriesOf(container);
    const [opening, closing] = array ? ['[', ']'] : ['{', '}'];
    const size = array ? `${entries.length} ${entries.length === 1 ? 'item' : 'items'}` : `${entries.length} ${entries.length === 1 ? 'key' : 'keys'}`;

    return (
        <div>
            <div className="group flex items-center py-px pr-2" style={indent}>
                <button
                    type="button"
                    onClick={() => onToggle(path, open)}
                    aria-expanded={open}
                    aria-label={open ? 'Collapse' : 'Expand'}
                    className="inline-flex w-4 shrink-0 items-center justify-center rounded text-slate-400 hover:text-white focus:outline-none focus:ring-2 focus:ring-sky-300"
                >
                    {open ? <ChevronDown className="w-3.5 h-3.5" /> : <ChevronRight className="w-3.5 h-3.5" />}
                </button>
                <span className="min-w-0">
                    <Key name={name} needle={needle} />
                    {embedded && <span className="mr-1.5 rounded border border-slate-600 px-1 text-[10px] font-semibold text-slate-300">JSON string</span>}
                    <span className="text-slate-400">{opening}</span>
                    {!open && (
                        <button type="button" onClick={() => onToggle(path, open)} className="mx-1 rounded bg-slate-700 px-1.5 text-[10px] text-slate-300 hover:bg-slate-600">
                            {size}
                        </button>
                    )}
                    {!open && <span className="text-slate-400">{closing}</span>}
                </span>
                <span className="ml-2 opacity-0 group-hover:opacity-100 focus-within:opacity-100 [&_button:hover]:bg-white/10 [&_svg]:text-slate-400">
                    <CopyButton value={embedded ? value : JSON.stringify(container, null, 2)} />
                </span>
            </div>
            {open && (
                <>
                    {entries.map(([key, child]) => (
                        <JsonNode
                            key={key}
                            name={key}
                            value={child}
                            path={`${path}/${key}`}
                            depth={depth + 1}
                            isOpen={isOpen}
                            onToggle={onToggle}
                            needle={needle}
                        />
                    ))}
                    <div className="py-px text-slate-400" style={indent}><span className="inline-block w-4" />{closing}</div>
                </>
            )}
        </div>
    );
};

const JsonTree = ({ data, isOpen, onToggle, needle }) => (
    <div className="font-mono text-xs leading-relaxed text-slate-100">
        <JsonNode name={null} value={data} path="" depth={0} isOpen={isOpen} onToggle={onToggle} needle={needle} />
    </div>
);

export default JsonTree;
