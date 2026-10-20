import React from 'react';
import { statusClass } from './exchangeModel.js';
import { METHOD_TONES, STATUS_STYLES } from './httpStyles.js';

export const MethodChip = ({ method }) => (
    <span className={`shrink-0 rounded px-2 py-0.5 font-mono text-xs font-bold tracking-wide ${METHOD_TONES[method] || 'bg-slate-300 text-slate-950'}`}>
        {method || 'HTTP'}
    </span>
);

export const StatusChip = ({ status }) => {
    const { family, label } = statusClass(status);
    const { chip, icon: Icon } = STATUS_STYLES[family];
    return (
        <span className={`shrink-0 inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-bold ${chip}`}>
            <Icon className="w-3.5 h-3.5" />
            {status ?? '—'} <span className="font-semibold">{label}</span>
        </span>
    );
};
