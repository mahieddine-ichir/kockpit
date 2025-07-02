import axios from 'axios';

//const API_BASE = "https://rcu-apim-dev.azure-api.net/backend/api";
const API_BASE = "https://kockpit-apim-pro.azure-api.net/backend/api";
//const API_BASE = 'http://localhost:8080/backend/api';

export const fetchAuditReportsForDomainAndEnv = async (domain, env) => {
  const response = await axios.get(`${API_BASE}/audits/${domain}/${env}`);
  return response.data.items;
};

// deprecated use paging one
export const fetchAuditReports = async () => {
  const response = await axios.get(`${API_BASE}/audit-reports`);
  return response.data;
};

// deprecated use paging one
export const searchAudits = async (query) => {
  const response = await axios.get(`${API_BASE}/audits/_search?query=${query}`);
  return response.data;
};

// deprecated
export const fetchAuditReportsWithPaging = async (size, start) => {
  const response = await axios.get(`${API_BASE}/audits?size=${size}&start=${start}`);
  return response.data;
};

export const fetchAuditReportById = async (id) => {
  const response = await axios.get(`${API_BASE}/audit-reports/${id}`);
  return response.data;
};

export const fetchAuditRequests = async (id) => {
  const response = await axios.get(`${API_BASE}/audit-reports/${id}/requests`);
  console.log('Audit Requests Response:', JSON.stringify(response.data, null, 2));
  return response.data;
};

export const fetchAuditDetails = async (id, traceId) => {
  const response = await axios.get(`${API_BASE}/audit-reports/${id}/requests/${traceId}`);
  return response.data;
};
