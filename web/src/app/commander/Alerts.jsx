import { useState, useMemo, useCallback } from 'react';
import { AlertTriangle, Bell, Info, Filter, Check, X } from 'lucide-react';
import { persons, alerts as staticAlerts } from '../../data/mockData';
import { motion } from 'framer-motion';
import { AnimatedSection } from '../../components/AnimatedSection';

/* ── colour tokens ── */
const GREEN  = '#00E5A0';
const RED    = '#FF4757';
const YELLOW = '#FFA502';
const CYAN   = '#00D4FF';

/* ── Alert type definitions ── */
const ALERT_TYPES = {
  LOW_READINESS: { label: 'Low Readiness', icon: AlertTriangle, color: RED },
  HIGH_STRESS:   { label: 'High Stress',   icon: AlertTriangle, color: RED },
  POOR_SLEEP:    { label: 'Poor Sleep',    icon: Bell,          color: YELLOW },
  INJURY_RISK:   { label: 'Injury Risk',   icon: AlertTriangle, color: RED },
  INFO:          { label: 'Info',           icon: Info,          color: CYAN },
};

const SEVERITY_CONFIG = {
  critical: { label: 'Critical', color: RED, bg: '#FF475715' },
  warning:  { label: 'Warning',  color: YELLOW, bg: '#FFA50215' },
  info:     { label: 'Info',     color: CYAN, bg: '#00D4FF15' },
};

/* ── Generate alerts from personnel data ── */
function generateAlerts() {
  const generated = [];
  const now = Date.now();

  persons.forEach((p, idx) => {
    const m = p.metrics;

    if (m.readinessScore < 50) {
      const severity = m.readinessScore < 30 ? 'critical' : 'warning';
      generated.push({
        id: `auto-read-${p.id}`,
        type: 'LOW_READINESS',
        severity,
        personId: p.id,
        personName: p.name,
        unit: p.unit,
        message: `Readiness score at ${m.readinessScore}. ${severity === 'critical' ? 'Immediate review required.' : 'Monitor closely.'}`,
        timestamp: new Date(now - (idx * 7 + 3) * 60000).toISOString(),
        acknowledged: false,
      });
    }

    if (m.stressScore > 80) {
      generated.push({
        id: `auto-stress-${p.id}`,
        type: 'HIGH_STRESS',
        severity: 'critical',
        personId: p.id,
        personName: p.name,
        unit: p.unit,
        message: `Stress level at ${m.stressScore}. Sustained high stress detected — intervention recommended.`,
        timestamp: new Date(now - (idx * 5 + 10) * 60000).toISOString(),
        acknowledged: false,
      });
    }

    if (m.sleepScore < 45) {
      generated.push({
        id: `auto-sleep-${p.id}`,
        type: 'POOR_SLEEP',
        severity: m.sleepScore < 30 ? 'critical' : 'warning',
        personId: p.id,
        personName: p.name,
        unit: p.unit,
        message: `Sleep score at ${m.sleepScore} — chronic poor sleep pattern detected.`,
        timestamp: new Date(now - (idx * 11 + 20) * 60000).toISOString(),
        acknowledged: false,
      });
    }

    if (m.readinessScore < 40 && m.stressScore > 65) {
      generated.push({
        id: `auto-injury-${p.id}`,
        type: 'INJURY_RISK',
        severity: 'warning',
        personId: p.id,
        personName: p.name,
        unit: p.unit,
        message: `Compound risk: low readiness (${m.readinessScore}) combined with elevated stress (${m.stressScore}). Restrict high-intensity activities.`,
        timestamp: new Date(now - (idx * 9 + 15) * 60000).toISOString(),
        acknowledged: false,
      });
    }
  });

  /* Map static alerts into same shape */
  staticAlerts.forEach((sa) => {
    const person = persons.find((p) => p.id === sa.personId);
    let alertType = 'INFO';
    if (sa.title.toLowerCase().includes('stress')) alertType = 'HIGH_STRESS';
    else if (sa.title.toLowerCase().includes('readiness')) alertType = 'LOW_READINESS';
    else if (sa.title.toLowerCase().includes('sleep')) alertType = 'POOR_SLEEP';
    else if (sa.title.toLowerCase().includes('hrv')) alertType = 'INJURY_RISK';

    generated.push({
      id: sa.id,
      type: alertType,
      severity: sa.type,
      personId: sa.personId,
      personName: person?.name || 'System',
      unit: person?.unit || '—',
      message: sa.message,
      timestamp: sa.timestamp,
      acknowledged: sa.acknowledged,
    });
  });

  /* Deduplicate by personId + type, keep the one with earlier timestamp */
  const seen = new Map();
  generated.forEach((a) => {
    const key = `${a.personId}-${a.type}`;
    if (!seen.has(key) || new Date(a.timestamp) < new Date(seen.get(key).timestamp)) {
      seen.set(key, a);
    }
  });

  return Array.from(seen.values()).sort(
    (a, b) => new Date(b.timestamp) - new Date(a.timestamp),
  );
}

