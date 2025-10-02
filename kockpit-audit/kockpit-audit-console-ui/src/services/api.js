import axios from 'axios';
import {loginRequest} from "../authConfig.js";

const API_BASE = import.meta.env.VITE_API_BASE;
console.log('API_BASE:', API_BASE);

// deprecated use paging one
export const searchAudits = async (query, domain, env, size, start) => {
  const response = await axios.get(`${API_BASE}/${domain}/${env}/audits/_search?query=${query}&size=${size}&start=${start}`);
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

export const getToken = (instance, accounts) => {
    return instance
        .acquireTokenSilent({
            ...loginRequest,
            account: accounts[0],
        })
}

export const logout = async() => {
    const response = await axios.get("/logout");
    return response.data;
}
