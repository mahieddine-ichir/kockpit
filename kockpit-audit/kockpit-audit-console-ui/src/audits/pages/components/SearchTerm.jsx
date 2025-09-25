import React, {useEffect, useState} from "react";
import {XMarkIcon} from "@heroicons/react/20/solid/index.js";

const SearchTerm = ({term, setTerm, clearTerm}) => {
    const [text, setText] = useState('');
    useEffect(() => {
        if (text.length > 0) {
            setTerm(text);
        } else {
            clearTerm();
        }
    }, [text]);

    return (
        <div className="relative flex">
              <span className="absolute inset-y-0 left-1 pl-1 flex items-center pointer-events-none">
                  {term.name}
              </span>
            <input
                type="text"
                value={text}
                onChange={(e) => setText(e.target.value)}
                className="block w-full pl-10 py-1 rounded-xl border border-slate-200 bg-slate-50 text-slate-800 placeholder-slate-400 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-base transition-all shadow-sm"
            />
            <span className="absolute inset-y-0 right-2 pl-1 flex items-center pointer-events-none"
                  onClick={() => setText('')}
            >
                <XMarkIcon className="h-5 w-5 text-slate-400" />
            </span>
        </div>
    )
}

export default SearchTerm;
