import { useState, useMemo, useCallback } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell,
} from 'recharts';
import { RefreshCw, AlertTriangle, Shield, Heart, Moon } from 'lucide-react';
import { persons, units as unitsList, alerts as rawAlerts } from '../../data/mockData';

/* ── colour tokens ── */
const GREEN  = '#00E5A0';
const RED    = '#FF4757';
const YELLOW = '#FFA502';
const CYAN   = '#00D4FF';

function statusColor(readiness) {
  if (readiness >= 70) return GREEN;
  if (readiness >= 50) return YELLOW;
  return RED;
}

/* ── KPI Card ── */
function KpiCard({ icon: Icon, label, value, sub, color }) {
  return (
    <div className="bg-[#111318] border border-[#1e2028] rounded-lg p-4 flex items-start gap-3 min-w-0">
      <div
        className="shrink-0 w-9 h-9 rounded-md flex items-center justify-center"
        style={{ backgroundColor: `${color}18`, color }}
      >
        <Icon size={18} />
      </div>
      <div className="min-w-0">
        <p className="text-[11px] uppercase tracking-wider text-[#6b7280] font-mono truncate">
          {label}
        </p>
        <p className="text-2xl font-bold font-mono leading-tight" style={{ color }}>
          {value}
        </p>
        {sub && <p className="text-[11px] text-[#6b7280] font-mono mt-0.5">{sub}</p>}
      </div>
    </div>
  );
}

/* ── Alert Row ── */
function AlertRow({ alert, person }) {
  const ts = new Date(alert.timestamp);
  const ago = Math.round((Date.now() - ts) / 60000);
  const agoStr = ago < 60 ? `${ago}m ago` : `${Math.round(ago / 60)}h ago`;

  return (
    <div className="flex items-start gap-2 py-2 border-b border-[#1e2028] last:border-0">
      <span
        className="mt-1 shrink-0 w-2 h-2 rounded-full"
        style={{
          backgroundColor: alert.type === 'critical' ? RED : alert.type === 'warning' ? YELLOW : CYAN,
        }}
      />
      <div className="min-w-0 flex-1">
        <p className="text-xs font-mono text-[#e5e7eb] truncate">
          {person ? person.name : 'Unit-wide'}{' '}
          <span className="text-[#6b7280]">— {alert.title}</span>
        </p>
        <p className="text-[10px] font-mono text-[#6b7280] truncate">{alert.message}</p>
      </div>
      <span className="text-[10px] font-mono text-[#6b7280] shrink-0">{agoStr}</span>
    </div>
  );
}

/* ── Readiness heatmap tooltip ── */
function HeatmapCell({ person }) {
  const r = person.metrics.readinessScore;
  const bg = statusColor(r);
  return (
    <div
      className="group relative rounded-sm cursor-default"
      style={{ backgroundColor: bg, width: '100%', paddingBottom: '100%' }}
    >
      <div className="absolute inset-0 flex items-center justify-center">
        <span className="text-[9px] font-mono font-bold text-[#0a0b0d] opacity-80 select-none">
          {r}
        </span>
      </div>
      {/* tooltip */}
      <div className="pointer-events-none absolute z-30 bottom-full left-1/2 -translate-x-1/2 mb-1 hidden group-hover:block">
        <div className="bg-[#1e2028] border border-[#2a2d36] text-[#e5e7eb] text-[10px] font-mono rounded px-2 py-1 whitespace-nowrap shadow-lg">
          <p className="font-semibold">{person.name}</p>
          <p>Readiness {r} · Stress {person.metrics.stressScore}</p>
          <p>{person.unit}</p>
        </div>
      </div>
    </div>
  );
}

