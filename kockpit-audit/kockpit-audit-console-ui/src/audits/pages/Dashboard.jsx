import React,{ useEffect, useState, useCallback } from 'react';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend, AreaChart, Area, LineChart, Line } from 'recharts';
import { ClipboardDocumentIcon, EyeIcon, CheckIcon, ChartPieIcon, ClockIcon, ExclamationTriangleIcon, ArrowTrendingUpIcon, ArrowPathIcon, FunnelIcon } from '@heroicons/react/24/outline';
import { fetchDashboardSummary,  fetchDistribution,  fetchAvailableFilters} from "../../services/api.js"

const STATUS_COLORS = {
    '200': { primary: '#10b981', secondary: '#065f46', gradient: 'from-emerald-500 to-green-600' },
    '201': { primary: '#06d6a0', secondary: '#064e3b', gradient: 'from-teal-500 to-emerald-600' },
    '204': { primary: '#84cc16', secondary: '#365314', gradient: 'from-lime-500 to-green-600' },
    '400': { primary: '#f59e0b', secondary: '#92400e', gradient: 'from-amber-500 to-orange-600' },
    '401': { primary: '#ef4444', secondary: '#991b1b', gradient: 'from-red-500 to-red-600' },
    '403': { primary: '#dc2626', secondary: '#7f1d1d', gradient: 'from-red-600 to-red-700' },
    '404': { primary: '#f97316', secondary: '#9a3412', gradient: 'from-orange-500 to-red-600' },
    '500': { primary: '#7c2d12', secondary: '#451a03', gradient: 'from-orange-800 to-red-900' },
    '502': { primary: '#991b1b', secondary: '#450a0a', gradient: 'from-red-800 to-red-900' },
    '503': { primary: '#dc2626', secondary: '#7f1d1d', gradient: 'from-red-600 to-red-800' },
};

const CHART_COLORS = ['#8b5cf6', '#06b6d4', '#10b981', '#f59e0b', '#ef4444', '#ec4899', '#6366f1', '#84cc16'];

