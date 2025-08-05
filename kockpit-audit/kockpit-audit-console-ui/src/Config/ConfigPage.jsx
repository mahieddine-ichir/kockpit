import React, { useEffect, useState } from 'react';
import { getConfig } from '../services/api';
import ReactJson from 'react-json-view';

const ConfigPage = () => {
    const [config, setConfig] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        getConfig()
            .then(data => {
                setConfig(data);
                setLoading(false);
            })
            .catch(err => {
                setError('Failed to fetch config');
                setLoading(false);
                console.error('Error fetching config:', err.message)
            });
    }, []);

    if (loading) return <div className="p-6 text-slate-200">Loading config...</div>;
    if (error) return <div className="p-6 text-red-400">{error}</div>;

    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold mb-4 text-slate-100">Config</h1>
            <div className="bg-slate-800 p-4 rounded-lg overflow-x-auto">
                <ReactJson src={config} theme="ocean" collapsed={2} displayDataTypes={false} enableClipboard={true} />
            </div>
        </div>
    );
};

export default ConfigPage;