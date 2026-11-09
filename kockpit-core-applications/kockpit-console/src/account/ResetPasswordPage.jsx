import React, {useEffect, useState} from "react";
import {AlertTriangle, CheckCircle2, Clock, KeyRound} from "lucide-react";
import {previewPasswordReset, reasonOf, resetPassword} from "../services/tenantApi.js";

const inputClass = "mt-1 w-full rounded-lg border border-slate-400 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-slate-400";

// /reset-password#<code>: choose a new password; signed in or not, the link is what counts.
const ResetPasswordPage = ({code}) => {
    const [preview, setPreview] = useState(null);
    const [password, setPassword] = useState('');
    const [confirmation, setConfirmation] = useState('');
    const [error, setError] = useState(null);
    const [busy, setBusy] = useState(false);
    const [done, setDone] = useState(false);

    useEffect(() => {
        if (!code) {
            setError('This link holds no code: ask an administrator for the full link.');
            return;
        }
        previewPasswordReset(code).then(setPreview).catch(err => setError(reasonOf(err)));
    }, [code]);

    const submit = async (e) => {
        e.preventDefault();
        setError(null);
        if (password !== confirmation) {
            setError('The two passwords differ.');
            return;
        }
        setBusy(true);
        try {
            await resetPassword(code, password);
            setDone(true);
        } catch (err) {
            const retryAfter = err?.response?.headers?.['retry-after'];
            setError(reasonOf(err) + (retryAfter ? ` (retry in ${Math.ceil(retryAfter / 60)} min)` : ''));
        } finally {
            setBusy(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-slate-100 p-4">
            <div className="w-full max-w-sm bg-white rounded-xl shadow-lg border border-slate-200">
                <div className="flex justify-center pt-8 pb-4 bg-slate-800 rounded-t-xl">
                    <img src="/kockpit-logo.png" alt="Kockpit" className="h-10"/>
                </div>
                <div className="p-6">
                    <h1 className="text-lg font-bold text-slate-800 flex items-center gap-2 mb-4">
                        <KeyRound className="h-5 w-5" aria-hidden="true"/> Choose a new password
                    </h1>

                    {!preview && !error && (
                        <p className="text-sm text-slate-600 flex items-center gap-2"><Clock className="h-4 w-4 animate-spin" aria-hidden="true"/> Checking the link…</p>
                    )}

                    {done ? (
                        <div className="space-y-4">
                            <p role="status" className="flex items-start gap-2 text-sm text-slate-800">
                                <CheckCircle2 className="h-4 w-4 mt-0.5 flex-shrink-0" aria-hidden="true"/>
                                <span><span className="font-semibold">Password changed. </span>Sessions opened before this change are signed out.</span>
                            </p>
                            <a href="/" className="block text-center w-full py-2 rounded-lg bg-slate-800 text-white font-semibold hover:bg-slate-900">Go to sign in</a>
                        </div>
                    ) : preview && (
                        <form onSubmit={submit} className="space-y-4">
                            <p className="text-sm text-slate-700">For the account <span className="font-mono break-all">{preview.email}</span></p>
                            <input type="email" autoComplete="username" value={preview.email} readOnly hidden/>
                            <label className="block">
                                <span className="text-sm font-medium text-slate-700">New password</span>
                                <input type="password" required minLength={10} autoComplete="new-password" value={password}
                                       onChange={e => setPassword(e.target.value)} className={inputClass}/>
                                <span className="text-xs text-slate-500">At least 10 characters</span>
                            </label>
                            <label className="block">
                                <span className="text-sm font-medium text-slate-700">New password, again</span>
                                <input type="password" required autoComplete="new-password" value={confirmation}
                                       onChange={e => setConfirmation(e.target.value)} className={inputClass}/>
                            </label>
                            <button type="submit" disabled={busy}
                                    className="w-full py-2 rounded-lg bg-slate-800 text-white font-semibold hover:bg-slate-900 disabled:opacity-50">
                                {busy ? 'Please wait…' : 'Change my password'}
                            </button>
                        </form>
                    )}

                    {error && (
                        <div role="alert" className="flex items-start gap-2 text-sm text-red-800 bg-red-50 border border-red-300 rounded-lg p-3 mt-4">
                            <AlertTriangle className="h-4 w-4 mt-0.5 flex-shrink-0" aria-hidden="true"/>
                            <span><span className="font-semibold">Error: </span>{error}</span>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ResetPasswordPage;
