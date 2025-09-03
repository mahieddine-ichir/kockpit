import axios from 'axios';

// const API_BASE = import.meta.env.VITE_API_BASE;
const API_BASE = 'http://localhost:8080/backend/api';
console.log('API_BASE:', API_BASE);

const API_BASEE = 'http://localhost:8082/api/insights/dashboard';


axios.interceptors.request.use(function (config) {
  const creds = localStorage.getItem('creds');
  config.headers.Authorization = `Basic ${creds}`;
  return config;
});

export const authenticate = async (username, password) => {
  console.log(`Authenticating user ${username} on ${API_BASE}/me`);
  const response = await axios.get(`${API_BASE}/me`, {
    auth: {
      username: username,
      password: password
    }
  });
  return response.data;
}

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

export const getConfig = async (domain, appId) => {
  console.log(`Fetching config from ${API_BASE}/config`);
  const response = await axios.get(`${API_BASE}/config/${domain}?appId=${appId}`);
  return response.data;
}

export const fetchDashboardSummary = async (filters = {}) => {
  const params = buildParams(filters);
  const response = await axios.get(`${API_BASEE}/summary`, { params });
  return response.data;
};

export const fetchDistribution = async (type, filters = {}) => {
  const params = buildParams(filters);

  const endpointMap = {
    statusDistribution: 'status-distribution',
    methodDistribution: 'method-distribution',
    domainDistribution: 'domain-env-distribution',
  };

  const endpoint = endpointMap[type] || type;
  const response = await axios.get(`${API_BASEE}/charts/${endpoint}`, { params });
  return response.data;
};

export const fetchAvailableFilters = async () => {
  try {
    const response = await axios.get(`${API_BASEE}/filters`);
    return response.data;
  } catch (error) {
    console.warn('Filters endpoint not available; using empty filters set.');
    return { domains: [], environments: [] };
  }
};

export const subscribeToUpdates = (callback) => {
  const eventSource = new EventSource(`${API_BASEE}/stream`);
  eventSource.onmessage = (event) => {
    callback(JSON.parse(event.data));
  };
  return () => eventSource.close();
};

const buildParams = (filters) => {
  const params = {};

  if (filters.from) {
    params.from = filters.from.toISOString();
  }

  if (filters.to) {
    params.to = filters.to.toISOString();
  }

  if (filters.domain) {
    params.domain = filters.domain;
  }

  if (filters.env) {
    params.env = filters.env;
  }

  return params;
};