const getStatusColor = (entry) => {
    const key = String(entry?.label ?? entry?.name ?? entry?.status ?? entry?.code ?? '')
        .replace('HTTP ', '');
    return STATUS_COLORS[key]?.primary || '#6B7280';
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
    const [activeFilters, setActiveFilters] = useState(0);

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

    useEffect(() => {
        const count = Object.values(filters).filter(v => v !== null && v !== undefined && v !== '').length;
        setActiveFilters(count);
    }, [filters]);

    const handleFilterChange = (key, value) => {
        setFilters(prev => ({ ...prev, [key]: value }));
    };

    const clearFilters = () => {
        setFilters({});
    };

    if (loading && !summary) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-indigo-600 mx-auto mb-4"></div>
                    <p className="text-lg text-slate-600 font-medium">Loading your dashboard...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100">
            <div className="bg-white/80 backdrop-blur-sm border-b border-white/20 sticky top-0 z-10">
                <div className="max-w-7xl mx-auto px-6 py-4">
                    <div className="flex justify-between items-center">
                        <div className="flex items-center space-x-4">
                            <div className="p-2 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-xl">
                                <ChartPieIcon className="h-8 w-8 text-white" />
                            </div>
                            <div>
                                <h1 className="text-3xl font-bold bg-gradient-to-r from-slate-800 to-slate-600 bg-clip-text text-transparent">
                                    Analytics Dashboard
                                </h1>
                                <p className="text-slate-500 text-sm">Real-time monitoring and insights</p>
                            </div>
                        </div>
                        <div className="flex items-center space-x-4">
                            {isRefreshing && (
                                <div className="flex items-center space-x-2 text-indigo-600">
                                    <ArrowPathIcon className="h-4 w-4 animate-spin" />
                                    <span className="text-sm font-medium">Refreshing...</span>
                                </div>
                            )}
                            <div className="text-right">
                                <p className="text-sm text-slate-600 font-medium">Last updated</p>
                                <p className="text-xs text-slate-500">{lastUpdated.toLocaleString()}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="max-w-7xl mx-auto px-6 py-8 space-y-8">
                <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-lg border border-white/20 p-6">
                    <div className="flex justify-between items-center mb-6">
                        <div className="flex items-center space-x-3">
                            <FunnelIcon className="h-6 w-6 text-indigo-600" />
                            <h2 className="text-xl font-semibold text-slate-800">Filters</h2>
                            {activeFilters > 0 && (
                                <span className="bg-indigo-100 text-indigo-800 text-xs font-semibold px-2.5 py-0.5 rounded-full">
                  {activeFilters} active
                </span>
                            )}
                        </div>
                        <button
                            onClick={clearFilters}
                            className="px-4 py-2 bg-gradient-to-r from-slate-100 to-slate-200 hover:from-slate-200 hover:to-slate-300 text-slate-700 rounded-xl text-sm font-medium transition-all duration-200 shadow-sm hover:shadow-md"
                        >
                            Clear All
                        </button>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                        <FilterSelect
                            label="Domain"
                            value={filters.domain || ''}
                            options={availableFilters.domains}
                            onChange={(value) => handleFilterChange('domain', value || null)}
                            placeholder="All Domains"
                        />
                        <FilterSelect
                            label="Environment"
                            value={filters.env || ''}
                            options={availableFilters.environments}
                            onChange={(value) => handleFilterChange('env', value || null)}
                            placeholder="All Environments"
                        />
                        <FilterDateTime
                            label="From"
                            onChange={(value) => handleFilterChange('from', value ? new Date(value) : null)}
                        />
                        <FilterDateTime
                            label="To"
                            onChange={(value) => handleFilterChange('to', value ? new Date(value) : null)}
                        />
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    <MetricCard
                        title="Total Requests"
                        value={summary?.totalRequests?.toLocaleString() || '0'}
                        icon={<ClipboardDocumentIcon className="h-8 w-8" />}
                        gradient="from-blue-500 to-blue-600"
                        loading={loading}
                    />
                    <MetricCard
                        title="Success Rate"
                        value={`${summary?.averageSuccessRate?.toFixed(1) || 0}%`}
                        icon={<CheckIcon className="h-8 w-8" />}
                        gradient="from-emerald-500 to-green-600"
                        loading={loading}
                    />
                    <MetricCard
                        title="Avg Response Time"
                        value={`${summary?.averageDuration?.toFixed(0) || 0}ms`}
                        icon={<ClockIcon className="h-8 w-8" />}
                        gradient="from-purple-500 to-purple-600"
                        loading={loading}
                    />
                    <MetricCard
                        title="Error Rate"
                        value={`${summary?.averageErrorRate?.toFixed(1) || 0}%`}
                        icon={<ExclamationTriangleIcon className="h-8 w-8" />}
                        gradient="from-red-500 to-red-600"
                        loading={loading}
                    />
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                    <ChartCard title="HTTP Status Distribution" icon={<ChartPieIcon className="h-5 w-5" />} loading={isInitialLoad && loading} refreshing={isRefreshing}>
                        <ResponsiveContainer width="100%" height={320}>
                            <PieChart>
                                <defs>
                                    {statusData.map((entry, index) => (
                                        <linearGradient key={`gradient-${index}`} id={`statusGradient-${index}`} x1="0%" y1="0%" x2="100%" y2="100%">
                                            <stop offset="0%" stopColor={getStatusColor(entry)} />
                                            <stop offset="100%" stopColor={getStatusColor(entry) + '80'} />
                                        </linearGradient>
                                    ))}
                                </defs>
                                <Pie
                                    data={statusData}
                                    dataKey="value"
                                    nameKey="label"
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={60}
                                    outerRadius={120}
                                    paddingAngle={2}
                                    label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                                >
                                    {statusData.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={`url(#statusGradient-${index})`} />
                                    ))}
                                </Pie>
                                <Tooltip contentStyle={{
                                    backgroundColor: 'rgba(255, 255, 255, 0.95)',
                                    border: 'none',
                                    borderRadius: '12px',
                                    boxShadow: '0 10px 25px rgba(0, 0, 0, 0.1)'
                                }} />
                            </PieChart>
                        </ResponsiveContainer>
                    </ChartCard>

                    <ChartCard title="HTTP Methods" icon={<ArrowTrendingUpIcon className="h-5 w-5" />} loading={isInitialLoad && loading} refreshing={isRefreshing}>
                        <ResponsiveContainer width="100%" height={320}>
                            <BarChart data={methodData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                                <defs>
                                    {methodData.map((_, index) => (
                                        <linearGradient key={`methodGradient-${index}`} id={`methodGradient-${index}`} x1="0%" y1="0%" x2="0%" y2="100%">
                                            <stop offset="0%" stopColor={CHART_COLORS[index % CHART_COLORS.length]} />
                                            <stop offset="100%" stopColor={CHART_COLORS[index % CHART_COLORS.length] + '60'} />
                                        </linearGradient>
                                    ))}
                                </defs>
                                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                                <XAxis dataKey="label" tick={{ fill: '#64748b', fontSize: 12 }} />
                                <YAxis tick={{ fill: '#64748b', fontSize: 12 }} />
                                <Tooltip contentStyle={{
                                    backgroundColor: 'rgba(255, 255, 255, 0.95)',
                                    border: 'none',
                                    borderRadius: '12px',
                                    boxShadow: '0 10px 25px rgba(0, 0, 0, 0.1)'
                                }} />
                                <Bar dataKey="value" radius={[6, 6, 0, 0]}>
                                    {methodData.map((_, index) => (
                                        <Cell key={`cell-${index}`} fill={`url(#methodGradient-${index})`} />
                                    ))}
                                </Bar>
                            </BarChart>
                        </ResponsiveContainer>
                    </ChartCard>

                    <ChartCard title="Domain Distribution" icon={<EyeIcon className="h-5 w-5" />} loading={isInitialLoad && loading} refreshing={isRefreshing}>
                        <ResponsiveContainer width="100%" height={320}>
                            <PieChart>
                                <defs>
                                    {domainData.map((_, index) => (
                                        <linearGradient key={`domainGradient-${index}`} id={`domainGradient-${index}`} x1="0%" y1="0%" x2="100%" y2="100%">
                                            <stop offset="0%" stopColor={CHART_COLORS[index % CHART_COLORS.length]} />
                                            <stop offset="100%" stopColor={CHART_COLORS[index % CHART_COLORS.length] + '70'} />
                                        </linearGradient>
                                    ))}
                                </defs>
                                <Pie
                                    data={domainData}
                                    dataKey="value"
                                    nameKey="label"
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={50}
                                    outerRadius={120}
                                    paddingAngle={3}
                                    label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                                >
                                    {domainData.map((_, index) => (
                                        <Cell key={`cell-${index}`} fill={`url(#domainGradient-${index})`} />
                                    ))}
                                </Pie>
                                <Tooltip contentStyle={{
                                    backgroundColor: 'rgba(255, 255, 255, 0.95)',
                                    border: 'none',
                                    borderRadius: '12px',
                                    boxShadow: '0 10px 25px rgba(0, 0, 0, 0.1)'
                                }} />
                            </PieChart>
                        </ResponsiveContainer>
                    </ChartCard>

                    <ChartCard title="Performance" icon={<ArrowTrendingUpIcon className="h-5 w-5" />} loading={isInitialLoad && loading} refreshing={isRefreshing}>
                        <ResponsiveContainer width="100%" height={320}>
                            <AreaChart data={statusData.slice(0, 7)} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                                <defs>
                                    <linearGradient id="performanceGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                                        <stop offset="0%" stopColor="#8b5cf6" stopOpacity={0.8}/>
                                        <stop offset="100%" stopColor="#8b5cf6" stopOpacity={0.1}/>
                                    </linearGradient>
                                </defs>
                                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                                <XAxis dataKey="label" tick={{ fill: '#64748b', fontSize: 12 }} />
                                <YAxis tick={{ fill: '#64748b', fontSize: 12 }} />
                                <Tooltip contentStyle={{
                                    backgroundColor: 'rgba(255, 255, 255, 0.95)',
                                    border: 'none',
                                    borderRadius: '12px',
                                    boxShadow: '0 10px 25px rgba(0, 0, 0, 0.1)'
                                }} />
                                <Area type="monotone" dataKey="value" stroke="#8b5cf6" strokeWidth={2} fill="url(#performanceGradient)" />
                            </AreaChart>
                        </ResponsiveContainer>
                    </ChartCard>
                </div>
            </div>
        </div>
    );
};

