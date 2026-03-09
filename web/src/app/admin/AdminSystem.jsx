import { useState, useMemo } from "react";
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from "recharts";
import { persons, accounts } from "../../data/mockData";
import { motion } from "framer-motion";
import { AnimatedSection } from "../../components/AnimatedSection";

/* ——————————————————————————————————————————————
   Admin System — System overview dashboard
   Background: #0A0F1A  Accent: #00E5A0
   —————————————————————————————————————————————— */

const ACCENT = "#00E5A0";
const BG = "#0A0F1A";
const CARD_BG = "#111827";
const BORDER = "#1F2937";

const ROLE_COLORS = {
  admin: "#00E5A0",
  commander: "#3B82F6",
  physician: "#8B5CF6",
  psychologist: "#EC4899",
  trainer: "#F59E0B",
  soldier: "#06B6D4",
  patient: "#EF4444",
};

const PLAN_COLORS = {
  Basic: "#6B7280",
  Pro: "#3B82F6",
  Enterprise: "#00E5A0",
};

/* ——— Generate mock user growth (last 12 months) ——— */
function generateUserGrowth() {
  const data = [];
  const now = new Date();
  let total = 8;
  for (let i = 11; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
    const label = d.toLocaleString("default", { month: "short", year: "2-digit" });
    total += Math.floor(Math.random() * 4) + 1;
    data.push({ month: label, users: total });
  }
  return data;
}

/* ——— Generate mock recent activity ——— */
function generateRecentActivity() {
  const actions = [
    "Logged in", "Synced biometric data", "Updated profile",
    "Exported report", "Changed password", "Viewed dashboard",
    "Added appointment", "Submitted mood entry", "Completed training",
    "Reviewed patient file",
  ];
  const entries = [];
  const now = Date.now();
  for (let i = 0; i < 15; i++) {
    const person = persons[Math.floor(Math.random() * persons.length)];
    const action = actions[Math.floor(Math.random() * actions.length)];
    const ts = new Date(now - i * 1000 * 60 * (Math.floor(Math.random() * 30) + 5));
    entries.push({
      id: `activity-${i}`,
      timestamp: ts.toISOString(),
      userName: person.name,
      role: person.role,
      action,
    });
  }
  return entries.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
}

/* ——— Stat Card ——— */
function StatCard({ label, value, sub, icon }) {
  return (
    <motion.div whileHover={{ y: -2, scale: 1.02 }} transition={{ duration: 0.2 }} className="bg-[#111827] border border-[#1F2937] rounded-xl p-5 hover:border-[#374151] transition-colors">
      <div className="flex items-center justify-between mb-3">
        <span className="text-xs text-gray-400 uppercase tracking-wider font-medium">{label}</span>
        <span className="text-lg">{icon}</span>
      </div>
      <p className="text-3xl font-bold text-white">{value}</p>
      {sub && <p className="text-xs text-gray-500 mt-1">{sub}</p>}
    </motion.div>
  );
}

/* ——— System Health Indicator ——— */
function HealthIndicator({ label, status }) {
  const colors = {
    operational: { bg: "bg-green-500/20", text: "text-green-400", dot: "bg-green-400", label: "Operational" },
    degraded: { bg: "bg-yellow-500/20", text: "text-yellow-400", dot: "bg-yellow-400", label: "Degraded" },
    down: { bg: "bg-red-500/20", text: "text-red-400", dot: "bg-red-400", label: "Down" },
  };
  const c = colors[status] || colors.operational;
  return (
    <div className={`flex items-center justify-between px-4 py-3 rounded-lg ${c.bg}`}>
      <span className="text-sm text-gray-300">{label}</span>
      <span className={`flex items-center gap-2 text-xs font-medium ${c.text}`}>
        <span className={`w-2 h-2 rounded-full ${c.dot} animate-pulse`} />
        {c.label}
      </span>
    </div>
  );
}

