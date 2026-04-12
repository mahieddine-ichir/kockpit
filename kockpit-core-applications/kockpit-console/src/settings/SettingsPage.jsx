import React, {useState} from 'react';
import {Cog6ToothIcon} from '@heroicons/react/24/outline';
import {Check} from 'lucide-react';
import {loadSettings, useSettings} from './useSettings.js';

const SettingRow = ({label, description, children}) => (
    <div className="flex items-center justify-between py-4 border-b border-slate-100 last:border-0">
        <div className="flex-1 min-w-0 mr-8">
            <p className="text-sm font-medium text-slate-800">{label}</p>
            {description && <p className="text-xs text-slate-500 mt-0.5">{description}</p>}
        </div>
        <div className="flex-shrink-0">{children}</div>
    </div>
);

export default function SettingsPage() {
    const {updateSetting} = useSettings();
    const [draft, setDraft] = useState(loadSettings);
    const [saved, setSaved] = useState(false);

    const handleChange = (key, value) => {
        setDraft(d => ({...d, [key]: value}));
        setSaved(false);
    };

    const handleSave = () => {
        Object.entries(draft).forEach(([key, value]) => updateSetting(key, value));
        setSaved(true);
        setTimeout(() => setSaved(false), 2000);
    };

    return (
        <div className="min-h-screen bg-slate-50">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="mb-8 flex items-center justify-between">
                    <h1 className="text-3xl font-bold text-slate-800 flex items-center gap-3">
                        <Cog6ToothIcon className="h-8 w-8 text-blue-600" />
                        Settings
                    </h1>
                </div>

                <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 mb-4">
                    <h2 className="text-sm font-semibold text-slate-500 uppercase tracking-wider mb-2">Health</h2>

                    <SettingRow
                        label="Auto-refresh rate"
                        description="How often the health page reloads data. Set to 0 to disable auto-refresh."
                    >
                        <div className="flex items-center gap-2">
                            <input
                                type="number"
                                min={0}
                                max={300}
                                value={draft.healthRefreshRate}
                                onChange={e => handleChange('healthRefreshRate', Number(e.target.value))}
                                className="w-24 border border-slate-300 rounded-lg px-3 py-1.5 text-sm text-center"
                            />
                            <span className="text-sm text-slate-500">sec</span>
                        </div>
                    </SettingRow>
                </div>

                <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 mb-4">
                    <h2 className="text-sm font-semibold text-slate-500 uppercase tracking-wider mb-2">Audits</h2>

                    <SettingRow
                        label="Default page size"
                        description="Number of audit entries loaded per page."
                    >
                        <select
                            value={draft.auditDefaultPageSize}
                            onChange={e => handleChange('auditDefaultPageSize', Number(e.target.value))}
                            className="border border-slate-300 rounded-lg px-3 py-1.5 text-sm"
                        >
                            {[10, 25, 50, 100, 200].map(n => (
                                <option key={n} value={n}>{n}</option>
                            ))}
                        </select>
                    </SettingRow>
                </div>

                <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6">
                    <h2 className="text-sm font-semibold text-slate-500 uppercase tracking-wider mb-2">Dyna Config</h2>

                    <SettingRow
                        label="Value truncate limit"
                        description="Number of characters before a long value is truncated in the config key list."
                    >
                        <input
                            type="number"
                            min={10}
                            max={500}
                            value={draft.dynaConfigTruncateLimit}
                            onChange={e => handleChange('dynaConfigTruncateLimit', Number(e.target.value))}
                            className="w-24 border border-slate-300 rounded-lg px-3 py-1.5 text-sm text-center"
                        />
                    </SettingRow>
                </div>

                <div className="mt-6 flex items-center justify-end gap-3">
                    {saved && (
                        <span className="text-sm text-green-600 font-medium flex items-center gap-1">
                            <Check className="h-4 w-4" /> Saved
                        </span>
                    )}
                    <button
                        onClick={handleSave}
                        className="px-5 py-2 bg-blue-600 text-white text-sm font-semibold rounded-lg hover:bg-blue-700 transition-colors"
                    >
                        Save
                    </button>
                </div>
            </div>
        </div>
    );
}