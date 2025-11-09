import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE;
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


export const login = async() => {
    console.log(`login on ${import.meta.env.MODE}`)
    if (import.meta.env.MODE === 'development') {
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
    const response = await axios.get("/.auth/me");
    return response.data;
}

export const logout = async() => {
    const response = await axios.get("/logout");
    return response.data;
}

export const getFeatureFlags = async (domain, env) => {
    const response = await instance.get(`/${domain}/${env}/feature-flipping`);
    return response.data;
};

export const updateFeatureFlag = async (domain, env, flag) => {
    const response = await instance.put(`/${domain}/${env}/feature-flipping?key=${flag.key}`, flag);
    return response.data;
};

export const getFeatureHistory = async (domain, env) => {
    const response = await instance.get(`/${domain}/${env}/feature-flipping/history`);
    return response.data;
};

// stat's
export const getAppDistributionData = async () => {
    const response = await instance.get(`/dashboard/app_distribution_data`);
    return response.data;
};

export const getStatusDistributionByAppId = async (timeRange) => {
    const response = await instance.get(`/dashboard/status_distribution_by_appId?gte=now-${timeRange}`);
    return response.data;
};

export const getOverTimeByAppId = async (timeRange) => {
    const response = await instance.get(`/dashboard/overTime_by_appId?gte=now-${timeRange}`);
    return response.data;
};

export const getHealth = async () => {
    const response = await instance.get(`/heartbeat`);
    return response.data;
};

