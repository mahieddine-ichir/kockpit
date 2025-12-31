import React from 'react';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, ResponsiveContainer } from 'recharts';
import { ClipboardDocumentIcon, EyeIcon, CheckIcon, ChartPieIcon } from '@heroicons/react/24/outline';

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

const Dashboard = ({ audits, getHttStatus, getMethod }) => {
    const statusCounts = audits.reduce((acc, audit) => {
        const status = getHttStatus(audit);
        acc[status] = (acc[status] || 0) + 1;
        return acc;
    }, {});

    const statusData = Object.entries(statusCounts).map(([status, count]) => ({
        name: status,
        value: count,
        percentage: ((count / audits.length) * 100).toFixed(1)
    }));

    const methodCounts = audits.reduce((acc, audit) => {
        const method = getMethod(audit);
        acc[method] = (acc[method] || 0) + 1;
        return acc;
    }, {});

    const methodData = Object.entries(methodCounts).map(([method, count]) => ({
        name: method,
        value: count,
        percentage: ((count / audits.length) * 100).toFixed(1)
    }));

    const domainEnvCounts = audits.reduce((acc, audit) => {
        const domainEnv = `${audit.domain}/${audit.env}`;
        acc[domainEnv] = (acc[domainEnv] || 0) + 1;
        return acc;
    }, {});

    const domainEnvData = Object.entries(domainEnvCounts).map(([domainEnv, count]) => ({
        name: domainEnv,
        value: count,
        percentage: ((count / audits.length) * 100).toFixed(1)
    }));

    const durationByStatus = audits.reduce((acc, audit) => {
        const status = getHttStatus(audit);
        const duration = audit.end - audit.start;
        if (!acc[status]) {
            acc[status] = { total: 0, count: 0 };
        }
        acc[status].total += duration;
        acc[status].count += 1;
        return acc;
    }, {});

    const avgDurationData = Object.entries(durationByStatus).map(([status, data]) => ({
        status,
        avgDuration: Math.round(data.total / data.count).toFixed(2)
    }));

    const CustomTooltip = ({ active, payload, label }) => {
        if (active && payload && payload.length) {
            const data = payload[0].payload;
            return (
                <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg">
                    <p className="font-medium">{`${data.name}: ${data.value} (${data.percentage}%)`}</p>
                </div>
            );
        }
        return null;
    };

    const renderCustomizedLabel = ({ cx, cy, midAngle, innerRadius, outerRadius, percent }) => {
        if (percent < 0.05) return null;
        const RADIAN = Math.PI / 180;
        const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
        const x = cx + radius * Math.cos(-midAngle * RADIAN);
        const y = cy + radius * Math.sin(-midAngle * RADIAN);
        return (
            <text
                x={x}
                y={y}
                fill="white"
                textAnchor={x > cx ? 'start' : 'end'}
                dominantBaseline="central"
                fontSize={12}
                fontWeight="bold"
            >
                {`${(percent * 100).toFixed(0)}%`}
            </text>
        );
    };

    return (
        <div className="space-y-8">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-2xl p-6 text-white">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-blue-100 text-sm font-medium">Total Requests</p>
                            <p className="text-3xl font-bold">{audits.length.toLocaleString()}</p>
                        </div>
                        <div className="bg-blue-400/30 rounded-lg p-3">
                            <ChartPieIcon className="h-8 w-8" />
                        </div>
                    </div>
                </div>
                <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-2xl p-6 text-white">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-green-100 text-sm font-medium">Success Rate</p>
                            <p className="text-3xl font-bold">
                                {((statusData.filter(s => s.name >= 200 && s.name < 300).reduce((sum, s) => sum + s.value, 0) / audits.length) * 100).toFixed(1)}%
                            </p>
                        </div>
                        <div className="bg-green-400/30 rounded-lg p-3">
                            <CheckIcon className="h-8 w-8" />
                        </div>
                    </div>
                </div>
                <div className="bg-gradient-to-br from-amber-500 to-amber-600 rounded-2xl p-6 text-white">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-amber-100 text-sm font-medium">Avg Duration</p>
                            <p className="text-3xl font-bold">
                                {Math.round(audits.reduce((sum, audit) => sum + (audit.end - audit.start), 0) / audits.length).toFixed(2)} ms
                            </p>
                        </div>
                        <div className="bg-amber-400/30 rounded-lg p-3">
                            <ClipboardDocumentIcon className="h-8 w-8" />
                        </div>
                    </div>
                </div>
                <div className="bg-gradient-to-br from-purple-500 to-purple-600 rounded-2xl p-6 text-white">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-purple-100 text-sm font-medium">Error Rate</p>
                            <p className="text-3xl font-bold">
                                {((statusData.filter(s => s.name >= 400).reduce((sum, s) => sum + s.value, 0) / audits.length) * 100).toFixed(1)}%
                            </p>
                        </div>
                        <div className="bg-purple-400/30 rounded-lg p-3">
                            <EyeIcon className="h-8 w-8" />
                        </div>
                    </div>
                </div>
            </div>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                <div className="bg-white rounded-2xl shadow-lg p-8 border border-slate-200">
                    <h3 className="text-xl font-bold text-slate-800 mb-6 flex items-center">
                        <ChartPieIcon className="h-6 w-6 mr-2 text-blue-600" />
                        HTTP Status Distribution
                    </h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <PieChart>
                            <Pie
                                data={statusData}
                                cx="50%"
                                cy="50%"
                                labelLine={false}
                                label={renderCustomizedLabel}
                                outerRadius={100}
                                fill="#8884d8"
                                dataKey="value"
                                stroke="#fff"
                                strokeWidth={2}
                            >
                                {statusData.map((entry, index) => (
                                    <Cell key={`cell-status-${index}`} fill={COLORS[entry.name] || '#6b7280'} />
                                ))}
                            </Pie>
                            <RechartsTooltip content={<CustomTooltip />} />
                        </PieChart>
                    </ResponsiveContainer>
                    <div className="mt-6 grid grid-cols-2 gap-2">
                        {statusData.map((entry, index) => (
                            <div key={entry.name} className="flex items-center space-x-2">
                                <div
                                    className="w-3 h-3 rounded-full"
                                    style={{ backgroundColor: COLORS[entry.name] || '#6b7280' }}
                                />
                                <span className="text-sm text-slate-600">
                  {entry.name}: {entry.value} ({entry.percentage}%)
                </span>
                            </div>
                        ))}
                    </div>
                </div>
                <div className="bg-white rounded-2xl shadow-lg p-8 border border-slate-200">
                    <h3 className="text-xl font-bold text-slate-800 mb-6 flex items-center">
                        <ChartPieIcon className="h-6 w-6 mr-2 text-green-600" />
                        HTTP Method Distribution
                    </h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={methodData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                            <XAxis dataKey="name" tick={{ fontSize: 12 }} stroke="#64748b" />
                            <YAxis tick={{ fontSize: 12 }} stroke="#64748b" />
                            <RechartsTooltip
                                contentStyle={{
                                    backgroundColor: 'white',
                                    border: '1px solid #e2e8f0',
                                    borderRadius: '8px',
                                    boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)'
                                }}
                            />
                            <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                                {methodData.map((entry, index) => (
                                    <Cell key={`cell-method-${index}`} fill={`hsl(${index * 60}, 70%, 50%)`} />
                                ))}
                            </Bar>
                        </BarChart>
                    </ResponsiveContainer>
                </div>
                <div className="bg-white rounded-2xl shadow-lg p-8 border border-slate-200">
                    <h3 className="text-xl font-bold text-slate-800 mb-6 flex items-center">
                        <ChartPieIcon className="h-6 w-6 mr-2 text-purple-600" />
                        Domain/Environment Distribution
                    </h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <PieChart>
                            <Pie
                                data={domainEnvData}
                                cx="50%"
                                cy="50%"
                                labelLine={false}
                                label={renderCustomizedLabel}
                                outerRadius={100}
                                fill="#8884d8"
                                dataKey="value"
                                stroke="#fff"
                                strokeWidth={2}
                            >
                                {domainEnvData.map((entry, index) => (
                                    <Cell key={`cell-domainenv-${index}`} fill={`hsl(${index * 120}, 60%, 60%)`} />
                                ))}
                            </Pie>
                            <RechartsTooltip content={<CustomTooltip />} />
                        </PieChart>
                    </ResponsiveContainer>
                </div>
                <div className="bg-white rounded-2xl shadow-lg p-8 border border-slate-200">
                    <h3 className="text-xl font-bold text-slate-800 mb-6 flex items-center">
                        <ChartPieIcon className="h-6 w-6 mr-2 text-indigo-600" />
                        Average Duration by Status
                    </h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={avgDurationData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                            <XAxis dataKey="status" tick={{ fontSize: 12 }} stroke="#64748b" />
                            <YAxis tick={{ fontSize: 12 }} stroke="#64748b" />
                            <RechartsTooltip
                                contentStyle={{
                                    backgroundColor: 'white',
                                    border: '1px solid #e2e8f0',
                                    borderRadius: '8px',
                                    boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)'
                                }}
                                formatter={(value) => [`${value}ms`, 'Avg Duration']}
                            />
                            <Bar dataKey="avgDuration" radius={[4, 4, 0, 0]}>
                                {avgDurationData.map((entry, index) => (
                                    <Cell key={`cell-avgduration-${index}`} fill={COLORS[entry.status] || '#6366f1'} />
                                ))}
                            </Bar>
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
