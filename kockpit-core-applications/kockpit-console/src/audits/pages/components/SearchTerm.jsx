import React, {useEffect, useRef, useState} from "react";

const displayValue = (value) => {
    if (value === undefined || value === null) {
        return '';
    }
    return Array.isArray(value) ? value.join(', ') : String(value);
};

const SearchTerm = ({term, value, setTerm, clearTerm}) => {
    const [text, setText] = useState(() => displayValue(value));
    // the input is only turned into a filter on blur or Enter, never while typing
    const dirtyRef = useRef(false);

    // resync when the term is changed from outside (saved filter, chip removal, clear all)
    useEffect(() => {
        setText(displayValue(value));
        dirtyRef.current = false;
    }, [value]);

    const commitTerm = () => {
        if (!dirtyRef.current) {
            return;
        }
        dirtyRef.current = false;

        const trimmed = text.trim();
        setText(trimmed);
        if (trimmed.length === 0) {
            clearTerm();
        } else {
            setTerm(term.type === 'number' ? Number(trimmed) : trimmed);
        }
    };

    const handleChange = (e) => {
        dirtyRef.current = true;
        setText(e.target.value);
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            commitTerm();
        }
    };

    return (
        <div className="space-y-2">
            <label className="block text-sm font-medium text-slate-700">
                {term.name}
            </label>
            <input
                type={term.type === 'number' ? 'number' : 'text'}
                value={text}
                onChange={handleChange}
                onBlur={commitTerm}
                onKeyDown={handleKeyDown}
                placeholder={`Enter ${term.name.toLowerCase()}...`}
                className="block w-full px-3 py-2.5 rounded-lg border border-slate-200 bg-white text-slate-800 placeholder-slate-400 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm transition-colors shadow-sm"
            />
        </div>
    );
}

export default SearchTerm;
