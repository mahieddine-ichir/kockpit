import {useState} from 'react';

const STORAGE_KEY = 'kockpit_settings';

const DEFAULTS = {
    dynaConfigTruncateLimit: 60,
    auditDefaultPageSize: 50,
    healthRefreshRate: 30,
};

export function loadSettings() {
    try {
        const stored = localStorage.getItem(STORAGE_KEY);
        return stored ? {...DEFAULTS, ...JSON.parse(stored)} : {...DEFAULTS};
    } catch {
        return {...DEFAULTS};
    }
}

export function useSettings() {
    const [settings, setSettings] = useState(loadSettings);

    const updateSetting = (key, value) => {
        const updated = {...settings, [key]: value};
        localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
        setSettings(updated);
    };

    return {settings, updateSetting};
}