/* ── Main component ── */
export default function CommanderOps() {
  const allUnits = useMemo(() => [...new Set(persons.map((p) => p.unit))].sort(), []);
  const [unitFilter, setUnitFilter] = useState('ALL');
  const [refreshKey, setRefreshKey] = useState(0);

  const filtered = useMemo(
    () => (unitFilter === 'ALL' ? persons : persons.filter((p) => p.unit === unitFilter)),
    [unitFilter, refreshKey],
  );

  /* KPIs */
  const avgReadiness = useMemo(
    () => Math.round(filtered.reduce((s, p) => s + p.metrics.readinessScore, 0) / (filtered.length || 1)),
    [filtered],
  );
  const avgSleep = useMemo(
    () => Math.round(filtered.reduce((s, p) => s + p.metrics.sleepScore, 0) / (filtered.length || 1)),
    [filtered],
  );
  const fitPercent = useMemo(
    () => Math.round((filtered.filter((p) => p.metrics.readinessScore >= 60).length / (filtered.length || 1)) * 100),
    [filtered],
  );

  /* Alerts — personnel with readiness < 50 or stress > 80 */
  const activeAlerts = useMemo(() => {
    const generated = [];
    filtered.forEach((p) => {
      if (p.metrics.readinessScore < 50) {
        generated.push({
          id: `gen-r-${p.id}`,
          type: p.metrics.readinessScore < 30 ? 'critical' : 'warning',
          title: 'LOW_READINESS',
          message: `Readiness at ${p.metrics.readinessScore}`,
          personId: p.id,
          timestamp: new Date().toISOString(),
          acknowledged: false,
        });
      }
      if (p.metrics.stressScore > 80) {
        generated.push({
          id: `gen-s-${p.id}`,
          type: 'critical',
          title: 'HIGH_STRESS',
          message: `Stress at ${p.metrics.stressScore}`,
          personId: p.id,
          timestamp: new Date().toISOString(),
          acknowledged: false,
        });
      }
    });
    /* merge with static alerts relevant to filtered set */
    const filteredIds = new Set(filtered.map((p) => p.id));
    const staticRelevant = rawAlerts
      .filter((a) => !a.acknowledged && (a.personId === null || filteredIds.has(a.personId)))
      .map((a) => ({ ...a }));
    /* deduplicate by personId+type */
    const seen = new Set(staticRelevant.map((a) => `${a.personId}-${a.title}`));
    generated.forEach((g) => {
      const key = `${g.personId}-${g.title}`;
      if (!seen.has(key)) {
        staticRelevant.push(g);
        seen.add(key);
      }
    });
    return staticRelevant.sort((a, b) => {
      const order = { critical: 0, warning: 1, info: 2 };
      return (order[a.type] ?? 3) - (order[b.type] ?? 3);
    });
  }, [filtered]);

  /* Unit comparison chart data */
  const unitChartData = useMemo(() => {
    const map = {};
    persons.forEach((p) => {
      if (!map[p.unit]) map[p.unit] = { unit: p.unit, sum: 0, count: 0 };
      map[p.unit].sum += p.metrics.readinessScore;
      map[p.unit].count += 1;
    });
    return Object.values(map)
      .map((u) => ({ name: u.unit.replace(' Company', '').replace(' ', '\n'), avg: Math.round(u.sum / u.count) }))
      .sort((a, b) => b.avg - a.avg);
  }, [refreshKey]);

  const handleRefresh = useCallback(() => setRefreshKey((k) => k + 1), []);
  const personMap = useMemo(() => Object.fromEntries(persons.map((p) => [p.id, p])), []);

  return (
    <div className="min-h-screen bg-[#0a0b0d] text-[#e5e7eb] font-mono p-4 space-y-4">
      {/* Header bar */}
      <div className="flex items-center justify-between gap-3 flex-wrap">
        <h1 className="text-lg font-bold tracking-wide" style={{ color: CYAN }}>
          OPERATIONS OVERVIEW
        </h1>
        <div className="flex items-center gap-2">
          <select
            className="bg-[#111318] border border-[#1e2028] rounded px-2 py-1 text-xs font-mono text-[#e5e7eb] focus:outline-none focus:border-[#00D4FF]"
            value={unitFilter}
            onChange={(e) => setUnitFilter(e.target.value)}
          >
            <option value="ALL">All Units</option>
            {allUnits.map((u) => (
              <option key={u} value={u}>
                {u}
              </option>
            ))}
          </select>
          <button
            onClick={handleRefresh}
            className="bg-[#111318] border border-[#1e2028] rounded p-1.5 hover:border-[#00D4FF] transition-colors"
            title="Refresh"
          >
            <RefreshCw size={14} className="text-[#6b7280]" />
          </button>
        </div>
      </div>

      {/* KPI row */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        <KpiCard icon={Shield} label="Unit Readiness" value={avgReadiness} sub="avg score" color={CYAN} />
        <KpiCard
          icon={AlertTriangle}
          label="Active Alerts"
          value={activeAlerts.length}
          sub={`${activeAlerts.filter((a) => a.type === 'critical').length} critical`}
          color={RED}
        />
        <KpiCard icon={Heart} label="Personnel Fit" value={`${fitPercent}%`} sub="readiness ≥ 60" color={GREEN} />
        <KpiCard icon={Moon} label="Avg Sleep" value={avgSleep} sub="avg score" color={YELLOW} />
      </div>

      {/* Main body: heatmap + alerts */}
      <div className="grid grid-cols-1 lg:grid-cols-[1fr_320px] gap-4">
        {/* Heatmap */}
        <div className="bg-[#111318] border border-[#1e2028] rounded-lg p-4">
          <h2 className="text-xs uppercase tracking-wider text-[#6b7280] mb-3">
            Personnel Readiness Heatmap
            <span className="ml-2 text-[#e5e7eb]">{filtered.length} personnel</span>
          </h2>
          <div
            className="grid gap-1"
            style={{
              gridTemplateColumns: `repeat(auto-fill, minmax(36px, 1fr))`,
            }}
          >
            {filtered
              .slice()
              .sort((a, b) => a.metrics.readinessScore - b.metrics.readinessScore)
              .map((p) => (
                <HeatmapCell key={p.id} person={p} />
              ))}
          </div>
          {/* legend */}
          <div className="flex items-center gap-4 mt-3 text-[10px] text-[#6b7280] font-mono">
            <span className="flex items-center gap-1">
              <span className="w-3 h-3 rounded-sm" style={{ backgroundColor: RED }} /> &lt;50
            </span>
            <span className="flex items-center gap-1">
              <span className="w-3 h-3 rounded-sm" style={{ backgroundColor: YELLOW }} /> 50-69
            </span>
            <span className="flex items-center gap-1">
              <span className="w-3 h-3 rounded-sm" style={{ backgroundColor: GREEN }} /> ≥70
            </span>
          </div>
        </div>

        {/* Alerts panel */}
        <div className="bg-[#111318] border border-[#1e2028] rounded-lg p-4 max-h-[480px] overflow-y-auto">
          <h2 className="text-xs uppercase tracking-wider text-[#6b7280] mb-3">
            Active Alerts <span className="text-[#FF4757]">{activeAlerts.length}</span>
          </h2>
          {activeAlerts.length === 0 ? (
            <p className="text-xs text-[#6b7280]">No alerts</p>
          ) : (
            activeAlerts.map((a) => (
              <AlertRow key={a.id} alert={a} person={personMap[a.personId]} />
            ))
          )}
        </div>
      </div>

      {/* Unit comparison bar chart */}
      <div className="bg-[#111318] border border-[#1e2028] rounded-lg p-4">
        <h2 className="text-xs uppercase tracking-wider text-[#6b7280] mb-3">
          Unit Readiness Comparison
        </h2>
        <div className="h-56">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={unitChartData} margin={{ top: 4, right: 12, left: 0, bottom: 4 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1e2028" />
              <XAxis
                dataKey="name"
                tick={{ fill: '#6b7280', fontSize: 10, fontFamily: 'monospace' }}
                axisLine={{ stroke: '#1e2028' }}
                tickLine={false}
              />
              <YAxis
                domain={[0, 100]}
                tick={{ fill: '#6b7280', fontSize: 10, fontFamily: 'monospace' }}
                axisLine={{ stroke: '#1e2028' }}
                tickLine={false}
                width={30}
              />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#1e2028',
                  border: '1px solid #2a2d36',
                  borderRadius: 4,
                  fontFamily: 'monospace',
                  fontSize: 11,
                  color: '#e5e7eb',
                }}
              />
              <Bar dataKey="avg" radius={[3, 3, 0, 0]} maxBarSize={40}>
                {unitChartData.map((entry, i) => (
                  <Cell key={i} fill={statusColor(entry.avg)} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
