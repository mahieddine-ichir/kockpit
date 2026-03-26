export const getApiBase = () => {
    // Try runtime config first, then fallback to build-time env, then default
    const runtimeBase = window.ENV?.VITE_API_BASE;
    if (runtimeBase && !runtimeBase.startsWith('${')) {
        return runtimeBase;
    }
    return import.meta.env.VITE_API_BASE || '/api';
}