/* ——— Custom Tooltip for charts ——— */
function ChartTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-[#1F2937] border border-gray-700 rounded-lg px-3 py-2 shadow-xl">
      <p className="text-xs text-gray-400">{label}</p>
      {payload.map((p, i) => (
        <p key={i} className="text-sm font-semibold" style={{ color: p.color || ACCENT }}>
          {p.name}: {p.value}
        </p>
      ))}
    </div>
  );
}

/* ——— Pie Label ——— */
function renderPieLabel({ cx, cy, midAngle, innerRadius, outerRadius, name, percent }) {
  const RADIAN = Math.PI / 180;
  const radius = innerRadius + (outerRadius - innerRadius) * 1.4;
  const x = cx + radius * Math.cos(-midAngle * RADIAN);
  const y = cy + radius * Math.sin(-midAngle * RADIAN);
  if (percent < 0.05) return null;
  return (
    <text x={x} y={y} fill="#9CA3AF" fontSize={11} textAnchor={x > cx ? "start" : "end"} dominantBaseline="central">
      {name} ({(percent * 100).toFixed(0)}%)
    </text>
  );
}

export default function AdminSystem() {
  const [userGrowth] = useState(() => generateUserGrowth());
  const [recentActivity] = useState(() => generateRecentActivity());

  /* ——— Computed data ——— */
  const totalUsers = persons.length;
  const activeToday = Math.floor(totalUsers * 0.75);
  const dataPoints = totalUsers * 7 * 6;
  const storageUsed = "2.4 GB";

  /* Users by role */
  const roleData = useMemo(() => {
    const counts = {};
    persons.forEach((p) => {
      counts[p.role] = (counts[p.role] || 0) + 1;
    });
    return Object.entries(counts).map(([name, value]) => ({ name, value }));
  }, []);

  /* Users by plan */
  const planData = useMemo(() => {
    const plans = {};
    persons.forEach((_, i) => {
      const plan = i % 3 === 0 ? "Enterprise" : i % 3 === 1 ? "Pro" : "Basic";
      plans[plan] = (plans[plan] || 0) + 1;
    });
    return Object.entries(plans).map(([name, value]) => ({ name, value }));
  }, []);

  function formatTimestamp(iso) {
    const d = new Date(iso);
    return d.toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" }) +
      " " + d.toLocaleDateString("en-US", { month: "short", day: "numeric" });
  }

  return (
    <div className="min-h-screen bg-[#0A0F1A] text-white p-6 space-y-6">
      {/* Header */}
      <AnimatedSection>
      <div>
        <h1 className="text-2xl font-bold">System Overview</h1>
        <p className="text-gray-400 text-sm mt-1">VitaNova platform health and analytics</p>
      </div>
      </AnimatedSection>

      {/* Stats Row */}
      <AnimatedSection delay={0.05}>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Total Users" value={totalUsers} sub={`${accounts.length} with accounts`} icon="👥" />
        <StatCard label="Active Today" value={activeToday} sub={`${Math.round((activeToday / totalUsers) * 100)}% of total`} icon="🟢" />
        <StatCard label="Data Points Collected" value={dataPoints.toLocaleString()} sub="Last 7 days" icon="📊" />
        <StatCard label="Storage Used" value={storageUsed} sub="of 10 GB allocated" icon="💾" />
      </div>
      </AnimatedSection>

      {/* Charts Row */}
      <AnimatedSection delay={0.1}>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* User Growth Line Chart */}
        <div className="bg-[#111827] border border-[#1F2937] rounded-xl p-5">
          <h2 className="text-sm font-semibold text-gray-300 mb-4">User Growth (Last 12 Months)</h2>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={userGrowth} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1F2937" />
                <XAxis
                  dataKey="month"
                  tick={{ fill: "#6B7280", fontSize: 11 }}
                  axisLine={{ stroke: "#1F2937" }}
                  tickLine={false}
                />
                <YAxis
                  tick={{ fill: "#6B7280", fontSize: 11 }}
                  axisLine={{ stroke: "#1F2937" }}
                  tickLine={false}
                  width={35}
                />
                <Tooltip content={<ChartTooltip />} />
                <Line
                  type="monotone"
                  dataKey="users"
                  stroke={ACCENT}
                  strokeWidth={2}
                  dot={{ fill: ACCENT, r: 3 }}
                  activeDot={{ r: 5, fill: ACCENT }}
                  name="Users"
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Users by Role Donut */}
        <div className="bg-[#111827] border border-[#1F2937] rounded-xl p-5">
          <h2 className="text-sm font-semibold text-gray-300 mb-4">Users by Role</h2>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={roleData}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={85}
                  paddingAngle={3}
                  dataKey="value"
                  label={renderPieLabel}
                >
                  {roleData.map((entry) => (
                    <Cell key={entry.name} fill={ROLE_COLORS[entry.name] || "#6B7280"} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    backgroundColor: "#1F2937",
                    border: "1px solid #374151",
                    borderRadius: 8,
                    fontSize: 12,
                    color: "#E5E7EB",
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Users by Plan + System Health */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Users by Plan Donut */}
        <div className="bg-[#111827] border border-[#1F2937] rounded-xl p-5">
          <h2 className="text-sm font-semibold text-gray-300 mb-4">Users by Plan</h2>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={planData}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={85}
                  paddingAngle={3}
                  dataKey="value"
                  label={renderPieLabel}
                >
                  {planData.map((entry) => (
                    <Cell key={entry.name} fill={PLAN_COLORS[entry.name] || "#6B7280"} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    backgroundColor: "#1F2937",
                    border: "1px solid #374151",
                    borderRadius: 8,
                    fontSize: 12,
                    color: "#E5E7EB",
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* System Health */}
        <div className="bg-[#111827] border border-[#1F2937] rounded-xl p-5">
          <h2 className="text-sm font-semibold text-gray-300 mb-4">System Health</h2>
          <div className="space-y-3">
            <HealthIndicator label="API Gateway" status="operational" />
            <HealthIndicator label="Database Cluster" status="operational" />
            <HealthIndicator label="Biometric Sync Service" status="operational" />
            <HealthIndicator label="Authentication Service" status="operational" />
            <HealthIndicator label="Notification Service" status="operational" />
            <HealthIndicator label="Backup Service" status="operational" />
          </div>
          <div className="mt-4 px-4 py-2 rounded-lg bg-[#0A0F1A] border border-[#1F2937]">
            <p className="text-xs text-gray-500">Last health check: <span className="text-gray-300">{new Date().toLocaleTimeString()}</span></p>
            <p className="text-xs text-gray-500 mt-0.5">Uptime: <span className="text-[#00E5A0] font-medium">99.97%</span> (last 30 days)</p>
          </div>
        </div>
      </div>
      </AnimatedSection>

      {/* Recent Activity Log */}
      <AnimatedSection delay={0.15}>
      <div className="bg-[#111827] border border-[#1F2937] rounded-xl p-5">
        <h2 className="text-sm font-semibold text-gray-300 mb-4">Recent Activity Log</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-[#1F2937] text-gray-400 text-xs uppercase tracking-wider">
                <th className="text-left px-4 py-2 font-medium">Timestamp</th>
                <th className="text-left px-4 py-2 font-medium">User</th>
                <th className="text-left px-4 py-2 font-medium">Role</th>
                <th className="text-left px-4 py-2 font-medium">Action</th>
              </tr>
            </thead>
            <tbody>
              {recentActivity.map((entry) => (
                <tr key={entry.id} className="border-b border-[#1F2937]/50 hover:bg-[#0A0F1A]/50 transition-colors">
                  <td className="px-4 py-2.5 text-gray-500 text-xs font-mono">{formatTimestamp(entry.timestamp)}</td>
                  <td className="px-4 py-2.5 text-white">{entry.userName}</td>
                  <td className="px-4 py-2.5">
                    <span className="inline-block px-2 py-0.5 rounded-md bg-[#0A0F1A] text-[#00E5A0] text-xs font-medium border border-[#00E5A0]/30">
                      {entry.role}
                    </span>
                  </td>
                  <td className="px-4 py-2.5 text-gray-300">{entry.action}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      </AnimatedSection>
    </div>
  );
}
