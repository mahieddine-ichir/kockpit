import { useCallback, useEffect, useMemo, useState } from 'react';
import { Chart as ChartJS, ArcElement, Tooltip } from 'chart.js';
import { Pie } from 'react-chartjs-2';
import './App.css';

ChartJS.register(ArcElement, Tooltip);

const REFRESH_INTERVAL_MS = 30_000;
const MAX_SLICES = 7;

function readCssVar(name) {
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
}

function useThemeTick() {
  const [, setTick] = useState(0);
  useEffect(() => {
    const mql = window.matchMedia('(prefers-color-scheme: dark)');
    const handler = () => setTick((t) => t + 1);
    mql.addEventListener('change', handler);
    return () => mql.removeEventListener('change', handler);
  }, []);
}

function formatPct(value) {
  return `${Number(value).toFixed(1)}%`;
}

// Relative (no leading slash) so requests resolve under whatever path prefix
// the page itself was loaded from — see vite.config.js `base`.
async function fetchJson(path) {
  const res = await fetch(path);
  const body = await res.json();
  if (!res.ok) {
    throw new Error(body.error || `Request failed: ${res.status}`);
  }
  return body;
}

export default function App() {
  const [rows, setRows] = useState([]);
  const [clusterHealth, setClusterHealth] = useState(null);
  const [shards, setShards] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [updatedAt, setUpdatedAt] = useState(null);
  const [autoRefresh, setAutoRefresh] = useState(true);
  useThemeTick();

  const load = useCallback(async () => {
    const [allocation, health, shardList] = await Promise.allSettled([
      fetchJson('api/allocation'),
      fetchJson('api/cluster-health'),
      fetchJson('api/shards'),
    ]);

    const errors = [];
    if (allocation.status === 'fulfilled') {
      setRows(Array.isArray(allocation.value) ? allocation.value : []);
    } else {
      errors.push(allocation.reason.message);
    }
    if (health.status === 'fulfilled') {
      setClusterHealth(health.value);
    } else {
      errors.push(health.reason.message);
    }
    if (shardList.status === 'fulfilled') {
      setShards(Array.isArray(shardList.value) ? shardList.value : []);
    } else {
      errors.push(shardList.reason.message);
    }

    setError(errors.length > 0 ? errors.join('; ') : null);
    setUpdatedAt(new Date());
    setLoading(false);
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    if (!autoRefresh) return undefined;
    const id = setInterval(load, REFRESH_INTERVAL_MS);
    return () => clearInterval(id);
  }, [autoRefresh, load]);

  const unassignedShards = useMemo(
    () => shards.filter((s) => s.state === 'UNASSIGNED'),
    [shards],
  );

  const healthMetrics = useMemo(() => {
    if (!clusterHealth) return [];
    return [
      { label: 'Nodes', value: clusterHealth.number_of_nodes },
      { label: 'Data nodes', value: clusterHealth.number_of_data_nodes },
      { label: 'Active shards', value: clusterHealth.active_shards },
      { label: 'Unassigned', value: clusterHealth.unassigned_shards },
      { label: 'Relocating', value: clusterHealth.relocating_shards },
      { label: 'Initializing', value: clusterHealth.initializing_shards },
      { label: 'Pending tasks', value: clusterHealth.number_of_pending_tasks },
    ];
  }, [clusterHealth]);

  const { chartData, legendItems, chartOptions } = useMemo(() => {
    const seriesColors = [
      readCssVar('--series-1'),
      readCssVar('--series-2'),
      readCssVar('--series-3'),
      readCssVar('--series-4'),
      readCssVar('--series-5'),
      readCssVar('--series-6'),
      readCssVar('--series-7'),
    ];
    const otherColor = readCssVar('--series-other');
    const surfaceColor = readCssVar('--surface-1');

    const withPercent = rows
      .filter((r) => r['disk.percent'] != null && r['disk.percent'] !== '')
      .map((r) => ({ node: r.node, percent: Number(r['disk.percent']) }))
      .sort((a, b) => b.percent - a.percent);

    const top = withPercent.slice(0, MAX_SLICES);
    const rest = withPercent.slice(MAX_SLICES);
    const otherTotal = rest.reduce((sum, r) => sum + r.percent, 0);

    const items = top.map((r, i) => ({ label: r.node, value: r.percent, color: seriesColors[i] }));
    if (rest.length > 0) {
      items.push({ label: `Other (${rest.length} nodes)`, value: otherTotal, color: otherColor });
    }

    const chartData = {
      labels: items.map((i) => i.label),
      datasets: [
        {
          data: items.map((i) => i.value),
          backgroundColor: items.map((i) => i.color),
          borderColor: surfaceColor,
          borderWidth: 2,
          hoverOffset: 6,
        },
      ],
    };

    const chartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: (ctx) => `${ctx.label}: ${formatPct(ctx.parsed)}`,
          },
        },
      },
    };

    return { chartData, legendItems: items, chartOptions };
  }, [rows]);

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>OpenSearch Monitor</h1>
          <p>Cluster health, shard allocation, and disk usage</p>
        </div>
        <div className="controls">
          {updatedAt && <span className="updated-at">Updated {updatedAt.toLocaleTimeString()}</span>}
          <label>
            <input
              type="checkbox"
              checked={autoRefresh}
              onChange={(e) => setAutoRefresh(e.target.checked)}
            />
            Auto-refresh (30s)
          </label>
          <button type="button" onClick={load}>
            Refresh
          </button>
        </div>
      </header>

      {error && <div className="banner-error">{error}</div>}

      <section className="card">
        <h2>Cluster health</h2>
        {!clusterHealth && !loading ? (
          <p className="empty-state">No cluster health data returned.</p>
        ) : (
          clusterHealth && (
            <>
              <div className="health-summary">
                <span className={`status-dot status-${clusterHealth.status}`} />
                <span className={`status-label status-${clusterHealth.status}`}>{clusterHealth.status}</span>
                <span className="cluster-name">{clusterHealth.cluster_name}</span>
              </div>
              <div className="metric-grid">
                {healthMetrics.map((m) => (
                  <div className="metric" key={m.label}>
                    <span className="metric-label">{m.label}</span>
                    <span className="metric-value">{m.value ?? '—'}</span>
                  </div>
                ))}
              </div>
            </>
          )
        )}
      </section>

      <section className="card">
        <h2>Unassigned shards</h2>
        {unassignedShards.length === 0 ? (
          <p className="empty-state">
            {loading ? 'Loading…' : 'No unassigned shards.'}
          </p>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>Index</th>
                  <th>Shard</th>
                  <th>Type</th>
                  <th>Reason</th>
                </tr>
              </thead>
              <tbody>
                {unassignedShards.map((s, idx) => (
                  <tr key={`${s.index}-${s.shard}-${s.prirep}-${idx}`}>
                    <td>{s.index}</td>
                    <td>{s.shard}</td>
                    <td>{s.prirep === 'p' ? 'primary' : 'replica'}</td>
                    <td>{s['unassigned.reason'] ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="card">
        <h2>Disk usage by node</h2>
        {legendItems.length === 0 && !loading ? (
          <p className="empty-state">No allocation data returned.</p>
        ) : (
          <div className="chart-layout">
            <div className="chart-canvas-wrap">
              <Pie data={chartData} options={chartOptions} />
            </div>
            <ul className="legend">
              {legendItems.map((item) => (
                <li key={item.label}>
                  <span className="swatch" style={{ background: item.color }} />
                  <span className="node-name">{item.label}</span>
                  <span className="node-pct">{formatPct(item.value)}</span>
                </li>
              ))}
            </ul>
          </div>
        )}
      </section>

      <section className="card">
        <h2>Raw allocation data</h2>
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Node</th>
                <th>Shards</th>
                <th>Disk indices</th>
                <th>Disk used</th>
                <th>Disk avail</th>
                <th>Disk total</th>
                <th>Disk %</th>
                <th>Host</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((r, idx) => (
                <tr key={`${r.node ?? 'unassigned'}-${r.ip ?? r.host ?? ''}-${idx}`}>
                  <td>{r.node ?? '—'}</td>
                  <td>{r.shards ?? '—'}</td>
                  <td>{r['disk.indices'] ?? '—'}</td>
                  <td>{r['disk.used'] ?? '—'}</td>
                  <td>{r['disk.avail'] ?? '—'}</td>
                  <td>{r['disk.total'] ?? '—'}</td>
                  <td>{r['disk.percent'] != null ? formatPct(r['disk.percent']) : '—'}</td>
                  <td>{r.host ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
