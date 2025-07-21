import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE;
console.log('API_BASE:', API_BASE);

const api = axios.create({
  baseURL: API_BASE,
  withCredentials: true
});

// Request interceptor
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor
api.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        localStorage.removeItem('token');
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
);

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

export const fetchAuditRequests = async (id, domain, env) => {
  const response = await axios.get(`${API_BASE}/${domain}/${env}/audits/${id}/requests`);
  console.log('Audit Requests Response:', JSON.stringify(response.data, null, 2));
  return response.data;
};

export const getConfig = async () => {
  console.log('Fetching config');
  const response = await axios.get(`${API_BASE}/config`);
  return response.data;
}