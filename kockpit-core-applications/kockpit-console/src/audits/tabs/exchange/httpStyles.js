import { AlertTriangle, CheckCircle, CornerUpRight, HelpCircle, XCircle } from 'lucide-react';
import { FAILED_BORDER } from '../rules/failedStyle.js';

// Method and status tones, shared by the Details and Request & Response tabs. A status always
// goes with its name and icon (see HttpChips), never its color alone.

export const METHOD_TONES = {
    GET: 'bg-sky-400 text-slate-950',
    POST: 'bg-emerald-400 text-slate-950',
    PUT: 'bg-amber-400 text-slate-950',
    PATCH: 'bg-violet-400 text-slate-950',
    DELETE: 'bg-rose-400 text-slate-950',
};

export const STATUS_STYLES = {
    success: { chip: 'bg-emerald-400 text-slate-950', band: 'bg-emerald-100 text-emerald-950', border: 'border-emerald-200', icon: CheckCircle },
    redirect: { chip: 'bg-sky-400 text-slate-950', band: 'bg-sky-100 text-sky-950', border: 'border-sky-200', icon: CornerUpRight },
    client: { chip: 'bg-amber-400 text-slate-950', band: 'bg-amber-100 text-amber-950', border: 'border-amber-300', icon: AlertTriangle },
    // a server error is a failure: same dashed border as failed rules
    server: { chip: 'bg-red-500 text-white', band: 'bg-red-100 text-red-950', border: FAILED_BORDER, icon: XCircle },
    unknown: { chip: 'bg-slate-500 text-white', band: 'bg-gray-100 text-gray-900', border: 'border-gray-200', icon: HelpCircle },
};
