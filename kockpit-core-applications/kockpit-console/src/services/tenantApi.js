import axios from 'axios';
import {getApiBase} from './config.js';

const instance = axios.create({
    withCredentials: true,
    baseURL: getApiBase()
});

// Local accounts (kockpit-backend-authentication-local): both answer {username, groups} and set the cookie.
export const signup = async (email, password, invitation) =>
    (await instance.post('/auth/signup', {email, password, invitation})).data;

// How accounts get created here: {mode: open | invite | closed}
export const getSignupMode = async () => (await instance.get('/auth/signup')).data.mode;

export const signin = async (email, password) => (await instance.post('/auth/login', {email, password})).data;

// Tenants and API keys (kockpit-backend-tenants)
export const getTenants = async () => (await instance.get('/tenants')).data;

export const createTenant = async (slug, envs) => (await instance.post('/tenants', {slug, envs})).data;

// the only response that holds the key itself
export const createApiKey = async (slug, name) =>
    (await instance.post(`/tenants/${encodeURIComponent(slug)}/api-keys`, {name})).data;

export const revokeApiKey = async (slug, id) =>
    instance.delete(`/tenants/${encodeURIComponent(slug)}/api-keys/${encodeURIComponent(id)}`);

// Members and invitations: the invitation code only ever travels in request bodies
const tenantPath = (slug) => `/tenants/${encodeURIComponent(slug)}`;

export const getMembers = async (slug) => (await instance.get(`${tenantPath(slug)}/members`)).data;

export const removeMember = async (slug, userId) =>
    instance.delete(`${tenantPath(slug)}/members/${encodeURIComponent(userId)}`);

// the only response that holds the code itself
export const createInvitation = async (slug, email) =>
    (await instance.post(`${tenantPath(slug)}/invitations`, {email: email || null})).data;

export const getInvitations = async (slug) => (await instance.get(`${tenantPath(slug)}/invitations`)).data;

export const cancelInvitation = async (slug, id) =>
    instance.delete(`${tenantPath(slug)}/invitations/${encodeURIComponent(id)}`);

export const previewInvitation = async (code) => (await instance.post('/invitations/preview', {code})).data;

export const acceptInvitation = async (code) => (await instance.post('/invitations/accept', {code})).data;

// The link to send: the code in the fragment, which browsers never send to a server (no access log keeps it)
export const invitationLink = (code) => `${window.location.origin}/join#${code}`;

// Invitations to create an account (administrators); the only creation response holds the code
export const createAccountInvitation = async (email) =>
    (await instance.post('/admin/invitations', {email: email || null})).data;

export const getAccountInvitations = async () => (await instance.get('/admin/invitations')).data;

export const cancelAccountInvitation = async (id) =>
    instance.delete(`/admin/invitations/${encodeURIComponent(id)}`);

// Password reset links: the code only ever travels in request bodies, and the link's fragment
export const createPasswordReset = async (email) => (await instance.post('/admin/password-resets', {email})).data;

export const previewPasswordReset = async (code) => (await instance.post('/auth/password-reset/preview', {code})).data;

export const resetPassword = async (code, password) => instance.post('/auth/password-reset', {code, password});

export const passwordResetLink = (code) => `${window.location.origin}/reset-password#${code}`;

// Administrators of the instance
export const getAdministrators = async () => (await instance.get('/admin/administrators')).data;

export const addAdministrator = async (email) => (await instance.post('/admin/administrators', {email})).data;

export const removeAdministrator = async (userId) =>
    instance.delete(`/admin/administrators/${encodeURIComponent(userId)}`);

export const getAllTenants = async () => (await instance.get('/admin/tenants')).data;

export const setApiKeyQuota = async (id, dailyQuota) =>
    (await instance.put(`/admin/api-keys/${encodeURIComponent(id)}/quota`, {dailyQuota})).data;

// The backend's explanation of a refused call ({error, message}), else the HTTP error.
export const reasonOf = (err) => err?.response?.data?.message || err?.message || 'Unknown error';
