import axios from 'axios';

// const API_BASE = import.meta.env.VITE_API_BASE;
const API_BASE = 'http://localhost:8080/backend/api';
console.log('API_BASE:', API_BASE);

// axios.interceptors.request.use(function (config) {
//   const creds = localStorage.getItem('creds');
//   console.log('credentialssssssssssss:', creds);
//   if (creds) {
//     config.headers.Authorization = `Basic ${creds}`;
//   }
//   return config;
// });



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
  try {
    const url = `${API_BASE}/${domain}/${env}/audits/${id}`;
    console.log('Fetching from URL:', url);

    const response = await axios.get(url);
    console.log('Raw API response:', response);
    console.log('Response data:', response.data);
    console.log('Response status:', response.status);

    return response.data;
  } catch (error) {
    console.error('API Error:', error);
    console.error('Error response:', error.response);
    throw error;
  }
};

export const getConfig = async (domain, appId) => {
  console.log(`Fetching config from ${API_BASE}/config/${domain}`);
  const response = await axios.get(`${API_BASE}/config/${domain}?appId=${appId}`);
  return response.data;
}