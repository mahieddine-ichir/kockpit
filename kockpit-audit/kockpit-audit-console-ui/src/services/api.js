import axios from 'axios';

const API_BASE = 'http://localhost:8080/backend/api';
console.log('API_BASE:', API_BASE);

// deprecated use paging one
export const searchAudits = async (query, domain, env, size, start) => {
  const response = await axios.get(`${API_BASE}/${domain}/${env}/audits/_search?query=${query}&size=${size}&start=${start}`);
  return response.data;
};

// deprecated use paging one
export const advancedSearchAudits = async (terms, domain, env, size, start) => {
  const response = await axios.post(`${API_BASE}/${domain}/${env}/audits/_search?size=${size}&start=${start}`, terms);
  return response.data;
};

// fixme remove deprecated
export const fetchAuditReportsWithPaging = async (domain, env, size, start) => {
  const response = await axios.get(`${API_BASE}/${domain}/${env}/audits?size=${size}&start=${start}`);
  return response.data;
};

export const fetchAuditById = async (id, domain, env) => {
  const response = await axios.get(`${API_BASE}/${domain}/${env}/audits/${id}`);
  return response.data;
};

export const getConfig = async () => {
  console.log(`Fetching config from ${API_BASE}/config`);
  const response = await axios.get(`${API_BASE}/config`);
  return response.data;
}

export const createConfig = async (configItem) => {
    const response = await axios.post(`${API_BASE}/config`, configItem);
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
