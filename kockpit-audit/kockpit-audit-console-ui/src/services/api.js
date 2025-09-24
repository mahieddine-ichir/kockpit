import axios from 'axios';

// const API_BASE = import.meta.env.VITE_API_BASE;
const API_BASE = "http://localhost:8080/backend/api";
console.log('API_BASE:', API_BASE);

// deprecated use paging one
export const searchAudits = async (query, status, domain, env, size, start) => {
    const params = new URLSearchParams();
    if (query) params.append('query', query);
    if (status) params.append('status', status);
    params.append('size', size);
    params.append('start', start);

    const response = await axios.get(`${API_BASE}/${domain}/${env}/audits/_search?${params}`);
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

// export const getConfig = async () => {
//   console.log(`Fetching config from ${API_BASE}/config`);
//   const response = await axios.get(`${API_BASE}/config`);
//   return response.data;
// }


export const getConfig = async () => {
    console.log('config endpoint');
    return [
        {
            domain: "rcu",
            env: "rec",
            services: [
                {
                    "name": "audit",
                    "label": "Track and analyze system activities",
                    "config": {
                        "columns": ["appId", "requestId", "method", "path", "duration", "start", "status"]
                    }
                }
        ]
        }
    ];
};

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
