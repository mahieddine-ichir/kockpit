import { useCallback, useEffect, useMemo, useState } from 'react';
import { Chart as ChartJS, ArcElement, Tooltip } from 'chart.js';
import { Pie, Doughnut } from 'react-chartjs-2';
import './App.css';

ChartJS.register(ArcElement, Tooltip);

const REFRESH_INTERVAL_MS = 30_000;
const MAX_SLICES = 7;
const TOP_INDICES_COUNT = 10;
// Roughly matches OpenSearch's default disk watermarks (85% low, 90% high).
const DISK_WARNING_PCT = 80;
const DISK_CRITICAL_PCT = 90;

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

function formatBytes(value) {
  const bytes = Number(value);
  if (!Number.isFinite(bytes)) return '—';
  if (bytes === 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB'];
  const i = Math.min(units.length - 1, Math.floor(Math.log(bytes) / Math.log(1024)));
  return `${(bytes / 1024 ** i).toFixed(1)} ${units[i]}`;
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
  const [indices, setIndices] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [updatedAt, setUpdatedAt] = useState(null);
  const [autoRefresh, setAutoRefresh] = useState(true);
  useThemeTick();

  const load = useCallback(async () => {
    const [allocation, health, shardList, indexList] = await Promise.allSettled([
      fetchJson('api/allocation'),
      fetchJson('api/cluster-health'),
      fetchJson('api/shards'),
      fetchJson('api/indices'),
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
    if (indexList.status === 'fulfilled') {
      setIndices(Array.isArray(indexList.value) ? indexList.value : []);
    } else {
      errors.push(indexList.reason.message);
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

  const nodeGauges = useMemo(() => {
    const usedColor = readCssVar('--status-ok');
    const warningColor = readCssVar('--status-warning');
    const criticalColor = readCssVar('--status-critical');
    const freeColor = readCssVar('--gridline');

    return rows
      .filter((r) => r['disk.percent'] != null && r['disk.percent'] !== '' && r.node)
      .map((r) => {
        const percent = Number(r['disk.percent']);
        const color = percent >= DISK_CRITICAL_PCT ? criticalColor : percent >= DISK_WARNING_PCT ? warningColor : usedColor;
        return {
          node: r.node,
          percent,
          used: r['disk.used'],
          avail: r['disk.avail'],
          total: r['disk.total'],
          data: {
            labels: ['Used', 'Free'],
            datasets: [
              {
                data: [percent, Math.max(0, 100 - percent)],
                backgroundColor: [color, freeColor],
                borderWidth: 0,
              },
            ],
          },
        };
      })
      .sort((a, b) => b.percent - a.percent);
  }, [rows]);

  const gaugeOptions = useMemo(
    () => ({
      responsive: true,
      maintainAspectRatio: false,
      cutout: '72%',
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: (ctx) => `${ctx.label}: ${formatPct(ctx.parsed)}`,
          },
        },
      },
    }),
    [],
  );

  const topIndices = useMemo(() => indices.slice(0, TOP_INDICES_COUNT), [indices]);

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
        <h2>Disk usage per node</h2>
        {nodeGauges.length === 0 && !loading ? (
          <p className="empty-state">No allocation data returned.</p>
        ) : (
          <div className="gauge-grid">
            {nodeGauges.map((g) => (
              <div className="gauge-card" key={g.node}>
                <div className="gauge-canvas-wrap">
                  <Doughnut data={g.data} options={gaugeOptions} />
                  <span className="gauge-center">{formatPct(g.percent)}</span>
                </div>
                <div className="gauge-node-name">{g.node}</div>
                <div className="gauge-detail">
                  {g.used ?? '—'} used / {g.avail ?? '—'} free
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="card">
        <h2>Top {TOP_INDICES_COUNT} indices by size</h2>
        {topIndices.length === 0 && !loading ? (
          <p className="empty-state">No index data returned.</p>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>Index</th>
                  <th>Health</th>
                  <th>Status</th>
                  <th>Docs</th>
                  <th>Size (total)</th>
                  <th>Size (primary)</th>
                </tr>
              </thead>
              <tbody>
                {topIndices.map((idx) => (
                  <tr key={idx.index}>
                    <td>{idx.index}</td>
                    <td className="node-cell">
                      <span className={`status-dot status-${idx.health}`} />
                      {idx.health ?? '—'}
                    </td>
                    <td>{idx.status ?? '—'}</td>
                    <td>{idx['docs.count'] ?? '—'}</td>
                    <td>{formatBytes(idx['store.size'])}</td>
                    <td>{formatBytes(idx['pri.store.size'])}</td>
                  </tr>
                ))}
              </tbody>
            </table>
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
