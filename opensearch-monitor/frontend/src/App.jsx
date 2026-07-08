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

export default function App() {
  const [rows, setRows] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [updatedAt, setUpdatedAt] = useState(null);
  const [autoRefresh, setAutoRefresh] = useState(true);
  useThemeTick();

  const load = useCallback(async () => {
    try {
      const res = await fetch('/api/allocation');
      const body = await res.json();
      if (!res.ok) {
        throw new Error(body.error || `Request failed: ${res.status}`);
      }
      setRows(Array.isArray(body) ? body : []);
      setError(null);
      setUpdatedAt(new Date());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    if (!autoRefresh) return undefined;
    const id = setInterval(load, REFRESH_INTERVAL_MS);
    return () => clearInterval(id);
  }, [autoRefresh, load]);

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
          <h1>OpenSearch Disk Allocation</h1>
          <p>GET _cat/allocation?v&amp;s=disk.percent:desc</p>
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
