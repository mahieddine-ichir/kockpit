import React, {useEffect, useRef, useState} from "react";
// import {XMarkIcon} from "@heroicons/react/20/solid/index.js";
import { CalendarIcon } from '@heroicons/react/24/outline';

const SearchTerm = ({term, setTerm, clearTerm}) => {
    const [text, setText] = useState('');
    const [fromText, setFromText] = useState('');
    const [toText, setToText] = useState('');
    const timeoutRef = useRef(null);

    useEffect(() => {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }

        timeoutRef.current = setTimeout(() => {
            if (term.type === 'date') {
                const from = fromText ? new Date(fromText).getTime() : null;
                const to = toText ? new Date(toText).getTime() : null;

                if (from || to) {
                    setTerm([from, to]);
                } else {
                    clearTerm();
                }
            } else {
                if (text && text.length > 0) {
                    let valueToSend = text;
                    if (term.type === 'number') {
                        valueToSend = Number(text);
                    }
                    setTerm(valueToSend);
                } else {
                    clearTerm();
                }
            }
        }, 300);

        return () => {
            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }
        };
    }, [text, fromText, toText, term.type]);

    if (term.type === 'date') {
        const setPreset = (msAgo) => {
            const now = new Date();
            const from = new Date(Date.now() - msAgo);
            const pad = (n) => String(n).padStart(2, '0');
            const toStr = `${now.getFullYear()}-${pad(now.getMonth()+1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`;
            const fromStr = `${from.getFullYear()}-${pad(from.getMonth()+1)}-${pad(from.getDate())}T${pad(from.getHours())}:${pad(from.getMinutes())}`;
            setFromText(fromStr);
            setToText(toStr);
            setTerm([from.getTime(), now.getTime()]);
        };

        const presets = [
            { label: '5m', ms: 5*60*1000 },
            { label: '10m', ms: 10*60*1000 },
            { label: '1h', ms: 60*60*1000 },
            { label: '24h', ms: 24*60*60*1000 },
            { label: '7d', ms: 7*24*60*60*1000 },
            { label: '14d', ms: 14*24*60*60*1000 },
            { label: '1mo', ms: 30*24*60*60*1000 },
            { label: '2mo', ms: 60*24*60*60*1000 }
        ];

        return (
            <div className="space-y-3">
                <div className="flex items-center gap-2 text-slate-700">
                    <CalendarIcon className="h-4 w-4" />
                    <span className="text-sm font-medium">{term.name}</span>
                </div>
                <div className="grid grid-cols-1 gap-2">
                    <input
                        type="datetime-local"
                        value={fromText}
                        onChange={(e) => setFromText(e.target.value)}
                        className="block w-full px-3 py-2 rounded-lg border border-slate-200 bg-white text-slate-800 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm transition-colors"
                    />
                    <input
                        type="datetime-local"
                        value={toText}
                        onChange={(e) => setToText(e.target.value)}
                        className="block w-full px-3 py-2 rounded-lg border border-slate-200 bg-white text-slate-800 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm transition-colors"
                    />
                </div>
                <div className="flex flex-wrap gap-1">
                    {presets.map((preset) => (
                        <button
                            key={preset.label}
                            type="button"
                            onClick={() => setPreset(preset.ms)}
                            className="px-2 py-1 text-xs rounded border border-slate-200 text-slate-600 hover:bg-slate-50 hover:border-slate-300 transition-colors"
                        >
                            {preset.label}
                        </button>
                    ))}
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-2">
            <input
                type={term.type === 'number' ? 'number' : 'text'}
                value={text}
                onChange={(e) => setText(e.target.value)}
                placeholder={`Enter ${term.name.toLowerCase()}...`}
                className="block w-full px-3 py-2.5 rounded-lg border border-slate-200 bg-white text-slate-800 placeholder-slate-400 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm transition-colors shadow-sm"
            />
        </div>
    );
}

export default SearchTerm;