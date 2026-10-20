// Walking the audit JSON for the tree: containers, JSON held in strings, and search matches

export const isContainer = (value) => value !== null && typeof value === 'object';

// A string holding a JSON object/array (audit events often are): its parsed value, else undefined
export const parseEmbedded = (text) => {
    const trimmed = text.trim();
    if (trimmed.length < 2 || (trimmed[0] !== '{' && trimmed[0] !== '[')) return undefined;
    try {
        const parsed = JSON.parse(trimmed);
        return isContainer(parsed) ? parsed : undefined;
    } catch {
        return undefined;
    }
};

// [key, child] pairs - array indexes as numbers, so they render apart from object keys
export const entriesOf = (container) =>
    Array.isArray(container) ? container.map((child, index) => [index, child]) : Object.entries(container);

const childrenOf = (value) => {
    if (isContainer(value)) return value;
    return typeof value === 'string' ? parseEmbedded(value) : undefined;
};

// Search: the paths of the containers on the way to a match (to open them) and the match count.
// A key matches on its name, a primitive on its text; JSON held in strings is searched too.
export const findMatches = (data, needle) => {
    const open = new Set();
    let count = 0;
    const visit = (value, path) => {
        const children = childrenOf(value);
        if (!children) {
            const hit = value !== null && String(value).toLowerCase().includes(needle);
            if (hit) count++;
            return hit;
        }
        let found = false;
        entriesOf(children).forEach(([key, child]) => {
            const keyHit = typeof key === 'string' && key.toLowerCase().includes(needle);
            if (keyHit) count++;
            if (visit(child, `${path}/${key}`) || keyHit) found = true;
        });
        if (found) open.add(path);
        return found;
    };
    visit(data, '');
    return { open, count };
};

// Every container path, for "expand all"
export const allContainerPaths = (data) => {
    const paths = new Set();
    const visit = (value, path) => {
        const children = childrenOf(value);
        if (!children) return;
        paths.add(path);
        entriesOf(children).forEach(([key, child]) => visit(child, `${path}/${key}`));
    };
    visit(data, '');
    return paths;
};
