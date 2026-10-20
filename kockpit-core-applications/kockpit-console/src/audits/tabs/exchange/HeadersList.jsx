import React, { useState } from 'react';
import { Eye, Lock, Search } from 'lucide-react';
import { headerEntries, isSensitiveHeader } from './exchangeModel.js';

// A credential-looking value stays hidden until clicked
const SensitiveValue = ({ value }) => {
    const [shown, setShown] = useState(false);
    if (shown) return <span className="break-all">{value}</span>;
    return (
        <button
            type="button"
            onClick={() => setShown(true)}
            className="inline-flex items-center gap-1.5 rounded border border-dashed border-gray-400 px-1.5 py-0.5 text-xs text-gray-600 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-400"
        >
            <Lock className="w-3 h-3" />hidden<span className="text-gray-400">·</span><Eye className="w-3 h-3" />show
        </button>
    );
};

const HeadersList = ({ headers }) => {
    const [filter, setFilter] = useState('');
    const entries = headerEntries(headers);
    const needle = filter.trim().toLowerCase();
    const shown = needle
        // a hidden value isn't searched: typing would reveal what it contains
        ? entries.filter(([name, value]) => name.toLowerCase().includes(needle)
            || (!isSensitiveHeader(name) && value.toLowerCase().includes(needle)))
        : entries;

    return (
        <section>
            <div className="flex flex-wrap items-center gap-2">
                <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-500">Headers</h4>
                <span className="rounded-full bg-gray-200 px-1.5 text-[10px] font-semibold text-gray-700 tabular-nums">{entries.length}</span>
                {entries.length > 6 && (
                    <label className="ml-auto flex items-center gap-1.5 rounded-md border border-gray-300 bg-white px-2 py-1 text-xs focus-within:ring-2 focus-within:ring-blue-400">
                        <Search className="w-3.5 h-3.5 text-gray-400" />
                        <input
                            type="search"
                            value={filter}
                            onChange={(e) => setFilter(e.target.value)}
                            placeholder="Filter"
                            aria-label="Filter headers"
                            className="w-28 bg-transparent outline-none"
                        />
                    </label>
                )}
            </div>
            {entries.length === 0 ? (
                <p className="mt-1.5 text-sm italic text-gray-400">No headers</p>
            ) : (
                <dl className="mt-1.5 max-h-80 overflow-y-auto rounded-lg border border-gray-200 divide-y divide-gray-100 text-xs">
                    {shown.map(([name, value]) => (
                        <div key={name} className="grid grid-cols-[minmax(7rem,38%)_minmax(0,1fr)] gap-3 px-3 py-1.5 hover:bg-gray-50">
                            <dt className="font-mono font-semibold text-slate-700 break-all">{name}</dt>
                            <dd className="font-mono text-gray-900 break-all">
                                {isSensitiveHeader(name) ? <SensitiveValue value={value} /> : value}
                            </dd>
                        </div>
                    ))}
                    {shown.length === 0 && <p className="px-3 py-2 italic text-gray-400">No header matches “{filter}”</p>}
                </dl>
            )}
        </section>
    );
};

export default HeadersList;
