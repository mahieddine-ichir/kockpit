import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE;
console.log('API_BASE:', API_BASE);

export const authenticate = async (username, password) => {
  const response = await axios.get(`/.auth/me`);
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

export const getConfig = async () => {
  console.log(`Fetching config from ${API_BASE}/config`);
  const response = await axios.get(`${API_BASE}/config`);
  return response.data;
}
