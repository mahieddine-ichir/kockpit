export function fetchIndexedValue(audit, key) {
    if (audit.indexedKeyValues) {
        const found = audit.indexedKeyValues.find(kv => kv.key === key);
        return found ? found.value : undefined;
    }
}

export function getHttStatus(audit) {
    return fetchIndexedValue(audit, 'httpStatus');
}

export function getMethod(audit) {
    return fetchIndexedValue(audit, 'httpMethod');
}

export function getPath(audit) {
    return fetchIndexedValue(audit, 'httpPath');
}

export function dateFormat(timestamp) {
    return new Date(timestamp).toLocaleString();
}