import React,{ useEffect, useState, useCallback } from 'react';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend  } from 'recharts';
import { ClipboardDocumentIcon, EyeIcon, CheckIcon, ChartPieIcon } from '@heroicons/react/24/outline';
import { fetchDashboardSummary,  fetchDistribution,  fetchAvailableFilters} from "../../services/api.js"

const COLORS = {
    '200': '#10b981',
    '201': '#06d6a0',
    '204': '#84cc16',
    '400': '#f59e0b',
    '401': '#ef4444',
    '403': '#dc2626',
    '404': '#f97316',
    '500': '#7c2d12',
    '502': '#991b1b',
    '503': '#dc2626',
};

const getStatusColor = (entry) => {
    const key = String(entry?.label ?? entry?.name ?? entry?.status ?? entry?.code ?? '')
        .replace('HTTP ', '');
    return COLORS[key] || '#6B7280';
};

const normalizeStatusLabel = (label) => {
    const text = String(label ?? '').replace('HTTP ', '');
    const match = text.match(/\d{3}/);
    return match ? match[0] : text || '—';
};

const Dashboard = () => {
    const [summary, setSummary] = useState(null);
    const [statusData, setStatusData] = useState([]);
    const [methodData, setMethodData] = useState([]);
    const [domainData, setDomainData] = useState([]);
    const [filters, setFilters] = useState({});
    const [availableFilters, setAvailableFilters] = useState({ domains: [], environments: [] });
    const [loading, setLoading] = useState(true);
    const [lastUpdated, setLastUpdated] = useState(new Date());
    const [isInitialLoad, setIsInitialLoad] = useState(true);
    const [isRefreshing, setIsRefreshing] = useState(false);

    const loadData = useCallback(async () => {
        try {
            if (isInitialLoad) {
                setLoading(true);
            } else {
                setIsRefreshing(true);
            }
            const [summaryRes, statusRes, methodRes, domainRes, filtersRes] = await Promise.all([
                fetchDashboardSummary(filters),
                fetchDistribution('statusDistribution', filters),
                fetchDistribution('methodDistribution', filters),
                fetchDistribution('domainDistribution', filters),
                fetchAvailableFilters()
            ]);

            setSummary(summaryRes);
            setStatusData(statusRes);
            setMethodData(methodRes);
            setDomainData(domainRes);
            setAvailableFilters(filtersRes);
            setLastUpdated(new Date());
        } catch (error) {
            console.error('failed to load dashboard data:', error);
        } finally {
            if (isInitialLoad) {
                setLoading(false);
                setIsInitialLoad(false);
            } else {
                setIsRefreshing(false);
            }
        }
    }, [filters, isInitialLoad]);

    useEffect(() => {
        let cancelled = false;
        let timeoutId;
        const poll = async () => {
            await loadData();
            if (!cancelled) {
                timeoutId = setTimeout(poll, 5000);
            }
        };
        poll();
        return () => {
            cancelled = true;
            if (timeoutId) clearTimeout(timeoutId);
        };
    }, [loadData]);

    const handleFilterChange = (key, value) => {
        setFilters(prev => ({ ...prev, [key]: value }));
    };

    const clearFilters = () => {
        setFilters({});
    };

    if (loading && !summary) return <div className="p-8 text-center">Loading dashboard...</div>;

    return (
        <div className="p-6 space-y-8">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-gray-800">Dashboard Insights</h1>
                <div className="text-sm text-gray-500">
                    Last updated: {lastUpdated.toLocaleTimeString()}
                </div>
            </div>

            <div className="bg-white p-4 rounded-lg shadow">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-lg font-semibold">Filters</h2>
                    <button
                        onClick={clearFilters}
                        className="px-3 py-1 bg-gray-200 text-gray-700 rounded text-sm hover:bg-gray-300"
                    >
                        Clear Filters
                    </button>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Domain</label>
                        <select
                            className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md"
                            value={filters.domain || ''}
                            onChange={(e) => handleFilterChange('domain', e.target.value || null)}
                        >
                            <option value="">All Domains</option>
                            {availableFilters.domains.map(domain => (
                                <option key={domain} value={domain}>{domain}</option>
                            ))}
                        </select>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Environment</label>
                        <select
                            className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md"
                            value={filters.env || ''}
                            onChange={(e) => handleFilterChange('env', e.target.value || null)}
                        >
                            <option value="">All Environments</option>
                            {availableFilters.environments.map(env => (
                                <option key={env} value={env}>{env}</option>
                            ))}
                        </select>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">From</label>
                        <input
                            type="datetime-local"
                            className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                            onChange={(e) => handleFilterChange('from', e.target.value ? new Date(e.target.value) : null)}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">To</label>
                        <input
                            type="datetime-local"
                            className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                            onChange={(e) => handleFilterChange('to', e.target.value ? new Date(e.target.value) : null)}
                        />
                    </div>
                </div>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <MetricCard
                    title="Total Requests"
                    value={summary?.totalRequests || 0}
                    color="bg-blue-500"
                    loading={loading}
                />
                <MetricCard
                    title="Success Rate"
                    value={`${summary?.averageSuccessRate?.toFixed(1) || 0}%`}
                    color="bg-green-500"
                    loading={loading}
                />
                <MetricCard
                    title="Avg Duration"
                    value={`${summary?.averageDuration?.toFixed(0) || 0}ms`}
                    color="bg-purple-500"
                    loading={loading}
                />
                <MetricCard
                    title="Error Rate"
                    value={`${summary?.averageErrorRate?.toFixed(1) || 0}%`}
                    color="bg-red-500"
                    loading={loading}
                />
            </div>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                <ChartCard title="HTTP Status Distribution" loading={isInitialLoad && loading} refreshing={isRefreshing}>
                    <ResponsiveContainer width="100%" height={300}>
                        <PieChart>
                            <Pie
                                data={statusData}
                                dataKey="value"
                                nameKey="label"
                                cx="50%"
                                cy="50%"
                                outerRadius={80}
                                fill="#8884d8"
                                label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                            >
                                {statusData.map((entry, index) => (
                                    <Cell key={`cell-${index}`} fill={getStatusColor(entry)} />
                                ))}
                            </Pie>
                            <Tooltip />
                            <Legend />
                        </PieChart>
                    </ResponsiveContainer>
                </ChartCard>
                <ChartCard title="HTTP Method Distribution" loading={isInitialLoad && loading} refreshing={isRefreshing}>
                    <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={methodData}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="label" />
                            <YAxis />
                            <Tooltip />
                            <Legend />
                            <Bar dataKey="value" fill="#8884d8" radius={[4, 4, 0, 0]}>
                                {methodData.map((entry, index) => (
                                    <Cell key={`cell-${index}`} fill={`hsl(${index * 90}, 70%, 50%)`} />
                                ))}
                            </Bar>
                        </BarChart>
                    </ResponsiveContainer>
                </ChartCard>
                <ChartCard title="Domain Distribution" loading={isInitialLoad && loading} refreshing={isRefreshing}>
                    <ResponsiveContainer width="100%" height={300}>
                        <PieChart>
                            <Pie
                                data={domainData}
                                dataKey="value"
                                nameKey="label"
                                cx="50%"
                                cy="50%"
                                outerRadius={80}
                                fill="#8884d8"
                                label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                            >
                                {domainData.map((entry, index) => (
                                    <Cell key={`cell-${index}`} fill={`hsl(${index * 60}, 70%, 50%)`} />
                                ))}
                            </Pie>
                            <Tooltip />
                            <Legend />
                        </PieChart>
                    </ResponsiveContainer>
                </ChartCard>
                <ChartCard title="Domain Distribution" loading={isInitialLoad && loading} refreshing={isRefreshing}>
                    <ResponsiveContainer width="100%" height={300}>
                        <PieChart>
                            <Pie
                                data={domainData}
                                dataKey="value"
                                nameKey="label"
                                cx="50%"
                                cy="50%"
                                outerRadius={80}
                                fill="#8884d8"
                                label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                            >
                                {domainData.map((entry, index) => (
                                    <Cell key={`cell-${index}`} fill={`hsl(${index * 60}, 70%, 50%)`} />
                                ))}
                            </Pie>
                            <Tooltip />
                            <Legend />
                        </PieChart>
                    </ResponsiveContainer>
                </ChartCard>


            </div>
        </div>
    );
};
const MetricCard = ({ title, value, color, icon, loading }) => (
    <div className={`${color} text-white rounded-xl p-6 shadow-lg ${loading ? 'opacity-70' : ''}`}>
        <div className="flex justify-between items-center">
            <div>
                <p className="text-sm opacity-80">{title}</p>
                <p className="text-2xl font-bold">{loading ? '...' : value}</p>
            </div>
            <span className="text-3xl">{icon}</span>
        </div>
    </div>
);
const ChartCard = ({ title, children, loading, refreshing }) => (
    <div className="bg-white p-6 rounded-xl shadow">
        <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-800">{title}</h3>
            {refreshing && !loading && (
                <div className="text-xs text-gray-500 animate-pulse">Updating…</div>
            )}
        </div>
        {loading ? (
            <div className="h-64 flex items-center justify-center">
                <div className="text-gray-500">Loading chart data...</div>
            </div>
        ) : (
            children
        )}
    </div>
);

export default Dashboard;