const MetricCard = ({ title, value, icon, gradient, loading, trend }) => (
    <div className={`bg-gradient-to-br ${gradient} rounded-2xl p-6 text-white shadow-lg hover:shadow-xl transition-all duration-300 transform hover:-translate-y-1 ${loading ? 'animate-pulse' : ''}`}>
        <div className="flex items-center justify-between mb-4">
            <div className="p-3 bg-white/20 rounded-xl backdrop-blur-sm">
                {icon}
            </div>
            {trend && (
                <div className="text-right">
          <span className="text-xs bg-white/20 px-2 py-1 rounded-full font-medium">
            {trend}
          </span>
                </div>
            )}
        </div>
        <div>
            <p className="text-white/80 text-sm font-medium mb-1">{title}</p>
            <p className="text-3xl font-bold">
                {loading ? (
                    <div className="h-8 bg-white/20 rounded animate-pulse"></div>
                ) : (
                    value
                )}
            </p>
        </div>
    </div>
);

const ChartCard = ({ title, icon, children, loading, refreshing }) => (
    <div className="bg-white/70 backdrop-blur-sm rounded-2xl shadow-lg border border-white/20 p-6 hover:shadow-xl transition-all duration-300">
        <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-3">
                <div className="p-2 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-lg text-white">
                    {icon}
                </div>
                <h3 className="text-lg font-semibold text-slate-800">{title}</h3>
            </div>
            {refreshing && !loading && (
                <div className="flex items-center space-x-2 text-indigo-600">
                    <div className="w-2 h-2 bg-indigo-600 rounded-full animate-pulse"></div>
                    <span className="text-xs font-medium">Live</span>
                </div>
            )}
        </div>
        {loading ? (
            <div className="h-80 flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto mb-4"></div>
                    <p className="text-slate-500 text-sm">Loading chart data...</p>
                </div>
            </div>
        ) : (
            children
        )}
    </div>
);

const FilterSelect = ({ label, value, options, onChange, placeholder }) => (
    <div className="space-y-2">
        <label className="block text-sm font-semibold text-slate-700">{label}</label>
        <select
            className="w-full px-4 py-3 bg-white/80 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all duration-200 text-sm font-medium text-slate-700"
            value={value}
            onChange={(e) => onChange(e.target.value)}
        >
            <option value="">{placeholder}</option>
            {options.map(option => (
                <option key={option} value={option}>{option}</option>
            ))}
        </select>
    </div>
);
const FilterDateTime = ({ label, onChange }) => (
    <div className="space-y-2">
        <label className="block text-sm font-semibold text-slate-700">{label}</label>
        <input
            type="datetime-local"
            className="w-full px-4 py-3 bg-white/80 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all duration-200 text-sm font-medium text-slate-700"
            onChange={(e) => onChange(e.target.value)}
        />
    </div>
);

export default Dashboard;