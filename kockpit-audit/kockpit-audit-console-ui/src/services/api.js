import axios from 'axios';

const API_BASE = import.meta.env.VITE_BACKEND_API;
console.log('API_BASE:', API_BASE);

// deprecated use paging one
export const fetchAuditReports = async () => {
  const response = await axios.get(`${API_BASE}/audit-reports`);
  return response.data;
};

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