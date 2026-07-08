import express from 'express';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const PORT = process.env.PORT || 3000;
const OPENSEARCH_URL = process.env.OPENSEARCH_URL || 'http://localhost:9200';
const OPENSEARCH_USER = process.env.OPENSEARCH_USER;
const OPENSEARCH_PASSWORD = process.env.OPENSEARCH_PASSWORD;

const app = express();

app.get('/api/allocation', async (req, res) => {
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

app.get('/api/health', (req, res) => res.json({ status: 'ok' }));

const publicDir = path.join(__dirname, '..', 'public');
app.use(express.static(publicDir));
app.get('*', (req, res) => {
  res.sendFile(path.join(publicDir, 'index.html'));
});

app.listen(PORT, () => {
  console.log(`opensearch-monitor listening on :${PORT}, proxying ${OPENSEARCH_URL}`);
});
