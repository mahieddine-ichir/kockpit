import React, {useEffect, useState} from 'react';
import {Edit, Check, X} from 'lucide-react';
import {AdjustmentsHorizontalIcon} from '@heroicons/react/24/outline';
import {useSearchParams} from "react-router-dom";
import {updateDynaConfig} from "../services/api.js";
import Stats from "../components/StatsComponent.jsx";

const KeyItem = ({keyItem, updateKeyValue}) => {
    const [editingKey, setEditingKey] = useState(null);
    const [value, setValue] = useState(keyItem.value);

    const saveValue = () => {
        updateKeyValue({
            key: keyItem.key,
            value: value,
            description: keyItem.description
        });
        setEditingKey(null);
    }

    return (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-all p-5 space-y-3">
            <div className="flex items-start justify-between gap-4">
                <div className="flex-1 min-w-0">
                    <span className="text-xs uppercase tracking-wide text-slate-400">Config Key</span>
                    <div className="font-mono text-slate-800 font-semibold break-all text-sm">
                        {keyItem.key}
                    </div>
                    {keyItem.description && (
                        <div className="text-xs text-slate-500 mt-1">
                            {keyItem.description}
                        </div>
                    )}
                </div>
                <div className="flex items-center gap-2 flex-shrink-0">
                    <span className="text-sm font-medium text-slate-700 px-3 py-1.5 bg-slate-100 rounded-lg">
                        {String(keyItem.value)}
                    </span>
                    {editingKey === keyItem.key ? (
                        <>
                            <button
                                onClick={saveValue}
                                className="p-2 border border-green-400 rounded-lg hover:bg-green-50 text-green-600"
                                title="Confirm"
                            >
                                <Check className="h-4 w-4" />
                            </button>
                            <button
                                onClick={() => {
                                    setEditingKey(null);
                                    setValue(keyItem.value);
                                }}
                                className="p-2 border border-red-300 rounded-lg hover:bg-red-50 text-red-500"
                                title="Cancel"
                            >
                                <X className="h-4 w-4" />
                            </button>
                        </>
                    ) : (
                        <button
                            onClick={() => {
                                setEditingKey(keyItem.key);
                                setValue(keyItem.value);
                            }}
                            className="p-2 border border-slate-300 rounded-lg hover:bg-slate-50"
                            title="Edit value"
                        >
                            <Edit className="h-4 w-4 text-slate-600" />
                        </button>
                    )}
                </div>
            </div>

            {editingKey === keyItem.key && (
                <div className="border-t border-slate-100 pt-3">
                    <label className="block text-xs text-slate-500 mb-1">Value</label>
                    <input
                        type="text"
                        value={value}
                        onChange={e => setValue(e.target.value)}
                        className="w-full border border-slate-300 rounded-lg px-2 py-1 text-sm"
                        placeholder="Enter value..."
                    />
                </div>
            )}
        </div>
    )
}

export default function ConfigManager({domain, env, config}) {
    const [searchParams] = useSearchParams();
    const serviceId = searchParams.get('service') || null;
    const [keys, setKeys] = useState([]);
    const [serviceName, setServiceName] = useState('Dyna Config');

    useEffect(() => {
        if (config['services']) {
            let service = config['services']
                .find(service => service.type === 'dyna-config' && service.id === serviceId);
            if (service) {
                if (service.config && service.config.keys) {
                    setKeys([...service.config.keys]);
                }
                setServiceName(service.name || service.label || 'Dyna Config');
            }
        }
    }, [domain, env, config, serviceId]);

    const updateKeyValue = (key) => {
        updateDynaConfig(domain, env, serviceId, key).then(data => {
            setKeys(keys.map(f => f.key === data.key ? { ...f, value: key.value } : f))
        });
    };

    const totalKeys = keys.length;
    const modifiedKeys = 0; // TODO: implement modification tracking

    return (
        <div className="min-h-screen bg-slate-50">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-slate-800 mb-2 flex items-center gap-3">
                        <AdjustmentsHorizontalIcon className="h-8 w-8 text-blue-600" />
                        {serviceName}
                    </h1>
                    <p className="text-slate-600">
                        Dyna Config
                    </p>
                </div>

                {/* Stats Cards */}
                <Stats keys={keys} totalKeys={totalKeys} modifiedKeys={modifiedKeys} />

                {/* Config Keys */}
                <div className="divide-y divide-gray-200">
                    {keys.map((key, keyIdx) => (
                        <KeyItem key={keyIdx} keyItem={key} updateKeyValue={updateKeyValue} />
                    ))}
                </div>
            </div>
        </div>
    );
}
