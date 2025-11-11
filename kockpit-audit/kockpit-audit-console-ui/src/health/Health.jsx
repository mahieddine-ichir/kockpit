import React, { useState, useEffect, useMemo } from 'react';
import {
    CheckCircle, XCircle, AlertCircle, Activity,
    RefreshCw, Clock, Server, Wifi, WifiOff
} from 'lucide-react';
import {getHealth} from "../services/api.js";

const HealthIndicatorsDashboard = ({domain, env}) => {
    const [indicators, setIndicators] = useState([]);
    const [currentTime, setCurrentTime] = useState(Date.now());
    const [loading, setLoading] = useState(false);
    const [autoRefresh, setAutoRefresh] = useState(true);

    // Données simulées - Remplacez par votre appel API
    //const [sampleData, setSampleData] = useState([]);

    // Charger les données initiales
    useEffect(() => {
        loadIndicators();
    }, []);

    // Mettre à jour l'heure actuelle toutes les secondes
    useEffect(() => {
        const interval = setInterval(() => {
            setCurrentTime(Date.now());
        }, 1000);
        return () => clearInterval(interval);
    }, []);

    // Auto-refresh toutes les 30 secondes si activé
    useEffect(() => {
        if (autoRefresh) {
            const interval = setInterval(() => {
                loadIndicators();
            }, 30000);
            return () => clearInterval(interval);
        }
    }, [autoRefresh]);

    const loadIndicators = async () => {
        setLoading(true);
        try {
            // Simuler un appel API
            //await new Promise(resolve => setTimeout(resolve, 500));
            getHealth(domain, env).then(data => {
                setIndicators(data);
            })

            // En production, remplacez par :
            // const response = await fetch('/api/health-indicators');
            // const data = await response.json();
            // setIndicators(data);


        } catch (error) {
            console.error('Error loading indicators:', error);
        } finally {
            setLoading(false);
        }
    };

    // Calculer le statut pour chaque indicateur
    const getStatus = (creationDate) => {
        const ageInMinutes = (currentTime - creationDate) / (1000 * 60);

        if (ageInMinutes < 3) {
            return {
                status: 'healthy',
                color: 'green',
                bgColor: 'bg-green-50',
                textColor: 'text-green-700',
                borderColor: 'border-green-200',
                icon: CheckCircle,
                iconColor: 'text-green-500'
            };
        } else if (ageInMinutes < 5) {
            return {
                status: 'warning',
                color: 'yellow',
                bgColor: 'bg-yellow-50',
                textColor: 'text-yellow-700',
                borderColor: 'border-yellow-200',
                icon: AlertCircle,
                iconColor: 'text-yellow-500'
            };
        } else {
            return {
                status: 'critical',
                color: 'red',
                bgColor: 'bg-red-50',
                textColor: 'text-red-700',
                borderColor: 'border-red-200',
                icon: XCircle,
                iconColor: 'text-red-500'
            };
        }
    };

    // Formater le temps écoulé
    const formatTimeAgo = (timestamp) => {
        const seconds = Math.floor((currentTime - timestamp) / 1000);

        if (seconds < 60) return `${seconds}s ago`;
        const minutes = Math.floor(seconds / 60);
        if (minutes < 60) return `${minutes}m ago`;
        const hours = Math.floor(minutes / 60);
        if (hours < 24) return `${hours}h ago`;
        const days = Math.floor(hours / 24);
        return `${days}d ago`;
    };

    // Formater la date complète
    const formatDateTime = (timestamp) => {
        return new Date(timestamp).toLocaleString('en-US', {
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    };

    // Calculer les statistiques
    const stats = useMemo(() => {
        const healthy = indicators.filter(i => getStatus(i.creationDate).status === 'healthy').length;
        const warning = indicators.filter(i => getStatus(i.creationDate).status === 'warning').length;
        const critical = indicators.filter(i => getStatus(i.creationDate).status === 'critical').length;

        return {
            total: indicators.length,
            healthy,
            warning,
            critical,
            healthRate: indicators.length > 0 ? ((healthy / indicators.length) * 100).toFixed(1) : 0
        };
    }, [indicators, currentTime]);

    // Badge pour l'environnement
    const EnvBadge = ({ env }) => {
        const colors = {
            production: 'bg-red-100 text-red-800 border-red-200',
            staging: 'bg-yellow-100 text-yellow-800 border-yellow-200',
            development: 'bg-blue-100 text-blue-800 border-blue-200',
            local: 'bg-gray-100 text-gray-800 border-gray-200'
        };

        return (
            <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border ${colors[env] || colors.local}`}>
        {env}
      </span>
        );
    };

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="mb-6">
                    <div className="flex justify-between items-center">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-2">
                                <Activity className="w-8 h-8 text-blue-600" />
                                Health Indicators Monitor
                            </h1>
                            <p className="text-gray-500 mt-1">Real-time system health monitoring</p>
                        </div>
                        <div className="flex items-center gap-3">
                            <label className="flex items-center gap-2 cursor-pointer">
                                <input
                                    type="checkbox"
                                    checked={autoRefresh}
                                    onChange={(e) => setAutoRefresh(e.target.checked)}
                                    className="w-4 h-4 text-blue-600 rounded"
                                />
                                <span className="text-sm text-gray-700">Auto-refresh (30s)</span>
                            </label>
                            <button
                                onClick={loadIndicators}
                                disabled={loading}
                                className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition disabled:opacity-50"
                            >
                                <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
                                Refresh
                            </button>
                        </div>
                    </div>
                </div>

                {/* Statistics Cards */}
                <div className="grid grid-cols-1 md:grid-cols-5 gap-4 mb-6">
                    <div className="bg-white rounded-lg shadow p-4 border border-gray-200">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-gray-500">Total</p>
                                <p className="text-2xl font-semibold text-gray-900">{stats.total}</p>
                            </div>
                            <Server className="w-8 h-8 text-gray-400" />
                        </div>
                    </div>

                    <div className="bg-green-50 rounded-lg shadow p-4 border border-green-200">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-green-600">Healthy</p>
                                <p className="text-2xl font-semibold text-green-700">{stats.healthy}</p>
                            </div>
                            <CheckCircle className="w-8 h-8 text-green-500" />
                        </div>
                    </div>

                    <div className="bg-yellow-50 rounded-lg shadow p-4 border border-yellow-200">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-yellow-600">Warning</p>
                                <p className="text-2xl font-semibold text-yellow-700">{stats.warning}</p>
                            </div>
                            <AlertCircle className="w-8 h-8 text-yellow-500" />
                        </div>
                    </div>

                    <div className="bg-red-50 rounded-lg shadow p-4 border border-red-200">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-red-600">Critical</p>
                                <p className="text-2xl font-semibold text-red-700">{stats.critical}</p>
                            </div>
                            <XCircle className="w-8 h-8 text-red-500" />
                        </div>
                    </div>

                    <div className="bg-blue-50 rounded-lg shadow p-4 border border-blue-200">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-blue-600">Health Rate</p>
                                <p className="text-2xl font-semibold text-blue-700">{stats.healthRate}%</p>
                            </div>
                            <Activity className="w-8 h-8 text-blue-500" />
                        </div>
                    </div>
                </div>

                {/* Table */}
                <div className="bg-white rounded-lg shadow overflow-hidden">
                    <div className="overflow-x-auto">
                        <table className="min-w-full divide-y divide-gray-200">
                            <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Status
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Instance ID
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Application
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Domain
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Environment
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Type
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Last Heartbeat
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Age
                                </th>
                            </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                            {indicators.map((indicator) => {
                                const status = getStatus(indicator.creationDate);
                                const StatusIcon = status.icon;

                                return (
                                    <tr key={`${indicator.id}-${indicator.creationDate}`}
                                        className={`hover:bg-gray-50 transition ${status.bgColor}`}>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="flex items-center">
                                                <StatusIcon className={`w-5 h-5 ${status.iconColor}`} />
                                                <span className={`ml-2 text-sm font-medium ${status.textColor}`}>
                            {status.status.toUpperCase()}
                          </span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="flex items-center">
                                                <Server className="w-4 h-4 text-gray-400 mr-2" />
                                                <span className="text-sm font-medium text-gray-900">{indicator.id}</span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                        <span className="text-sm text-gray-900">
                          {indicator.body?.appId || indicator.appId || 'N/A'}
                        </span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <span className="text-sm text-gray-600">{indicator.domain}</span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <EnvBadge env={indicator.env} />
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <span className="text-sm text-gray-600">{indicator.type}</span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="flex items-center text-sm text-gray-600">
                                                <Clock className="w-3 h-3 mr-1" />
                                                {formatDateTime(indicator.creationDate)}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`text-sm font-medium ${status.textColor}`}>
                          {formatTimeAgo(indicator.creationDate)}
                        </span>
                                        </td>
                                    </tr>
                                );
                            })}
                            </tbody>
                        </table>

                        {indicators.length === 0 && (
                            <div className="text-center py-12">
                                <WifiOff className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                                <p className="text-gray-500">No health indicators found</p>
                                <button
                                    onClick={loadIndicators}
                                    className="mt-4 text-blue-600 hover:text-blue-700 font-medium"
                                >
                                    Load indicators
                                </button>
                            </div>
                        )}
                    </div>
                </div>

                {/* Legend */}
                <div className="mt-6 bg-white rounded-lg shadow p-4">
                    <h3 className="text-sm font-medium text-gray-700 mb-3">Status Legend</h3>
                    <div className="flex flex-wrap gap-6">
                        <div className="flex items-center gap-2">
                            <CheckCircle className="w-5 h-5 text-green-500" />
                            <span className="text-sm text-gray-600">
                <span className="font-medium">Healthy:</span> Last heartbeat &lt; 3 minutes
              </span>
                        </div>
                        <div className="flex items-center gap-2">
                            <AlertCircle className="w-5 h-5 text-yellow-500" />
                            <span className="text-sm text-gray-600">
                <span className="font-medium">Warning:</span> Last heartbeat 3-5 minutes
              </span>
                        </div>
                        <div className="flex items-center gap-2">
                            <XCircle className="w-5 h-5 text-red-500" />
                            <span className="text-sm text-gray-600">
                <span className="font-medium">Critical:</span> Last heartbeat &gt; 5 minutes
              </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default HealthIndicatorsDashboard;
