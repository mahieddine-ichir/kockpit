import axios from 'axios';
import {useEffect, useState} from "react";

const getApiBase = () => {
    // Try runtime config first, then fallback to build-time env, then default
    return window.ENV?.VITE_API_BASE || import.meta.env.VITE_API_BASE || '/api';
}
const API_BASE = getApiBase();
console.log('API_BASE:', API_BASE);

const instance = axios.create({
   withCredentials: true,
   baseURL: API_BASE
})

// deprecated use paging one
export const searchAudits = async (query, domain, env, size, start) => {
  const response = await instance.get(`/${domain}/${env}/audits/_search?query=${query}&size=${size}&start=${start}`);
  return response.data;
};

// deprecated use paging one
export const advancedSearchAudits = async (terms, domain, env, size, start) => {
  const response = await instance.post(`/${domain}/${env}/audits/_search?size=${size}&start=${start}`, terms);
  return response.data;
};

// fixme remove deprecated
export const fetchAuditReportsWithPaging = async (domain, env, size, start) => {
  const response = await instance.get(`/${domain}/${env}/audits?size=${size}&start=${start}`);
  return response.data;
};

export const fetchAuditById = async (id, domain, env) => {
  const response = await instance.get(`/${domain}/${env}/audits/${id}`);
  return response.data;
};

export const getConfig = async () => {
  console.log(`Fetching config from ${API_BASE}/config`);
  const response = await instance.get(`/config`);
  return response.data;
}

export const createConfig = async (configItem) => {
    const response = await instance.post(`/config`, configItem);
    return response.data;
}


const getAuthProvider = () => {
    // Detect deployment environment
    if (import.meta.env.MODE === 'development') return 'dev';

    // Simple and reliable detection based on API base URL:
    // Azure: /api
    // AWS:   /backend
    const apiBase = getApiBase();

    if (apiBase.startsWith('/backend')) return 'aws';
    if (apiBase.startsWith('/api')) return 'azure';

    // Fallback for development or unknown configurations
    return 'dev';
}

export const useUserAws = () => {
    const [user, setUser] = useState(null);

    useEffect(() => {
        fetch('/api/me', { credentials: 'include' })
            .then(res => res.json())
            .then(setUser)
            .catch(() => setUser(null));
    }, []);

    return user; // { name, email, login, sub, ... }
}

export const login = async() => {
    const authProvider = getAuthProvider();
    console.log(`login on ${import.meta.env.MODE} using ${authProvider} auth`)

    if (authProvider === 'development' || import.meta.env.MODE === 'development') {
        return {
            "clientPrincipal": {
                "identityProvider": "aad",
                "userId": "123456789",
                "userDetails": "johndoe@mousquetaires.com",
                "userRoles": [
                    "support",
                    "anonymous",
                    "authenticated"
                ]
            }
        };
    }

    if (authProvider === 'azure') {
        // Azure Static Web Apps authentication
        const response = await axios.get("/.auth/me");
        return response.data;
    }

    if (authProvider === 'aws') {
        try {
            const response = await instance.get(`${API_BASE}/auth/me`);
            return {
                "clientPrincipal": {
                    "identityProvider": "cognito",
                    "userId": response.data.userId || response.data.sub,
                    "userDetails": response.data.email || response.data.username,
                    "userRoles": response.data.roles || ["authenticated"]
                }
            };
        } catch (error) {
            console.warn('Backend auth failed, using anonymous access:', error);
            // Fallback to anonymous access for AWS
            return {
                "clientPrincipal": {
                    "identityProvider": "anonymous",
                    "userId": "anonymous",
                    "userDetails": "anonymous@aws.local",
                    "userRoles": ["anonymous", "authenticated"]
                }
            };
        }
    }

    // Default fallback
    return {
        "clientPrincipal": {
            "identityProvider": "unknown",
            "userId": "anonymous",
            "userDetails": "anonymous@local",
            "userRoles": ["anonymous"]
        }
    };
}

export const logout = async() => {
    const authProvider = getAuthProvider();

    if (authProvider === 'azure') {
        // Azure Static Web Apps logout
        const response = await axios.get("/.auth/logout");
        return response.data;
    }

    if (authProvider === 'aws') {
        // AWS logout options:

        // Option 1: Backend-handled logout
        try {
            const response = await instance.post(`${API_BASE}/auth/logout`);
            return response.data;
        } catch (error) {
            console.warn('Backend logout failed:', error);
            // Option 2: Client-side logout (clear local storage, etc.)
            localStorage.clear();
            sessionStorage.clear();
            return { success: true };
        }
    }

    return { success: true };
}

export const getFeatureFlags = async (domain, env) => {
    const response = await instance.get(`/${domain}/${env}/feature-flipping`);
    return response.data;
};

export const updateFeatureFlag = async (domain, env, serviceId, flag) => {
    const response = await instance.put(`/${domain}/${env}/feature-flipping/${serviceId}?key=${flag.key}`, flag);
    return response.data;
};

export const getFeatureHistory = async (domain, env) => {
    const response = await instance.get(`/${domain}/${env}/feature-flipping/history`);
    return response.data;
};

// stat's
export const getAppDistributionData = async (domain, env, timeRange) => {
    const response = await instance.get(`/${domain}/${env}/dashboard/app_distribution_data?gte=now-${timeRange}`);
    return response.data;
};

export const getStatusDistributionByAppId = async (domain, env, timeRange) => {
    const response = await instance.get(`/${domain}/${env}/dashboard/status_distribution_by_appId?gte=now-${timeRange}`);
    return response.data;
};

export const getOverTimeByAppId = async (domain, env, timeRange) => {
    const response = await instance.get(`/${domain}/${env}/dashboard/overTime_by_appId?gte=now-${timeRange}`);
    return response.data;
};

// fixme use domain/env
export const getHealth = async (domain, env) => {
    const response = await instance.get(`${domain}/${env}/heartbeat`);
    return response.data;
};

export const updateDynaConfig = async (domain, env, serviceId, config) => {
    console.log(`Updating dyna config for ${serviceId} on ${domain}/${env} => ${JSON.stringify(config)}`);
    const response = await instance.put(`/${domain}/${env}/dyna-config/${serviceId}?key=${config.key}`, config);
    return response.data;
}