/* ── Severity badge ── */
function SeverityBadge({ severity }) {
  const cfg = SEVERITY_CONFIG[severity] || SEVERITY_CONFIG.info;
  return (
    <span
      className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-mono font-bold uppercase tracking-wider"
      style={{ color: cfg.color, backgroundColor: cfg.bg }}
    >
      {cfg.label}
    </span>
  );
}

/* ── Type badge ── */
function TypeBadge({ type }) {
  const cfg = ALERT_TYPES[type] || ALERT_TYPES.INFO;
  const Icon = cfg.icon;
  return (
    <span className="inline-flex items-center gap-1 text-[10px] font-mono" style={{ color: cfg.color }}>
      <Icon size={11} />
      {cfg.label}
    </span>
  );
}

/* ── Format timestamp ── */
function formatTimestamp(ts) {
  const d = new Date(ts);
  const mins = Math.round((Date.now() - d) / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.round(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
}

/* ── Alert row ── */
function AlertRow({ alert, onAcknowledge }) {
  const acked = alert.acknowledged;
  return (
    <motion.div
      initial={{ opacity: 0, x: -10 }}
      animate={{ opacity: 1, x: 0 }}
      whileHover={!acked ? { x: 4 } : {}}
      className={`flex items-start gap-3 px-4 py-3 border-b border-[#1e2028] last:border-0 transition-colors ${
        acked ? 'opacity-50' : 'hover:bg-[#15161c]'
      }`}
    >
      {/* left indicator */}
      <span
        className="mt-1.5 shrink-0 w-1.5 h-1.5 rounded-full"
        style={{
          backgroundColor: acked
            ? '#6b7280'
            : (SEVERITY_CONFIG[alert.severity]?.color || CYAN),
        }}
      />

      {/* content */}
      <div className="flex-1 min-w-0 space-y-1">
        <div className="flex items-center gap-2 flex-wrap">
          <TypeBadge type={alert.type} />
          <SeverityBadge severity={alert.severity} />
          <span className="text-[10px] font-mono text-[#6b7280]">
            {formatTimestamp(alert.timestamp)}
          </span>
        </div>
        <p className="text-xs font-mono text-[#e5e7eb]">
          <span className="font-bold">{alert.personName}</span>
          <span className="text-[#6b7280]"> — {alert.unit}</span>
        </p>
        <p className="text-[11px] font-mono text-[#9ca3af] leading-relaxed">{alert.message}</p>
      </div>

      {/* acknowledge button */}
      {!acked && (
        <button
          onClick={() => onAcknowledge(alert.id)}
          className="shrink-0 mt-1 flex items-center gap-1 text-[10px] font-mono px-2 py-1 rounded border border-[#1e2028] text-[#6b7280] hover:border-[#00E5A0] hover:text-[#00E5A0] transition-colors"
          title="Acknowledge"
        >
          <Check size={11} /> ACK
        </button>
      )}
      {acked && (
        <span className="shrink-0 mt-1 text-[10px] font-mono text-[#6b7280] flex items-center gap-1">
          <Check size={11} /> Acked
        </span>
      )}
    </motion.div>
  );
}

/* ── Main component ── */
export default function Alerts() {
  const [alerts, setAlerts] = useState(() => generateAlerts());
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [severityFilter, setSeverityFilter] = useState('ALL');
  const [showAcked, setShowAcked] = useState(false);

  const allTypes = useMemo(() => [...new Set(alerts.map((a) => a.type))].sort(), [alerts]);
  const allSeverities = ['critical', 'warning', 'info'];

  const filtered = useMemo(() => {
    let list = alerts;
    if (typeFilter !== 'ALL') list = list.filter((a) => a.type === typeFilter);
    if (severityFilter !== 'ALL') list = list.filter((a) => a.severity === severityFilter);
    if (!showAcked) list = list.filter((a) => !a.acknowledged);
    return list;
  }, [alerts, typeFilter, severityFilter, showAcked]);

  const handleAcknowledge = useCallback((id) => {
    setAlerts((prev) =>
      prev.map((a) => (a.id === id ? { ...a, acknowledged: true } : a)),
    );
  }, []);

  const handleAcknowledgeAll = useCallback(() => {
    setAlerts((prev) => prev.map((a) => ({ ...a, acknowledged: true })));
  }, []);

  /* stats */
  const totalUnacked = alerts.filter((a) => !a.acknowledged).length;
  const criticalCount = alerts.filter((a) => !a.acknowledged && a.severity === 'critical').length;
  const warningCount = alerts.filter((a) => !a.acknowledged && a.severity === 'warning').length;

  return (
    <div className="min-h-screen bg-[#0a0b0d] text-[#e5e7eb] font-mono p-4 space-y-4">
      {/* Header */}
      <AnimatedSection>
      <div className="flex items-center justify-between gap-3 flex-wrap">
        <h1 className="text-lg font-bold tracking-wide" style={{ color: CYAN }}>
          ALERTS
        </h1>
        <div className="flex items-center gap-2 flex-wrap">
          {/* stats pills */}
          <span className="text-[10px] font-mono px-2 py-0.5 rounded" style={{ color: RED, backgroundColor: '#FF475715' }}>
            {criticalCount} critical
          </span>
          <span className="text-[10px] font-mono px-2 py-0.5 rounded" style={{ color: YELLOW, backgroundColor: '#FFA50215' }}>
            {warningCount} warning
          </span>
          <span className="text-[10px] font-mono px-2 py-0.5 rounded text-[#6b7280] bg-[#111318]">
            {totalUnacked} unacknowledged
          </span>
        </div>
      </div>
      </AnimatedSection>

      {/* Filters bar */}
      <AnimatedSection delay={0.05}>
      <div className="flex items-center gap-2 flex-wrap">
        <Filter size={13} className="text-[#6b7280]" />

        <select
          className="bg-[#111318] border border-[#1e2028] rounded px-2 py-1 text-xs font-mono text-[#e5e7eb] focus:outline-none focus:border-[#00D4FF]"
          value={typeFilter}
          onChange={(e) => setTypeFilter(e.target.value)}
        >
          <option value="ALL">All Types</option>
          {allTypes.map((t) => (
            <option key={t} value={t}>
              {ALERT_TYPES[t]?.label || t}
            </option>
          ))}
        </select>

        <select
          className="bg-[#111318] border border-[#1e2028] rounded px-2 py-1 text-xs font-mono text-[#e5e7eb] focus:outline-none focus:border-[#00D4FF]"
          value={severityFilter}
          onChange={(e) => setSeverityFilter(e.target.value)}
        >
          <option value="ALL">All Severity</option>
          {allSeverities.map((s) => (
            <option key={s} value={s}>
              {s.charAt(0).toUpperCase() + s.slice(1)}
            </option>
          ))}
        </select>

        <label className="flex items-center gap-1.5 text-xs font-mono text-[#6b7280] cursor-pointer select-none">
          <input
            type="checkbox"
            checked={showAcked}
            onChange={(e) => setShowAcked(e.target.checked)}
            className="accent-[#00D4FF] w-3 h-3"
          />
          Show acknowledged
        </label>

        <div className="flex-1" />

        {totalUnacked > 0 && (
          <button
            onClick={handleAcknowledgeAll}
            className="flex items-center gap-1 text-[10px] font-mono px-2 py-1 rounded border border-[#1e2028] text-[#6b7280] hover:border-[#00E5A0] hover:text-[#00E5A0] transition-colors"
          >
            <Check size={11} /> Acknowledge All
          </button>
        )}
      </div>
      </AnimatedSection>

      {/* Alert list */}
      <AnimatedSection delay={0.1}>
      <div className="bg-[#111318] border border-[#1e2028] rounded-lg">
        {filtered.length === 0 ? (
          <div className="px-4 py-8 text-center">
            <p className="text-sm text-[#6b7280] font-mono">No alerts matching filters</p>
          </div>
        ) : (
          filtered.map((a) => (
            <AlertRow key={a.id} alert={a} onAcknowledge={handleAcknowledge} />
          ))
        )}
      </div>
      </AnimatedSection>

      {/* Count footer */}
      <p className="text-[10px] text-[#6b7280] font-mono text-right">
        Showing {filtered.length} of {alerts.length} total alerts
      </p>
    </div>
  );
}
