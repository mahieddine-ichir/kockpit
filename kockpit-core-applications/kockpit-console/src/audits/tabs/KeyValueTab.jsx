import React, { useMemo, useState } from 'react';
import { Calendar, Hash, Search, Tag, Type, Percent } from 'lucide-react';
import CopyButton from '../../components/CopyButton.jsx';

// The audit's indexed key-values (what audits can be searched on), in the Rules tab's style: a
// dark banner with the counts and a filter, then a card per key - a key indexed several times
// (e.g. KId/KResult, once per rules execution) lists all its values in one card. Types read from
// a text badge, not only their color.

const TYPES = {
    string: { label: 'TEXT', icon: <Type className="w-3 h-3" />, tone: 'bg-slate-100 text-slate-700 border-slate-300' },
    integer: { label: 'INT', icon: <Hash className="w-3 h-3" />, tone: 'bg-sky-100 text-sky-900 border-sky-300' },
    float: { label: 'DEC', icon: <Percent className="w-3 h-3" />, tone: 'bg-violet-100 text-violet-900 border-violet-300' },
    date: { label: 'DATE', icon: <Calendar className="w-3 h-3" />, tone: 'bg-emerald-100 text-emerald-900 border-emerald-300' },
};

const typeOf = (kv) => {
    if (kv.valueInteger != null) return 'integer';
    if (kv.valueFloat != null) return 'float';
    if (kv.valueDate) return 'date';
    return 'string';
};

const display = (kv, type) => {
    if (type === 'integer') return String(kv.valueInteger);
    if (type === 'float') return String(kv.valueFloat);
    if (type === 'date') return new Date(kv.valueDate).toLocaleString();
    return kv.value ?? '';
};

// Repeated keys grouped at their first occurrence, the indexing order kept otherwise
const groupByKey = (keyValues) => {
    const groups = new Map();
    keyValues.forEach((kv) => {
        const type = typeOf(kv);
        if (!groups.has(kv.key)) groups.set(kv.key, { key: kv.key, values: [] });
        groups.get(kv.key).values.push({ type, text: display(kv, type) });
    });
    return [...groups.values()];
};

const TypeBadge = ({ type }) => {
    const { label, icon, tone } = TYPES[type];
    return (
        <span className={`shrink-0 inline-flex items-center gap-1 rounded border px-1.5 py-0.5 text-[10px] font-bold tracking-wide ${tone}`}>
            {icon}{label}
        </span>
    );
};

const KeyCard = ({ group }) => {
    const types = [...new Set(group.values.map(v => v.type))];
    return (
        <section className="min-w-0 rounded-xl border-2 border-slate-200 bg-white shadow-sm">
            <div className="flex items-center gap-2 px-3 py-2 rounded-t-[10px] bg-slate-100">
                <span className="min-w-0 truncate font-mono text-sm font-semibold text-slate-900" title={group.key}>{group.key}</span>
                {group.values.length > 1 && (
                    <span className="shrink-0 rounded-full bg-slate-700 px-1.5 text-[10px] font-bold text-white tabular-nums" title={`Indexed ${group.values.length} times`}>
                        ×{group.values.length}
                    </span>
                )}
                <span className="ml-auto flex items-center gap-1">
                    {types.map(type => <TypeBadge key={type} type={type} />)}
                </span>
            </div>
            <ul className="divide-y divide-gray-100">
                {group.values.map((value, index) => (
                    <li key={index} className="flex items-center gap-2 px-3 py-2">
                        {value.text === ''
                            ? <span className="flex-1 text-sm italic text-gray-400">empty</span>
                            : <span className="flex-1 min-w-0 break-all font-mono text-sm font-semibold text-gray-900">{value.text}</span>}
                        <CopyButton value={value.text} />
                    </li>
                ))}
            </ul>
        </section>
    );
};

const KeyValueTab = ({ request }) => {
    const keyValues = useMemo(() => request?.indexedKeyValues || [], [request]);
    const groups = useMemo(() => groupByKey(keyValues), [keyValues]);
    const [filter, setFilter] = useState('');

    const needle = filter.trim().toLowerCase();
    const shown = needle
        ? groups.filter(group => group.key.toLowerCase().includes(needle)
            || group.values.some(value => value.text.toLowerCase().includes(needle)))
        : groups;

    if (keyValues.length === 0) {
        return (
            <div className="py-12 text-center">
                <Tag className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                <p className="text-sm text-gray-500 font-medium">No indexed key-values</p>
                <p className="text-xs text-gray-400 mt-1">They appear here when the application indexes some for this request</p>
            </div>
        );
    }

    return (
        <div className="space-y-4">
            <div className="flex flex-wrap items-center gap-x-3 gap-y-2 rounded-xl bg-slate-800 px-4 py-3 text-white shadow-sm">
                <Tag className="w-4 h-4 shrink-0 text-sky-300" />
                <span className="text-[10px] font-semibold uppercase tracking-wider text-slate-400">Indexed keys</span>
                <span className="text-sm font-semibold">
                    {groups.length} {groups.length === 1 ? 'key' : 'keys'}
                    {keyValues.length !== groups.length && <span className="font-normal text-slate-300"> · {keyValues.length} values</span>}
                </span>
                <span className="text-xs text-slate-400">searchable in the audit list</span>
                <label className="ml-auto flex items-center gap-1.5 rounded-md bg-slate-700 px-2 py-1 text-xs focus-within:ring-2 focus-within:ring-sky-300">
                    <Search className="w-3.5 h-3.5 text-slate-400" />
                    <input
                        type="search"
                        value={filter}
                        onChange={(e) => setFilter(e.target.value)}
                        placeholder="Filter keys or values"
                        aria-label="Filter key-values"
                        className="w-44 bg-transparent text-white placeholder:text-slate-400 outline-none"
                    />
                </label>
            </div>

            {shown.length === 0 ? (
                <p className="px-1 text-sm italic text-gray-400">No key or value matches “{filter}”</p>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-3 items-start">
                    {shown.map(group => <KeyCard key={group.key} group={group} />)}
                </div>
            )}
        </div>
    );
};

export default KeyValueTab;
