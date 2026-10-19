// The user's favorite domain/env, loaded by default when coming back to the console.
// Stored by domain/env rather than by position: the list's order changes when manifests are
// added or removed, while domain/env identifies an entry (the list is aggregated per domain/env).
const STORAGE_KEY = 'kockpit_favorite_config';

export function loadFavoriteConfig() {
    try {
        const stored = JSON.parse(localStorage.getItem(STORAGE_KEY));
        return stored?.domain && stored?.env ? {domain: stored.domain, env: stored.env} : null;
    } catch {
        return null;
    }
}

export function saveFavoriteConfig(favorite) {
    try {
        if (favorite) {
            localStorage.setItem(STORAGE_KEY, JSON.stringify({domain: favorite.domain, env: favorite.env}));
        } else {
            localStorage.removeItem(STORAGE_KEY);
        }
    } catch {
        // storage unavailable (private mode, blocked site data): the favorite just isn't kept
    }
}

export function isFavoriteConfig(favorite, config) {
    return !!favorite && !!config && favorite.domain === config.domain && favorite.env === config.env;
}

// index of the favorite in the configs list, -1 when there is none or it is no longer listed
export function findFavoriteIndex(configs, favorite = loadFavoriteConfig()) {
    return favorite ? configs.findIndex(config => isFavoriteConfig(favorite, config)) : -1;
}
