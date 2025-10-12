import React, { useEffect, useState } from "react";
import { getFeatureFlags, updateFeatureFlag, getFeatureHistory } from "../services/api.js";
import { RefreshCcw, Edit, Save, X, CheckCircle, CircleSlash, Clock } from "lucide-react";

const FeatureFlippingPage = ({ domain, env }) => {
    const [flags, setFlags] = useState([]);
    const [loading, setLoading] = useState(true);
    const [editingKey, setEditingKey] = useState(null);
    const [history, setHistory] = useState([]);
    const [activeTab, setActiveTab] = useState("properties");
    const [originalFlag, setOriginalFlag] = useState(null);

    useEffect(() => { loadFlags(); loadHistory(); }, [domain, env]);

    async function loadFlags() {
        setLoading(true);
        try {
            const data = await getFeatureFlags(domain, env);
            setFlags(Object.entries(data || {}).map(([key, value]) => ({ key, ...value })));
        } finally { setLoading(false); }
    }

    async function loadHistory() {
        try {
            const data = await getFeatureHistory(domain, env);
            setHistory(data || []);
        } catch (e) { console.error("Error loading history", e); }
    }

    async function handleToggle(flag) {
        const updated = { ...flag, enabled: !flag.enabled };
        setFlags(flags.map(f => (f.key === flag.key ? updated : f)));
        await updateFeatureFlag(domain, env, updated);
        await loadHistory();
    }

    async function handleSave(flag) {
        await updateFeatureFlag(domain, env, flag);
        setEditingKey(null);
        setOriginalFlag(null);
        loadFlags();
        await loadHistory();
    }

    return (
        <div className="min-h-screen bg-gradient-to-b from-slate-50 to-slate-100 px-6 py-8">
            <div className="max-w-5xl mx-auto space-y-6">
                <div className="flex justify-between items-center">
                    <div>
                        <h1 className="text-3xl font-bold text-slate-800">Feature Flipping</h1>
                        <p className="text-slate-500 text-sm">Manage runtime feature toggles across environments</p>
                    </div>
                    <button
                        onClick={loadFlags}
                        className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-xl hover:bg-blue-700 shadow-sm transition-all"
                    >
                        <RefreshCcw className="h-4 w-4" /> Refresh
                    </button>
                    {/*//add later refresh ico*/}
                </div>

                <div className="flex gap-3 border-b border-slate-200">
                    {["properties", "history"].map(tab => (
                        <button
                            key={tab}
                            className={`px-4 py-2 rounded-t-lg text-sm font-medium transition-all ${
                                activeTab === tab
                                    ? "bg-white border border-slate-200 border-b-0 text-blue-600"
                                    : "text-slate-500 hover:text-slate-700"
                            }`}
                            onClick={() => setActiveTab(tab)}
                        >
                            {tab === "properties" ? "Properties" : "History"}
                        </button>
                    ))}
                </div>

                {activeTab === "properties" && (
                    <div className="space-y-4">
                        {loading && (
                            <div className="text-center py-12 text-slate-500 animate-pulse">
                                Loading feature propert...
                            </div>
                        )}

                        {!loading &&
                            flags.map(flag => (
                                <div
                                    key={flag.key}
                                    className="bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-all p-5 space-y-4"
                                >
                                    <div>
                                        <span className="text-xs uppercase tracking-wide text-slate-400">Feature Key</span>
                                        <div className="font-mono text-slate-800 font-semibold break-all text-sm">
                                            {flag.key}
                                        </div>
                                    </div>
                                    <div className="border-t border-slate-100 pt-3 grid grid-cols-1 md:grid-cols-2 gap-4">
                                        {editingKey === flag.key ? (
                                            <div className="flex flex-col gap-3">
                                                {typeof flag.value !== "undefined" && (
                                                    <div>
                                                        <label className="block text-xs text-slate-500 mb-1">Value</label>
                                                        <input
                                                            type="text"
                                                            value={flag.value ?? ""}
                                                            onChange={e =>
                                                                setFlags(flags.map(f => f.key === flag.key ? { ...f, value: e.target.value } : f))
                                                            }
                                                            className="w-full border border-slate-300 rounded-lg px-2 py-1 text-sm"
                                                        />
                                                    </div>
                                                )}
                                                <div>
                                                    <label className="block text-xs text-slate-500 mb-1">Expiration Date</label>
                                                    <input
                                                        type="date"
                                                        value={flag.expiration || ""}
                                                        onChange={e =>
                                                            setFlags(flags.map(f => f.key === flag.key ? { ...f, expiration: e.target.value } : f))
                                                        }
                                                        className="w-full border border-slate-300 rounded-lg px-2 py-1 text-sm"
                                                    />
                                                </div>
                                                <div>
                                                    <label className="block text-xs text-slate-500 mb-1">Comment</label>
                                                    <input
                                                        type="text"
                                                        value={flag.comment || ""}
                                                        onChange={e =>
                                                            setFlags(flags.map(f => f.key === flag.key ? { ...f, comment: e.target.value } : f))
                                                        }
                                                        className="w-full border border-slate-300 rounded-lg px-2 py-1 text-sm"
                                                    />
                                                </div>
                                            </div>
                                        ) : (
                                            <div className="flex flex-col gap-3">
                                                {typeof flag.enabled !== "undefined" ? (
                                                    <button
                                                        onClick={() => handleToggle(flag)}
                                                        className={`flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold w-fit ${
                                                            flag.enabled
                                                                ? "bg-green-100 text-green-700"
                                                                : "bg-red-100 text-red-700"
                                                        }`}
                                                    >
                                                        {flag.enabled ? (
                                                            <CheckCircle className="h-4 w-4" />
                                                        ) : (
                                                            <CircleSlash className="h-4 w-4" />
                                                        )}
                                                        {flag.enabled ? "ENABLED" : "DISABLED"}
                                                    </button>
                                                ) : (
                                                    typeof flag.value !== "undefined" && (
                                                        <div>
                                                            <span className="text-xs text-slate-500 mr-1">Value:</span>
                                                            <span className="font-mono text-sm bg-slate-100 px-2 py-1 rounded">
                                {String(flag.value)}
                              </span>
                                                        </div>
                                                    )
                                                )}
                                                {flag.expiration && (
                                                    <div className="text-xs text-slate-500">
                                                        Expiration:{" "}
                                                        <span className="text-slate-700">{flag.expiration}</span>
                                                    </div>
                                                )}
                                                <div className="text-sm text-slate-600">
                                                    {flag.comment || <span className="italic text-slate-400">No comment</span>}
                                                </div>
                                            </div>
                                        )}

                                        <div className="flex md:justify-end gap-2 items-start mt-2 md:mt-0">
                                            {editingKey === flag.key ? (
                                                <>
                                                    <button
                                                        onClick={() => handleSave(flag)}
                                                        className="p-2 bg-green-500 text-white rounded-lg hover:bg-green-600"
                                                        title="Save changes"
                                                    >
                                                        <Save className="h-4 w-4" />
                                                    </button>
                                                    <button
                                                        onClick={() => setEditingKey(null)}
                                                        className="p-2 bg-slate-200 rounded-lg hover:bg-slate-300"
                                                        title="Cancel"
                                                    >
                                                        <X className="h-4 w-4" />
                                                    </button>
                                                </>
                                            ) : (
                                                <button
                                                    onClick={() => { setEditingKey(flag.key); setOriginalFlag({ ...flag }); }}
                                                    className="p-2 border border-slate-300 rounded-lg hover:bg-slate-50"
                                                    title="Edit flag"
                                                >
                                                    <Edit className="h-4 w-4 text-slate-600" />
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            ))}
                    </div>
                )}

                {activeTab === "history" && (
                    <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
                        <h3 className="font-semibold text-slate-800 mb-3 flex items-center gap-2">
                            <Clock className="h-4 w-4" /> Recent Changes
                        </h3>
                        <div className="max-h-96 overflow-auto text-sm">
                            {history.length === 0 ? (
                                <div className="text-slate-400 italic text-center py-4">No recent changes recorded</div>
                            ) : (
                                <table className="w-full text-left border-collapse">
                                    <thead>
                                    <tr className="text-slate-500 text-xs uppercase border-b border-slate-200">
                                        <th className="py-2 px-2">Timestamp</th>
                                        <th className="py-2 px-2">Key</th>
                                        <th className="py-2 px-2">Action</th>
                                        <th className="py-2 px-2">User</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {history.map((h, i) => (
                                        <tr
                                            key={i}
                                            className={`text-slate-700 ${i % 2 === 0 ? "bg-slate-50" : "bg-white"}`}
                                        >
                                            <td className="py-2 px-2">{h.timestamp}</td>
                                            <td className="py-2 px-2 font-mono">{h.key}</td>
                                            <td className="py-2 px-2">{h.action}</td>
                                            <td className="py-2 px-2">{h.user}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            )}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default FeatureFlippingPage;
