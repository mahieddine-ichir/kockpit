import React, {useEffect, useState} from 'react';
import {ChevronDown, Save, Settings} from 'lucide-react';
import {useSearchParams} from "react-router-dom";
import {updateDynaConfig} from "../services/api.js";

const Header = ({config, keys}) => {
    return (
        <div className="bg-gray-50 px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">{config.name}</h2>
            <p className="text-sm text-gray-500 mt-1">{keys.length} clés</p>
        </div>
    )
}

const KeyEditor = ({keyItem, keyIdx, updateKeyValue}) => {
    const [isExpanded, setIsExpanded] = useState(false);
    const [value, setValue] = useState(keyItem.value);
    const saveValue = (keyItem) => {
        updateKeyValue({
            key: keyItem.key,
            value: value,
            description: keyItem.description
        });
    }
    return (
        <div key={keyIdx} className="hover:bg-gray-50 transition space-y-2">
            <button
                onClick={() => setIsExpanded(!isExpanded)}
                className="w-full flex items-center justify-between px-6 py-4"
            >
                <div className="flex items-center gap-4 flex-1 text-left min-w-0">
                    <ChevronDown
                        size={18}
                        className={`text-gray-400 transition transform flex-shrink-0 ${
                            isExpanded ? 'rotate-180' : ''
                        }`}
                    />
                    <div className="min-w-0 flex-1">
                        <p className="font-medium text-gray-900 truncate">
                            {keyItem.key}
                        </p>
                        <p className="text-sm text-gray-600 truncate mt-1">
                            {keyItem.value}
                        </p>
                    </div>
                    {
                        /*
                        isModified && (
                        <div className="px-3 py-1 bg-yellow-100 text-yellow-700 rounded-full text-xs font-medium flex-shrink-0">
                            Modifiée
                        </div>
                        )
                         */
                    }
                </div>
            </button>

            {isExpanded && (
                <div className="px-6 py-4 bg-gray-50 border-t border-gray-200 space-y-4">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        Clé
                    </label>
                    <input
                        type="text"
                        value={keyItem.key}
                        readOnly
                        className="w-full px-3 py-2 border border-gray-300 bg-gray-100 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        Valeur
                    </label>
                    <div className="flex gap-2 items-center">
                        <input
                            type="text"
                            value={value}
                            onChange={e => setValue(e.target.value)}
                            className="flex-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        />
                        <button
                            onClick={() => saveValue(keyItem)}
                            className="flex-shrink-0 p-2 bg-green-500 text-white rounded-lg hover:bg-green-600"
                            title="Save changes"
                        >
                            <Save className="h-4 w-4" />
                        </button>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Description
                        </label>
                        <textarea
                            value={keyItem.description || ''}
                            readOnly
                            className="mb-2 w-full px-3 py-2 border border-gray-300 bg-gray-100 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                            rows="3"
                        />
                    </div>
                </div>
            )}
        </div>
    )
}

export default function ConfigManager({domain, env, config}) {
    //const [expandedKey, setExpandedKey] = useState(null);
    //const [loading, setLoading] = useState(false);
    //const [error, setError] = useState(null);
    const [searchParams] = useSearchParams();
    const serviceId = searchParams.get('service') || null;
    const [keys, setKeys] = useState([]);

    useEffect(() => {
        if (config['services']) {
            let service = config['services']
                .find(service => service.type === 'dyna-config' && service.id === serviceId);
            if (service.config && service.config.keys) {
                setKeys([...service.config.keys]);
            }
        }
    }, []);

    const updateKeyValue = (key) => {
        updateDynaConfig(domain, env, serviceId, key).then(data => {
            setKeys(keys.map(f => f.key === data.key ? { ...f, expiration: key.value } : f))
        });

        /* todo
        setEditingConfigs(prev => {
            const updated = JSON.parse(JSON.stringify(prev));
            const configIdx = updated.findIndex(c => c.name === configName);
            updated[configIdx].config.keys[keyIndex][field] = value;
            return updated;
        });
         */
    };

    return (
        <div className="min-h-screen bg-slate-50">
            <div className="max-w-7xl mx-auto px-6 py-8">
                <div className="flex justify-between items-start mb-8">
                    <div>
                        <div className="flex items-center gap-3 mb-2">
                            <div className="text-blue-600">
                                <Settings size={32} strokeWidth={1.5} />
                            </div>
                            <h1 className="text-4xl font-bold text-gray-900">{serviceId}</h1>
                        </div>
                        <p className="text-gray-500">Dyna Config</p>
                    </div>
                </div>

                {/* Configurations */}
                <div className="space-y-6">
                    <div key={config.name} className="bg-white rounded-lg border border-gray-200 overflow-hidden">
                        {/* Config Header */}
                        <Header config={config} keys={keys} />

                        {/* Keys List */}
                        <div className="divide-y divide-gray-200">
                            {
                                keys.map((key, keyIdx) => <KeyEditor keyItem={key} keyIdx={keyIdx} updateKeyValue={updateKeyValue} />)
                            }
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
