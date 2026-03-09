import { useState, useMemo } from 'react';
import {
  RadarChart, Radar, PolarGrid, PolarAngleAxis, PolarRadiusAxis,
  ResponsiveContainer, Tooltip, Legend,
} from 'recharts';
import { Users, AlertTriangle, ChevronRight } from 'lucide-react';
import { persons, units as unitsList } from '../../data/mockData';
import { motion } from 'framer-motion';
import { AnimatedSection, StaggerContainer, StaggerItem } from '../../components/AnimatedSection';

/* ── colour tokens ── */
const GREEN  = '#00E5A0';
const RED    = '#FF4757';
const YELLOW = '#FFA502';
const CYAN   = '#00D4FF';

const UNIT_COLORS = ['#00D4FF', '#00E5A0', '#FFA502', '#FF4757', '#A78BFA', '#F472B6', '#60A5FA', '#34D399'];

function statusColor(val) {
  if (val >= 70) return GREEN;
  if (val >= 50) return YELLOW;
  return RED;
}

/* ── Readiness gauge ── */
function RadialGauge({ value, size = 72 }) {
  const r = (size - 8) / 2;
  const circ = 2 * Math.PI * r;
  const pct = Math.min(value, 100) / 100;
  const color = statusColor(value);

  return (
    <svg width={size} height={size} className="shrink-0">
      <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="#1e2028" strokeWidth={5} />
      <circle
        cx={size / 2}
        cy={size / 2}
        r={r}
        fill="none"
        stroke={color}
        strokeWidth={5}
        strokeLinecap="round"
        strokeDasharray={`${circ * pct} ${circ * (1 - pct)}`}
        transform={`rotate(-90 ${size / 2} ${size / 2})`}
      />
      <text
        x={size / 2}
        y={size / 2}
        textAnchor="middle"
        dominantBaseline="central"
        fill={color}
        fontSize={16}
        fontFamily="monospace"
        fontWeight="bold"
      >
        {value}
      </text>
    </svg>
  );
}

/* ── Unit card ── */
function UnitCard({ unit, metrics, topAlert, onDrillDown }) {
  return (
    <motion.div
      whileHover={{ y: -4, scale: 1.01 }}
      transition={{ duration: 0.2 }}
      className="bg-[#111318] border border-[#1e2028] rounded-lg p-4 hover:border-[#2a2d36] transition-colors cursor-pointer"
      onClick={onDrillDown}
    >
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0 flex-1">
          <h3 className="text-sm font-bold font-mono truncate" style={{ color: CYAN }}>
            {unit.name}
          </h3>
          <p className="text-[10px] text-[#6b7280] font-mono mt-0.5">{unit.commander}</p>
        </div>
        <RadialGauge value={metrics.avgReadiness} size={64} />
      </div>

      <div className="grid grid-cols-3 gap-2 mt-3">
        <div>
          <p className="text-[10px] text-[#6b7280] font-mono">Personnel</p>
          <p className="text-sm font-mono font-bold flex items-center gap-1">
            <Users size={11} className="text-[#6b7280]" />
            {metrics.count}
          </p>
        </div>
        <div>
          <p className="text-[10px] text-[#6b7280] font-mono">Avg Sleep</p>
          <p className="text-sm font-mono font-bold">{metrics.avgSleep}</p>
        </div>
        <div>
          <p className="text-[10px] text-[#6b7280] font-mono">Avg Stress</p>
          <p className="text-sm font-mono font-bold" style={{ color: metrics.avgStress > 50 ? RED : '#e5e7eb' }}>
            {metrics.avgStress}
          </p>
        </div>
      </div>

      {topAlert && (
        <div className="mt-3 flex items-start gap-1.5 bg-[#0d0e12] rounded px-2 py-1.5">
          <AlertTriangle size={11} className="shrink-0 mt-0.5" style={{ color: RED }} />
          <p className="text-[10px] font-mono text-[#e5e7eb] leading-tight truncate">{topAlert}</p>
        </div>
      )}

      <div className="mt-2 flex items-center justify-end text-[10px] font-mono text-[#6b7280]">
        <span>Details</span>
        <ChevronRight size={12} />
      </div>
    </motion.div>
  );
}

