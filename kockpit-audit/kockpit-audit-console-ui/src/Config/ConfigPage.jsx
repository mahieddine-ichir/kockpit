import React, {useEffect, useState} from 'react';
import ReactJson from 'react-json-view';
import {getConfig, createConfig} from '../services/api.js';
import {
    DocumentPlusIcon,
    CloudArrowDownIcon,
    DocumentTextIcon,
    Cog6ToothIcon,
    CheckCircleIcon,
    ExclamationTriangleIcon
} from '@heroicons/react/24/outline';

//TODO: view , download , upload
//TODO: will use more cards design after


const ConfigPage = ({ configs }) => {
    const [items, setItems] = useState(configs || []);
    const [loading, setLoading] = useState(!configs || configs.length === 0);
    const [error, setError] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [success, setSuccess] = useState(null);

    useEffect(() => {
        if (!configs || configs.length === 0) {
            setLoading(true);
            getConfig()
                .then(setItems)
                .catch(e => setError(e?.message || 'Failed to load config'))
                .finally(() => setLoading(false));
        }
    }, [configs]);

    const showMessage = (message, isError = false) => {
        if (isError) {
            setError(message);
            setSuccess(null);
        } else {
            setSuccess(message);
            setError(null);
        }
        setTimeout(() => {
            setError(null);
            setSuccess(null);
        }, 5000);
    };

    const downloadJson = (obj, fileName) => {
        const safeName = fileName && fileName.trim().length > 0 ? fileName : 'config.json';
        const finalName = safeName.toLowerCase().endsWith('.json') ? safeName : `${safeName}.json`;
        try {
            const blob = new Blob([JSON.stringify(obj, null, 2)], { type: 'application/json' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = finalName;
            a.click();
            URL.revokeObjectURL(url);
            showMessage(`Configuration "${fileName}" downloaded successfully`);
        } catch (e) {
            showMessage('Failed to download configuration', true);
        }
    };

    const onUpload = async (evt) => {
        const file = evt.target.files?.[0];
        if (!file) return;

        setUploading(true);
        setError(null);

        try {
            const text = await file.text();
            const json = JSON.parse(text);

            const payload = { ...json, _filename: file.name };
            const saved = await createConfig(payload);
            const withFilename = { _filename: file.name, ...saved };
            setItems(prev => [withFilename, ...prev]);
            showMessage(`Configuration file uploaded successfully`);
        } catch (e) {
            showMessage(e?.message || 'Failed to upload configuration file', true);
        } finally {
            evt.target.value = '';
            setUploading(false);
        }
    };

    const getDisplayName = (item, idx) => {
        return item?._filename || item?.name || `config-${idx+1}.json`;
    };

    const ConfigCard = ({ item, index }) => (
        <div className="group bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-all duration-300 overflow-hidden">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-gradient-to-r from-slate-50 to-white">
                <div className="flex items-center gap-3">
                    <div className="p-2 rounded-xl bg-blue-100 text-blue-600">
                        <DocumentTextIcon className="h-5 w-5" />
                    </div>
                    <div>
                        <h3 className="text-lg font-semibold text-slate-800">
                            {getDisplayName(item, index)}
                        </h3>
                        <div className="flex items-center gap-3 mt-1">
                            <span className="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-slate-100 text-slate-600 text-xs font-medium">
                                <Cog6ToothIcon className="h-3 w-3" />
                                domain: {item?.domain || 'N/A'}
                            </span>
                            <span className="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-slate-100 text-slate-600 text-xs font-medium">
                                env: {item?.env || 'N/A'}
                            </span>
                        </div>
                    </div>
                </div>
                <button
                    onClick={() => downloadJson(item, getDisplayName(item, index))}
                    className="flex items-center gap-2 px-4 py-2.5 rounded-xl border border-slate-200 text-slate-700 hover:bg-slate-50 hover:border-slate-300 hover:text-slate-900 font-medium text-sm transition-all duration-200 group-hover:shadow-sm"
                >
                    <CloudArrowDownIcon className="h-4 w-4" />
                    Download
                </button>
            </div>

            <div className="p-6 bg-slate-50/50">
                <div className="rounded-xl border border-slate-200 overflow-hidden bg-white shadow-xs">
                    <ReactJson
                        src={item}
                        theme="rjv-default"
                        collapsed={2}
                        displayDataTypes={false}
                        enableClipboard={true}
                        iconStyle="triangle"
                        indentWidth={4}
                        collapseStringsAfterLength={60}
                        style={{
                            padding: '16px',
                            fontSize: '14px',
                            backgroundColor: 'white'
                        }}
                    />
                </div>
            </div>
        </div>
    );

    const LoadingState = () => (
        <div className="flex items-center justify-center py-12">
            <div className="text-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
                <p className="text-slate-600 font-medium">Loading....</p>
            </div>
        </div>
    );



    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50/30 px-4 py-8">
            <div className="max-w-6xl mx-auto">

                <div className="flex items-center justify-between mb-8">
                    <div className="flex items-center gap-4">
                        <div className="p-3 rounded-2xl bg-white shadow-sm border border-slate-200">
                            <Cog6ToothIcon className="h-8 w-8 text-blue-600" />
                        </div>
                        <div>
                            <h1 className="text-3xl font-bold text-slate-900 tracking-tight">
                                Configuration Manager
                            </h1>
                            <p className="text-slate-600 mt-1">
                                Manage and view your application configurations
                            </p>
                        </div>
                    </div>

                    <label className={`inline-flex items-center gap-3 px-6 py-3.5 rounded-xl border-2 border-dashed border-slate-300 bg-white text-slate-700 font-semibold shadow-sm hover:border-blue-400 hover:bg-blue-50 hover:text-blue-700 transition-all duration-200 cursor-pointer ${uploading ? 'opacity-50 cursor-not-allowed' : ''}`}>
                        {uploading ? (
                            <div className="animate-spin h-5 w-5 border-2 border-blue-600 border-t-transparent rounded-full"></div>
                        ) : (
                            <DocumentPlusIcon className="h-5 w-5" />
                        )}
                        {uploading ? 'Uploading...' : 'Upload JSON'}
                        <input
                            type="file"
                            accept="application/json"
                            className="hidden"
                            onChange={onUpload}
                            disabled={uploading}
                        />
                    </label>
                </div>

                {error && (
                    <div className="flex items-center gap-3 p-4 mb-6 rounded-xl bg-red-50 border border-red-200 text-red-700">
                        <ExclamationTriangleIcon className="h-5 w-5 flex-shrink-0" />
                        <div className="flex-1">
                            <p className="font-medium">{error}</p>
                        </div>
                        <button
                            onClick={() => setError(null)}
                            className="p-1 rounded-lg hover:bg-red-100 transition-colors"
                        >
                            <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>
                )}

                {success && (
                    <div className="flex items-center gap-3 p-4 mb-6 rounded-xl bg-green-50 border border-green-200 text-green-700">
                        <CheckCircleIcon className="h-5 w-5 flex-shrink-0" />
                        <div className="flex-1">
                            <p className="font-medium">{success}</p>
                        </div>
                        <button
                            onClick={() => setSuccess(null)}
                            className="p-1 rounded-lg hover:bg-green-100 transition-colors"
                        >
                            <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>
                )}

                {loading ? (
                    <LoadingState />
                ) : (
                    <div className="space-y-6">
                        <>
                            <div className="flex items-center justify-between p-4 bg-white rounded-2xl border border-slate-200 shadow-xs">
                                <div className="flex items-center gap-6">
                                    <div className="text-center">
                                        <div className="text-2xl font-bold text-slate-900">{items.length}</div>
                                        <div className="text-sm text-slate-600">Total Configs</div>
                                    </div>
                                    <div className="w-px h-8 bg-slate-200"></div>
                                    <div className="text-sm text-slate-600">
                                        Upload a JSON configuration file to add it to the list
                                    </div>
                                </div>
                            </div>

                            <div className="grid gap-6">
                                {items.map((item, idx) => (
                                    <ConfigCard key={idx} item={item} index={idx} />
                                ))}
                            </div>
                        </>

                    </div>
                )}
            </div>
        </div>
    );
};

export default ConfigPage;
