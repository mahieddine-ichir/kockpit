import React, {useEffect, useRef, useState} from "react";

const SearchTerm = ({term, setTerm, clearTerm}) => {
    const [text, setText] = useState('');
    const timeoutRef = useRef(null);

    useEffect(() => {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }

        timeoutRef.current = setTimeout(() => {
            if (text && text.length > 0) {
                let valueToSend = text;
                if (term.type === 'number') {
                    valueToSend = Number(text);
                }
                setTerm(valueToSend);
            } else {
                clearTerm();
            }
        }, 300);

        return () => {
            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }
        };
    }, [text, term.type]);

    return (
        <div className="space-y-2">
            <label className="block text-sm font-medium text-slate-700">
                {term.name}
            </label>
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