/* ── Personnel list (drill-down) ── */
function PersonnelList({ unitName, members, onBack }) {
  return (
    <div className="space-y-3">
      <button
        onClick={onBack}
        className="text-xs font-mono flex items-center gap-1 text-[#6b7280] hover:text-[#e5e7eb] transition-colors"
      >
        <ChevronRight size={12} className="rotate-180" /> Back to units
      </button>
      <h2 className="text-sm font-bold font-mono" style={{ color: CYAN }}>
        {unitName}
        <span className="text-[#6b7280] font-normal ml-2">{members.length} personnel</span>
      </h2>
      <div className="bg-[#111318] border border-[#1e2028] rounded-lg overflow-x-auto">
        <table className="w-full border-collapse">
          <thead>
            <tr className="border-b border-[#1e2028]">
              {['Name', 'Readiness', 'Sleep', 'HRV', 'Stress', 'Cognitive', 'Status'].map((h) => (
                <th key={h} className="px-3 py-2 text-left text-[10px] uppercase tracking-wider text-[#6b7280] font-mono">
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {members
              .slice()
              .sort((a, b) => a.metrics.readinessScore - b.metrics.readinessScore)
              .map((p) => {
                const m = p.metrics;
                return (
                  <tr key={p.id} className="border-b border-[#1e2028] hover:bg-[#15161c]">
                    <td className="px-3 py-2 text-xs font-mono whitespace-nowrap">{p.name}</td>
                    <td className="px-3 py-2 text-xs font-mono" style={{ color: statusColor(m.readinessScore) }}>
                      {m.readinessScore}
                    </td>
                    <td className="px-3 py-2 text-xs font-mono">{m.sleepScore}</td>
                    <td className="px-3 py-2 text-xs font-mono">{m.hrvScore}</td>
                    <td className="px-3 py-2 text-xs font-mono" style={{ color: m.stressScore > 60 ? RED : '#e5e7eb' }}>
                      {m.stressScore}
                    </td>
                    <td className="px-3 py-2 text-xs font-mono">{m.cognitiveScore}</td>
                    <td className="px-3 py-2">
                      <span
                        className="w-2 h-2 rounded-full inline-block"
                        style={{ backgroundColor: statusColor(m.readinessScore) }}
                      />
                    </td>
                  </tr>
                );
              })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

/* ── Main component ── */
export default function Units() {
  const [selectedUnit, setSelectedUnit] = useState(null);

  /* compute per-unit metrics */
  const unitMetrics = useMemo(() => {
    const map = {};
    persons.forEach((p) => {
      if (!map[p.unit]) {
        map[p.unit] = {
          count: 0,
          sumReadiness: 0,
          sumSleep: 0,
          sumHrv: 0,
          sumStress: 0,
          sumCognitive: 0,
          members: [],
          alerts: [],
        };
      }
      const u = map[p.unit];
      u.count++;
      u.sumReadiness += p.metrics.readinessScore;
      u.sumSleep += p.metrics.sleepScore;
      u.sumHrv += p.metrics.hrvScore;
      u.sumStress += p.metrics.stressScore;
      u.sumCognitive += p.metrics.cognitiveScore;
      u.members.push(p);
      if (p.metrics.readinessScore < 50) {
        u.alerts.push(`${p.name}: readiness ${p.metrics.readinessScore}`);
      }
      if (p.metrics.stressScore > 80) {
        u.alerts.push(`${p.name}: stress ${p.metrics.stressScore}`);
      }
    });

    return Object.entries(map).map(([name, u]) => ({
      name,
      count: u.count,
      avgReadiness: Math.round(u.sumReadiness / u.count),
      avgSleep: Math.round(u.sumSleep / u.count),
      avgHrv: Math.round(u.sumHrv / u.count),
      avgStress: Math.round(u.sumStress / u.count),
      avgCognitive: Math.round(u.sumCognitive / u.count),
      members: u.members,
      topAlert: u.alerts[0] || null,
    }));
  }, []);

  /* Unit info from unitsList for commander names */
  const unitsInfo = useMemo(() => Object.fromEntries(unitsList.map((u) => [u.name, u])), []);

  /* radar chart data */
  const radarData = useMemo(() => {
    const metrics = ['Readiness', 'Sleep', 'HRV', 'Stress (inv)', 'Cognitive'];
    return metrics.map((metric) => {
      const row = { metric };
      unitMetrics.forEach((u) => {
        if (metric === 'Readiness') row[u.name] = u.avgReadiness;
        else if (metric === 'Sleep') row[u.name] = u.avgSleep;
        else if (metric === 'HRV') row[u.name] = u.avgHrv;
        else if (metric === 'Stress (inv)') row[u.name] = 100 - u.avgStress;
        else if (metric === 'Cognitive') row[u.name] = u.avgCognitive;
      });
      return row;
    });
  }, [unitMetrics]);

  /* drill down view */
  if (selectedUnit) {
    const um = unitMetrics.find((u) => u.name === selectedUnit);
    if (um) {
      return (
        <div className="min-h-screen bg-[#0a0b0d] text-[#e5e7eb] font-mono p-4">
          <PersonnelList
            unitName={selectedUnit}
            members={um.members}
            onBack={() => setSelectedUnit(null)}
          />
        </div>
      );
    }
  }

  return (
    <div className="min-h-screen bg-[#0a0b0d] text-[#e5e7eb] font-mono p-4 space-y-4">
      <AnimatedSection>
      <h1 className="text-lg font-bold tracking-wide" style={{ color: CYAN }}>
        UNIT OVERVIEW
      </h1>
      </AnimatedSection>

      {/* Unit cards grid */}
      <AnimatedSection delay={0.05}>
      <StaggerContainer className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-3">
        {unitMetrics
          .sort((a, b) => a.avgReadiness - b.avgReadiness)
          .map((um) => {
            const info = unitsInfo[um.name];
            return (
              <StaggerItem key={um.name}>
              <UnitCard
                unit={{
                  name: um.name,
                  commander: info?.commander || '—',
                }}
                metrics={um}
                topAlert={um.topAlert}
                onDrillDown={() => setSelectedUnit(um.name)}
              />
              </StaggerItem>
            );
          })}
      </StaggerContainer>
      </AnimatedSection>

      {/* Radar comparison chart */}
      <AnimatedSection delay={0.1}>
      <div className="bg-[#111318] border border-[#1e2028] rounded-lg p-4">
        <h2 className="text-xs uppercase tracking-wider text-[#6b7280] mb-3">
          Unit Comparison — Key Metrics
        </h2>
        <div className="h-80">
          <ResponsiveContainer width="100%" height="100%">
            <RadarChart cx="50%" cy="50%" outerRadius="70%" data={radarData}>
              <PolarGrid stroke="#1e2028" />
              <PolarAngleAxis
                dataKey="metric"
                tick={{ fill: '#6b7280', fontSize: 10, fontFamily: 'monospace' }}
              />
              <PolarRadiusAxis
                angle={90}
                domain={[0, 100]}
                tick={{ fill: '#6b7280', fontSize: 9, fontFamily: 'monospace' }}
                axisLine={false}
              />
              {unitMetrics.map((u, i) => (
                <Radar
                  key={u.name}
                  name={u.name}
                  dataKey={u.name}
                  stroke={UNIT_COLORS[i % UNIT_COLORS.length]}
                  fill={UNIT_COLORS[i % UNIT_COLORS.length]}
                  fillOpacity={0.08}
                  strokeWidth={1.5}
                />
              ))}
              <Legend
                wrapperStyle={{ fontFamily: 'monospace', fontSize: 10, color: '#6b7280' }}
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
            </RadarChart>
          </ResponsiveContainer>
        </div>
      </div>
      </AnimatedSection>
    </div>
  );
}
