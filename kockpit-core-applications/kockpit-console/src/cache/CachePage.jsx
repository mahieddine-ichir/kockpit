import React, {useCallback, useEffect, useState} from 'react';
import {CircleStackIcon} from '@heroicons/react/24/outline';
import {Eraser, RefreshCw} from 'lucide-react';
import {useSearchParams} from 'react-router-dom';
import {clearCache, getCacheStats, resetCacheStats} from '../services/api.js';

const fmt = (v, decimals = 1) =>
    v == null ? '—' : Number(v).toFixed(decimals);

const pct = (v) =>
    v == null ? '—' : `${Number(v).toFixed(1)} %`;

const HitBar = ({hit, miss}) => {
    if (hit == null) return null;
    return (
        <div className="flex items-center gap-2">
            <div className="flex-1 h-2 rounded-full bg-slate-200 overflow-hidden">
                <div
                    className="h-full bg-green-500 rounded-full transition-all"
                    style={{width: `${Math.min(hit, 100)}%`}}
                />
            </div>
            <span className="text-xs font-mono text-slate-600 w-14 text-right">{pct(hit)}</span>
        </div>
    );
};

const StatCard = ({label, value}) => (
    <div className="bg-white rounded-xl border border-slate-200 p-4 flex flex-col gap-1 shadow-sm">
        <span className="text-xs uppercase tracking-wide text-slate-400">{label}</span>
        <span className="text-2xl font-bold text-slate-800">{value ?? '—'}</span>
    </div>
);

const CachePage = ({domain, env}) => {
    const [searchParams] = useSearchParams();
    const serviceId = searchParams.get('service') || null;
    const [stats, setStats] = useState([]);
    const [loading, setLoading] = useState(false);
    const [clearing, setClearing] = useState(false);
    const [resetting, setResetting] = useState(false);

    const refresh = useCallback(() => {
        if (!serviceId) return;
        setLoading(true);
        getCacheStats(domain, env, serviceId)
            .then(setStats)
            .catch(() => setStats([]))
            .finally(() => setLoading(false));
    }, [domain, env, serviceId]);

    useEffect(() => { refresh(); }, [refresh]);

    const handleClearCache = () => {
        setClearing(true);
        clearCache(domain, env, serviceId).finally(() => setClearing(false));
    };

    const handleResetStats = () => {
        setResetting(true);
        resetCacheStats(domain, env, serviceId)
            .then(() => setStats([]))
            .finally(() => setResetting(false));
    };

    const totalGets = stats.reduce((s, r) => s + (r.cacheGets ?? 0), 0);
    const totalHits = stats.reduce((s, r) => s + (r.cacheHits ?? 0), 0);
    const totalMisses = stats.reduce((s, r) => s + (r.cacheMisses ?? 0), 0);
    const overallHitPct = totalGets > 0 ? (totalHits / totalGets * 100) : null;

    return (
        <div className="min-h-screen bg-slate-50">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="mb-8 flex items-start justify-between">
                    <div>
                        <h1 className="text-3xl font-bold text-slate-800 mb-2 flex items-center gap-3">
                            <CircleStackIcon className="h-8 w-8 text-blue-600"/>
                            Cache Statistics
                        </h1>
                        <p className="text-slate-600">{serviceId}</p>
                    </div>
                    <div className="flex items-center gap-2">
                        <button
                            onClick={refresh}
                            disabled={loading}
                            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700 active:bg-blue-800 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-sm font-medium shadow-sm"
                            title="Refresh cache statistics from backend"
                        >
                            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`}/>
                            {loading ? 'Refreshing…' : 'Refresh'}
                        </button>
                        <button
                            onClick={handleClearCache}
                            disabled={clearing}
                            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-amber-500 text-white hover:bg-amber-600 active:bg-amber-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-sm font-medium shadow-sm"
                            title="Publish a cache reset message to consumers"
                        >
                            <Eraser className="h-4 w-4"/>
                            {clearing ? 'Clearing…' : 'Clear cache'}
                        </button>
                        <button
                            onClick={handleResetStats}
                            disabled={resetting}
                            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-red-600 text-white hover:bg-red-700 active:bg-red-800 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-sm font-medium shadow-sm"
                            title="Reset the statistics in the backend"
                        >
                            <RefreshCw className={`h-4 w-4 ${resetting ? 'animate-spin' : ''}`}/>
                            {resetting ? 'Resetting…' : 'Reset stats'}
                        </button>
                    </div>
                </div>

                <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
                    <StatCard label="Caches" value={stats.length}/>
                    <StatCard label="Total Gets" value={totalGets.toLocaleString()}/>
                    <StatCard label="Total Hits" value={totalHits.toLocaleString()}/>
                    <StatCard label="Hit Rate" value={overallHitPct != null ? pct(overallHitPct) : '—'}/>
                </div>

                {loading && (
                    <div className="text-center py-12 text-slate-400">Loading…</div>
                )}

                {!loading && stats.length === 0 && (
                    <div className="text-center py-12 text-slate-400 italic">
                        No cache statistics received yet.
                    </div>
                )}

                {!loading && stats.length > 0 && (
                    <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
                        <table className="w-full text-sm text-left">
                            <thead className="bg-slate-50 border-b border-slate-200">
                            <tr>
                                {['Cache', 'Gets', 'Hits', 'Misses', 'Removals', 'Evictions', 'Hit Rate', 'Avg Get (ms)', 'Avg Put (ms)', 'Avg Remove (ms)'].map(h => (
                                    <th key={h} className="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-slate-500">{h}</th>
                                ))}
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100">
                            {stats.map((row) => (
                                <tr key={row.name} className="hover:bg-slate-50 transition-colors">
                                    <td className="px-4 py-3 font-mono font-semibold text-slate-800 whitespace-nowrap">{row.name}</td>
                                    <td className="px-4 py-3 text-slate-700">{row.cacheGets?.toLocaleString() ?? '—'}</td>
                                    <td className="px-4 py-3 text-green-700 font-medium">{row.cacheHits?.toLocaleString() ?? '—'}</td>
                                    <td className="px-4 py-3 text-red-600">{row.cacheMisses?.toLocaleString() ?? '—'}</td>
                                    <td className="px-4 py-3 text-slate-500">{row.cacheRemovals?.toLocaleString() ?? '—'}</td>
                                    <td className="px-4 py-3 text-slate-500">{row.cacheEvictions?.toLocaleString() ?? '—'}</td>
                                    <td className="px-4 py-3 min-w-[140px]">
                                        <HitBar hit={row.cacheHitPercentage} miss={row.cacheMissPercentage}/>
                                    </td>
                                    <td className="px-4 py-3 font-mono text-slate-600">{fmt(row.averageGetTime)}</td>
                                    <td className="px-4 py-3 font-mono text-slate-600">{fmt(row.averagePutTime)}</td>
                                    <td className="px-4 py-3 font-mono text-slate-600">{fmt(row.averageRemoveTime)}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};

export default CachePage;
