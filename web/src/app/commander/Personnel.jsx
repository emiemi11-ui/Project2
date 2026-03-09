import { useState, useMemo, useCallback } from 'react';
import {
  LineChart, Line, XAxis, YAxis, ResponsiveContainer, Tooltip,
} from 'recharts';
import { Search, Download, ChevronDown, ChevronRight, Users } from 'lucide-react';
import { persons } from '../../data/mockData';
import { motion } from 'framer-motion';
import { AnimatedSection } from '../../components/AnimatedSection';

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

function statusLabel(readiness) {
  if (readiness >= 70) return 'Fit';
  if (readiness >= 50) return 'Caution';
  return 'At Risk';
}

/* ── Mini sparkline chart ── */
function MiniTrend({ data, color, label }) {
  return (
    <div className="flex-1 min-w-0">
      <p className="text-[10px] font-mono text-[#6b7280] mb-1">{label}</p>
      <div className="h-12">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data}>
            <Line type="monotone" dataKey="value" stroke={color} strokeWidth={1.5} dot={false} />
            <XAxis dataKey="date" hide />
            <YAxis domain={['auto', 'auto']} hide />
            <Tooltip
              contentStyle={{
                backgroundColor: '#1e2028',
                border: '1px solid #2a2d36',
                borderRadius: 4,
                fontFamily: 'monospace',
                fontSize: 10,
                color: '#e5e7eb',
              }}
              labelFormatter={(v) => v}
              formatter={(v) => [v, label]}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

/* ── Sortable column header ── */
function SortHeader({ label, field, sortField, sortDir, onSort }) {
  const active = sortField === field;
  return (
    <button
      className={`text-[10px] uppercase tracking-wider font-mono text-left whitespace-nowrap flex items-center gap-0.5 ${
        active ? 'text-[#00D4FF]' : 'text-[#6b7280]'
      } hover:text-[#e5e7eb] transition-colors`}
      onClick={() => onSort(field)}
    >
      {label}
      {active && <span className="text-[8px]">{sortDir === 'asc' ? '▲' : '▼'}</span>}
    </button>
  );
}

/* ── Expanded detail row ── */
function PersonDetail({ person }) {
  const m = person.metrics;
  const h = person.history;
  return (
    <tr>
      <td colSpan={8} className="bg-[#0d0e12] border-b border-[#1e2028] px-4 py-3">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div>
            <p className="text-[10px] text-[#6b7280] font-mono">Age</p>
            <p className="text-sm font-mono">{person.age}</p>
          </div>
          <div>
            <p className="text-[10px] text-[#6b7280] font-mono">Steps</p>
            <p className="text-sm font-mono">{m.steps.toLocaleString()}</p>
          </div>
          <div>
            <p className="text-[10px] text-[#6b7280] font-mono">Screen Time</p>
            <p className="text-sm font-mono">{Math.round(m.screenTime / 60)}h {m.screenTime % 60}m</p>
          </div>
          <div>
            <p className="text-[10px] text-[#6b7280] font-mono">Cognitive</p>
            <p className="text-sm font-mono">{m.cognitiveScore}</p>
          </div>
        </div>
        <div className="mt-3 grid grid-cols-2 md:grid-cols-4 gap-3">
          <MiniTrend data={h.readiness} color={CYAN} label="Readiness" />
          <MiniTrend data={h.sleep} color={YELLOW} label="Sleep" />
          <MiniTrend data={h.hrv} color={GREEN} label="HRV" />
          <MiniTrend data={h.stress} color={RED} label="Stress" />
        </div>
      </td>
    </tr>
  );
}

/* ── Main component ── */
export default function Personnel() {
  const [search, setSearch] = useState('');
  const [sortField, setSortField] = useState('readinessScore');
  const [sortDir, setSortDir] = useState('desc');
  const [expandedId, setExpandedId] = useState(null);
  const [groupByUnit, setGroupByUnit] = useState(false);

  const handleSort = useCallback(
    (field) => {
      if (sortField === field) {
        setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
      } else {
        setSortField(field);
        setSortDir('desc');
      }
    },
    [sortField],
  );

  const filtered = useMemo(() => {
    let list = persons.filter((p) =>
      p.name.toLowerCase().includes(search.toLowerCase()),
    );

    const getValue = (p, f) => {
      if (f === 'name') return p.name;
      if (f === 'unit') return p.unit;
      return p.metrics[f] ?? 0;
    };

    list.sort((a, b) => {
      const av = getValue(a, sortField);
      const bv = getValue(b, sortField);
      if (typeof av === 'string') {
        return sortDir === 'asc' ? av.localeCompare(bv) : bv.localeCompare(av);
      }
      return sortDir === 'asc' ? av - bv : bv - av;
    });

    return list;
  }, [search, sortField, sortDir]);

  /* Group by unit */
  const grouped = useMemo(() => {
    if (!groupByUnit) return null;
    const map = {};
    filtered.forEach((p) => {
      if (!map[p.unit]) map[p.unit] = [];
      map[p.unit].push(p);
    });
    return Object.entries(map).sort(([a], [b]) => a.localeCompare(b));
  }, [filtered, groupByUnit]);

  const handleExport = useCallback(() => {
    const header = 'Name,Unit,Readiness,Sleep,HRV,Stress,Status\n';
    const rows = filtered
      .map((p) => {
        const m = p.metrics;
        return `"${p.name}","${p.unit}",${m.readinessScore},${m.sleepScore},${m.hrvScore},${m.stressScore},"${statusLabel(m.readinessScore)}"`;
      })
      .join('\n');
    const blob = new Blob([header + rows], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'personnel_export.csv';
    a.click();
    URL.revokeObjectURL(url);
  }, [filtered]);

  const toggleExpand = useCallback(
    (id) => setExpandedId((prev) => (prev === id ? null : id)),
    [],
  );

  /* table row renderer */
  const renderRow = (p) => {
    const m = p.metrics;
    const isExpanded = expandedId === p.id;
    return [
      <tr
        key={p.id}
        className="border-b border-[#1e2028] hover:bg-[#15161c] cursor-pointer transition-colors"
        onClick={() => toggleExpand(p.id)}
      >
        <td className="px-3 py-2 text-xs">
          {isExpanded ? (
            <ChevronDown size={12} className="text-[#6b7280]" />
          ) : (
            <ChevronRight size={12} className="text-[#6b7280]" />
          )}
        </td>
        <td className="px-3 py-2 text-xs font-mono whitespace-nowrap">{p.name}</td>
        <td className="px-3 py-2 text-xs font-mono text-[#6b7280] whitespace-nowrap">{p.unit}</td>
        <td className="px-3 py-2 text-xs font-mono" style={{ color: statusColor(m.readinessScore) }}>
          {m.readinessScore}
        </td>
        <td className="px-3 py-2 text-xs font-mono">{m.sleepScore}</td>
        <td className="px-3 py-2 text-xs font-mono">{m.hrvScore}</td>
        <td className="px-3 py-2 text-xs font-mono" style={{ color: m.stressScore > 60 ? RED : '#e5e7eb' }}>
          {m.stressScore}
        </td>
        <td className="px-3 py-2">
          <span className="flex items-center gap-1.5">
            <span
              className="w-2 h-2 rounded-full shrink-0"
              style={{ backgroundColor: statusColor(m.readinessScore) }}
            />
            <span
              className="text-[10px] font-mono"
              style={{ color: statusColor(m.readinessScore) }}
            >
              {statusLabel(m.readinessScore)}
            </span>
          </span>
        </td>
      </tr>,
      isExpanded && <PersonDetail key={`${p.id}-detail`} person={p} />,
    ];
  };

  const columns = [
    { label: '', field: null },
    { label: 'Name', field: 'name' },
    { label: 'Unit', field: 'unit' },
    { label: 'Readiness', field: 'readinessScore' },
    { label: 'Sleep', field: 'sleepScore' },
    { label: 'HRV', field: 'hrvScore' },
    { label: 'Stress', field: 'stressScore' },
    { label: 'Status', field: 'readinessScore' },
  ];

  return (
    <div className="min-h-screen bg-[#0a0b0d] text-[#e5e7eb] font-mono p-4 space-y-4">
      {/* Header */}
      <AnimatedSection>
      <div className="flex items-center justify-between gap-3 flex-wrap">
        <h1 className="text-lg font-bold tracking-wide" style={{ color: CYAN }}>
          PERSONNEL
        </h1>
        <div className="flex items-center gap-2 flex-wrap">
          {/* search */}
          <div className="relative">
            <Search size={13} className="absolute left-2 top-1/2 -translate-y-1/2 text-[#6b7280]" />
            <input
              type="text"
              placeholder="Search name..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="bg-[#111318] border border-[#1e2028] rounded pl-7 pr-2 py-1 text-xs font-mono text-[#e5e7eb] placeholder-[#6b7280] w-44 focus:outline-none focus:border-[#00D4FF]"
            />
          </div>
          {/* group toggle */}
          <button
            onClick={() => setGroupByUnit((v) => !v)}
            className={`flex items-center gap-1 text-xs font-mono px-2 py-1 rounded border transition-colors ${
              groupByUnit
                ? 'bg-[#00D4FF15] border-[#00D4FF] text-[#00D4FF]'
                : 'bg-[#111318] border-[#1e2028] text-[#6b7280] hover:border-[#00D4FF]'
            }`}
          >
            <Users size={12} /> Group
          </button>
          {/* export */}
          <button
            onClick={handleExport}
            className="flex items-center gap-1 text-xs font-mono px-2 py-1 rounded border bg-[#111318] border-[#1e2028] text-[#6b7280] hover:border-[#00D4FF] transition-colors"
          >
            <Download size={12} /> Export
          </button>
        </div>
      </div>
      </AnimatedSection>

      {/* Count */}
      <AnimatedSection delay={0.05}>
      <p className="text-[11px] text-[#6b7280] font-mono">
        Showing {filtered.length} of {persons.length} personnel
      </p>
      </AnimatedSection>

      {/* Table */}
      <AnimatedSection delay={0.1}>
      <div className="bg-[#111318] border border-[#1e2028] rounded-lg overflow-x-auto">
        <table className="w-full border-collapse">
          <thead>
            <tr className="border-b border-[#1e2028]">
              {columns.map((col, i) => (
                <th key={i} className="px-3 py-2 text-left">
                  {col.field ? (
                    <SortHeader
                      label={col.label}
                      field={col.field}
                      sortField={sortField}
                      sortDir={sortDir}
                      onSort={handleSort}
                    />
                  ) : null}
                </th>
              ))}
            </tr>
          </thead>

          {groupByUnit && grouped ? (
            grouped.map(([unit, members]) => (
              <tbody key={unit}>
                <tr>
                  <td
                    colSpan={8}
                    className="px-3 py-1.5 text-[10px] uppercase tracking-wider font-mono bg-[#0d0e12] border-b border-[#1e2028]"
                    style={{ color: CYAN }}
                  >
                    {unit}{' '}
                    <span className="text-[#6b7280]">({members.length})</span>
                  </td>
                </tr>
                {members.map(renderRow)}
              </tbody>
            ))
          ) : (
            <tbody>{filtered.map(renderRow)}</tbody>
          )}
        </table>
      </div>
      </AnimatedSection>
    </div>
  );
}
