import axios from 'axios';
import {getApiBase} from './config.js';

const API_BASE = getApiBase();

const instance = axios.create({
    withCredentials: true,
    baseURL: API_BASE
});

export const getManifests = async () => {
    const response = await instance.get('/manifests');
    return response.data;
};

// One entry per domain/env, services of all its manifests merged (what the user may see).
export const getAggregatedManifests = async () => {
    const response = await instance.get('/manifests/aggregated');
    return response.data;
};

export const getManifestByName = async (domain, env, name) => {
    const response = await instance.get(`/manifests/${name}`);
    return response.data;
};

export const saveManifest = async (manifest) => {
    const response = await instance.post(`/manifests`, manifest);
    return response.data;
};
