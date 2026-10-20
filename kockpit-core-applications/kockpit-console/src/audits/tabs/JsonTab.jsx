import React, { useMemo, useState } from 'react';
import { Check, ChevronsDownUp, ChevronsUpDown, Code2, Copy, Download, ListTree, Search, WrapText } from 'lucide-react';
import JsonTree from './json/JsonTree.jsx';
import { findMatches } from './json/jsonTreeModel.js';
import { MAX_HIGHLIGHTED, highlightJson } from './jsonHighlight.jsx';
import { formatBytes } from './exchange/exchangeModel.js';

// The raw audit, in the Rules tab's style: a dark banner (size, search, view, expand/collapse,
// copy, download) over a dark panel showing it as a collapsible tree - the default, with JSON held
// in strings opened as trees too - or as highlighted text. Everything is rendered as text: markup
// inside the audit's values is shown, never run.

// Opened at first, below that collapsed - unless a search or expand/collapse all says otherwise
const DEFAULT_OPEN_DEPTH = 2;

const VIEWS = [
    { id: 'tree', label: 'Tree', icon: <ListTree className="w-3.5 h-3.5" /> },
    { id: 'text', label: 'Text', icon: <Code2 className="w-3.5 h-3.5" /> },
];

const bannerButton = 'inline-flex items-center gap-1.5 rounded-md px-2.5 py-1 text-xs font-semibold text-slate-200 hover:bg-white/10 focus:outline-none focus:ring-2 focus:ring-sky-300';

const JsonTab = ({ request }) => {
    const jsonString = useMemo(() => JSON.stringify(request, null, 2), [request]);
    const [view, setView] = useState('tree');
    const [search, setSearch] = useState('');
    const [mode, setMode] = useState('default');
    const [overrides, setOverrides] = useState(() => new Map());
    const [copied, setCopied] = useState(false);
    const [wrap, setWrap] = useState(true);

    const needle = search.trim().toLowerCase();
    const matches = useMemo(() => (needle ? findMatches(request, needle) : null), [request, needle]);
    const text = useMemo(
        () => (view === 'text' && jsonString.length <= MAX_HIGHLIGHTED ? highlightJson(jsonString) : jsonString),
        [view, jsonString]);

    const isOpen = (path, depth) => {
        if (overrides.has(path)) return overrides.get(path);
        if (path === '') return true;
        if (matches) return matches.open.has(path);
        if (mode === 'all') return true;
        if (mode === 'none') return false;
        return depth < DEFAULT_OPEN_DEPTH;
    };
    const toggle = (path, wasOpen) => setOverrides(prev => new Map(prev).set(path, !wasOpen));
    const expandAll = (all) => {
        setMode(all ? 'all' : 'none');
        setOverrides(new Map());
    };
    const changeSearch = (value) => {
        setSearch(value);
        setOverrides(new Map());
    };

    const copy = async () => {
        try {
            await navigator.clipboard.writeText(jsonString);
            setCopied(true);
            setTimeout(() => setCopied(false), 1500);
        } catch (err) {
            console.error(err);
        }
    };

    const download = () => {
        const url = URL.createObjectURL(new Blob([jsonString], { type: 'application/json' }));
        const a = document.createElement('a');
        a.href = url;
        a.download = `audit-${request?.id || request?.requestId || 'data'}.json`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    };

    return (
        <div className="space-y-3">
            <div className="flex flex-wrap items-center gap-x-3 gap-y-2 rounded-xl bg-slate-800 px-4 py-3 text-white shadow-sm">
                <Code2 className="w-4 h-4 shrink-0 text-sky-300" />
                <span className="text-[10px] font-semibold uppercase tracking-wider text-slate-400">Raw audit</span>
                <span className="text-sm font-semibold tabular-nums">{formatBytes(jsonString)}</span>

                <div role="radiogroup" aria-label="JSON view" className="inline-flex rounded-lg bg-slate-700 p-0.5">
                    {VIEWS.map(({ id, label, icon }) => (
                        <button
                            key={id}
                            type="button"
                            role="radio"
                            aria-checked={view === id}
                            onClick={() => setView(id)}
                            className={`inline-flex items-center gap-1.5 rounded-md px-2.5 py-1 text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-sky-300 ${view === id ? 'bg-white text-slate-900' : 'text-slate-300 hover:text-white'}`}
                        >
                            {icon}{label}
                        </button>
                    ))}
                </div>

                <span className="ml-auto flex flex-wrap items-center gap-1">
                    {view === 'tree' ? (
                        <>
                            <label className="flex items-center gap-1.5 rounded-md bg-slate-700 px-2 py-1 text-xs focus-within:ring-2 focus-within:ring-sky-300">
                                <Search className="w-3.5 h-3.5 text-slate-400" />
                                <input
                                    type="search"
                                    value={search}
                                    onChange={(e) => changeSearch(e.target.value)}
                                    placeholder="Search keys and values"
                                    aria-label="Search the audit"
                                    className="w-44 bg-transparent text-white placeholder:text-slate-400 outline-none"
                                />
                            </label>
                            {matches && (
                                <span className={`px-1 text-xs font-semibold tabular-nums ${matches.count ? 'text-yellow-300' : 'text-slate-400'}`}>
                                    {matches.count} {matches.count === 1 ? 'match' : 'matches'}
                                </span>
                            )}
                            <button type="button" className={bannerButton} onClick={() => expandAll(true)} title="Expand everything">
                                <ChevronsUpDown className="w-3.5 h-3.5" />Expand
                            </button>
                            <button type="button" className={bannerButton} onClick={() => expandAll(false)} title="Collapse everything">
                                <ChevronsDownUp className="w-3.5 h-3.5" />Collapse
                            </button>
                        </>
                    ) : (
                        <button type="button" className={bannerButton} onClick={() => setWrap(w => !w)} aria-pressed={wrap}>
                            <WrapText className="w-3.5 h-3.5" />{wrap ? 'Wrapped' : 'One line each'}
                        </button>
                    )}
                    <button type="button" className={bannerButton} onClick={copy}>
                        {copied ? <Check className="w-3.5 h-3.5 text-emerald-300" /> : <Copy className="w-3.5 h-3.5" />}{copied ? 'Copied' : 'Copy'}
                    </button>
                    <button type="button" className={bannerButton} onClick={download}>
                        <Download className="w-3.5 h-3.5" />Download
                    </button>
                </span>
            </div>

            <div className="max-h-[75vh] overflow-auto rounded-xl bg-slate-900 p-4 shadow-sm">
                {view === 'tree' ? (
                    <JsonTree data={request} isOpen={isOpen} onToggle={toggle} needle={needle} />
                ) : (
                    <pre className={`font-mono text-xs leading-relaxed text-slate-100 ${wrap ? 'whitespace-pre-wrap break-all' : 'whitespace-pre'}`}>{text}</pre>
                )}
            </div>
        </div>
    );
};

export default JsonTab;
