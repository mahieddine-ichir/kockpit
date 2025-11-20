import React, {useEffect, useState} from "react";
import {updateFeatureFlag} from "../services/api.js";
import {CheckCircle, CircleSlash, Clock, Edit, Save, X} from "lucide-react";
import {useSearchParams} from "react-router-dom";
import Stats from "../components/StatsComponent.jsx";

const StateButtonForFlag = ({flag, toggle}) => {
    const [enabled, setEnabled] = useState(flag.enabled);
    function handleToggle(flag) {
        flag.enabled = !flag.enabled;
        setEnabled(flag.enabled);
        toggle(flag);
    }

    return (
        <div className="flex flex-col gap-3">
            <button
                onClick={() => handleToggle(flag)}
                className={`flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold w-fit ${
                    enabled
                        ? "bg-green-100 text-green-700"
                        : "bg-red-100 text-red-700"
                }`}
            >
                {flag.enabled ? (
                    <CheckCircle className="h-4 w-4" />
                ) : (
                    <CircleSlash className="h-4 w-4" />
                )}
                {enabled ? "ENABLED" : "DISABLED"}
            </button>
        {flag.expiration && (
            <div className="text-xs text-slate-500">
                Expiration:{" "}
                <span className="text-slate-700">{flag.expiration}</span>
            </div>
        )}
        {flag.comment && (
            <div className="text-sm text-slate-600">
                {flag.comment}
            </div>
        )}
    </div>
    )
};

const FeatureFlippingPage = ({ domain, env, config }) => {
    const [searchParams] = useSearchParams();
    const [loading, setLoading] = useState(false);
    const [editingKey, setEditingKey] = useState(null);
    const [history, setHistory] = useState([]);
    const [activeTab, setActiveTab] = useState("properties");
    const [originalFlag, setOriginalFlag] = useState(null);

    let flags = [];
    if (config['services']) {
        let service = config['services'].find(service => service.type === 'feature-flipping');
        console.log(`service ${JSON.stringify(service)}`);
        if (service['config']) {
            flags = service['config'].keys;
        }
    }

    useEffect(() => {
    }, [domain, env, config]);

    const loadFlags = () => {}

    const handleToggle = (flag) => {
        updateFeatureFlag(domain, env, flag)
            .then(resp => console.log(resp))
            .catch(err => {
                console.log(`Error updating ${err}`);
                flag.enabled = !flag.enabled;
            });
    }

    function handleSave(flag) {
        //await updateFeatureFlag(domain, env, flag);
        setEditingKey(null);
        setOriginalFlag(null);
        loadFlags();
        //await loadHistory();
    }

    const totalKeys = 0;//flags.reduce((sum, config) => sum + config.length, 0);
    const modifiedKeys = 0;
    /*flags.reduce((sum, config) => {
        return sum + config.filter((key, idx) => {
            const origConfig = flags.find(c => c.name === config.name);
            return !origConfig || key.value !== origConfig.config.keys[idx]?.value;
        }).length;
    }, 0);
     */

    return (
        <div className="min-h-screen bg-slate-50">
            <div className="mx-auto py-8">
                {/* Stats Cards */}
                <Stats keys={flags} totalKeys={totalKeys} modifiedKeys={modifiedKeys} />

                {/* Flags - keys */}
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

                {/* Config Header */}
                {activeTab === "properties" && (

                    <div className="divide-y divide-gray-200">
                        {flags.map(flag => (
                            <div key={flag.key}
                                 className="bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-all p-5 space-y-4">
                            <div>
                            <span className="text-xs uppercase tracking-wide text-slate-400">Feature Key</span>
                            <div className="font-mono text-slate-800 font-semibold break-all text-sm">
                                {flag.key}
                            </div>
                            </div>
                                <div className="border-t border-slate-100 pt-3 grid grid-cols-1 md:grid-cols-2 gap-4">
                                        {editingKey === flag.key ? (
                                            <div className="flex flex-col gap-3">
                                                <div>
                                                    <label className="block text-xs text-slate-500 mb-1">Expiration Date</label>
                                                    <input
                                                        type="date"
                                                        value={flag.expiration || ""}
                                                        onChange={e =>
                                                            console.log("onChange: "+e.target.value)
                                                            //setFlags(flags.map(f => f.key === flag.key ? { ...f, expiration: e.target.value } : f))
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
                                                            console.log("onChange: "+e.target.value)
                                                            //setFlags(flags.map(f => f.key === flag.key ? { ...f, comment: e.target.value } : f))
                                                        }
                                                        className="w-full border border-slate-300 rounded-lg px-2 py-1 text-sm"
                                                    />
                                                </div>
                                            </div>
                                        ) : (
                                            <StateButtonForFlag flag={flag} toggle={handleToggle} />
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
