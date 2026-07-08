import express from 'express';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const PORT = process.env.PORT || 3000;
const OPENSEARCH_URL = process.env.OPENSEARCH_URL || 'http://localhost:9200';
const OPENSEARCH_USER = process.env.OPENSEARCH_USER;
const OPENSEARCH_PASSWORD = process.env.OPENSEARCH_PASSWORD;

// Path prefix this app is mounted under behind a shared reverse proxy/ALB
// that forwards the full original path unchanged (plain ALB path-pattern
// rules don't strip the matched prefix). Leave unset for standalone
// deployments where the app owns the domain root, e.g. "/opensearch-monitor".
function normalizeBasePath(raw) {
  if (!raw) return '';
  const withLeadingSlash = raw.startsWith('/') ? raw : `/${raw}`;
  return withLeadingSlash.replace(/\/+$/, '');
}
const BASE_PATH = normalizeBasePath(process.env.BASE_PATH);

const app = express();
const router = express.Router();

router.get('/api/allocation', async (req, res) => {
  try {
    const url = new URL('/_cat/allocation', OPENSEARCH_URL);
    url.searchParams.set('format', 'json');
    url.searchParams.set('s', 'disk.percent:desc');

    const headers = { Accept: 'application/json' };
    if (OPENSEARCH_USER) {
      const token = Buffer.from(`${OPENSEARCH_USER}:${OPENSEARCH_PASSWORD ?? ''}`).toString('base64');
      headers.Authorization = `Basic ${token}`;
    }

    const upstream = await fetch(url, { headers });
    if (!upstream.ok) {
      const body = await upstream.text();
      res.status(upstream.status).json({ error: `OpenSearch responded ${upstream.status}`, body });
      return;
    }

    const rows = await upstream.json();
    res.json(rows);
  } catch (err) {
    res.status(502).json({ error: 'Failed to reach OpenSearch', detail: err.message });
  }
});

router.get('/api/health', (req, res) => res.json({ status: 'ok' }));

const publicDir = path.join(__dirname, '..', 'public');
router.use(express.static(publicDir));
router.get('*', (req, res) => {
  res.sendFile(path.join(publicDir, 'index.html'));
});

// Prefix-specific routes must be registered before the root fallback mount
// below — the root mount's own catch-all matches every path (it's mounted at
// "/"), so if it ran first it would swallow prefixed requests too.
if (BASE_PATH) {
  // Relative asset/API paths in the frontend only resolve correctly if the
  // page itself was loaded with a trailing slash (e.g. "/opensearch-monitor/",
  // not "/opensearch-monitor"), so redirect the bare prefix to add one.
  // Plain string comparison, not app.get(BASE_PATH, ...): Express's route
  // matcher treats "/foo" and "/foo/" as the same route by default (non-strict
  // routing), which would match the already-slashed URL too and redirect it
  // to itself in an infinite loop.
  app.use((req, res, next) => {
    if (req.path === BASE_PATH) {
      res.redirect(308, `${BASE_PATH}/`);
      return;
    }
    next();
  });
  app.use(BASE_PATH, router);
}

// Root fallback — covers standalone deployments and target-group health
// checks, which hit the container directly without any prefix.
app.use(router);

app.listen(PORT, () => {
  const suffix = BASE_PATH ? `, also mounted at ${BASE_PATH}` : '';
  console.log(`opensearch-monitor listening on :${PORT}, proxying ${OPENSEARCH_URL}${suffix}`);
});
