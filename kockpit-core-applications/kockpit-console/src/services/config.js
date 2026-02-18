export const getApiBase = () => {
    // Try runtime config first, then fallback to build-time env, then default
    //return window.ENV?.VITE_API_BASE || import.meta.env.VITE_API_BASE || '/api';
    return '/api'; // fixme for local dev
}
