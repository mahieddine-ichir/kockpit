import React from 'react';

// JSON syntax highlighting as React elements - never as an HTML string, so markup inside the
// values is shown, not run. Keys, strings, numbers and literals are told apart by weight and
// style as well as color; meant for a dark (slate-900) panel.

// Beyond this size, highlight nothing: tokenizing would stall the page
export const MAX_HIGHLIGHTED = 200 * 1024;

const JSON_TOKEN = /("(?:\\u[a-fA-F0-9]{4}|\\[^u]|[^\\"])*")(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?/g;

export const highlightJson = (text) => {
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
