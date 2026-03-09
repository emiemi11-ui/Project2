import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { persons, acwrData } from "../../data/mockData";
import {
  ResponsiveContainer, RadialBarChart, RadialBar, BarChart, Bar, XAxis, YAxis,
  CartesianGrid, Tooltip, LineChart, Line,
} from "recharts";
import { AnimatedSection, StaggerContainer, StaggerItem, AnimatedNumber } from "../../components/AnimatedSection";
import { motion } from "framer-motion";

const statusColor = {
  ready: { bg: "bg-emerald-500/20", text: "text-emerald-400", dot: "bg-emerald-400", label: "Ready" },
  monitor: { bg: "bg-amber-500/20", text: "text-amber-400", dot: "bg-amber-400", label: "Monitor" },
  risk: { bg: "bg-red-500/20", text: "text-red-400", dot: "bg-red-400", label: "At Risk" },
};

function getStatus(readiness) {
  if (readiness >= 70) return "ready";
  if (readiness >= 45) return "monitor";
  return "risk";
}

function MiniGauge({ value, color }) {
  const data = [{ value, fill: color }];
  return (
    <div className="w-14 h-14">
      <ResponsiveContainer width="100%" height="100%">
        <RadialBarChart cx="50%" cy="50%" innerRadius="60%" outerRadius="100%" startAngle={90} endAngle={-270} data={data}>
          <RadialBar background dataKey="value" cornerRadius={10} max={100} />
        </RadialBarChart>
      </ResponsiveContainer>
    </div>
  );
}

function Sparkline({ data, color, height = 30 }) {
  return (
    <div style={{ height }}>
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data}>
          <Line type="monotone" dataKey="value" stroke={color} strokeWidth={1.5} dot={false} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

export default function TrainerTeam() {
  const navigate = useNavigate();
  const [filter, setFilter] = useState("all");
  const [sortBy, setSortBy] = useState("readiness");

  const athletes = useMemo(() => {
    const list = persons
      .filter((p) => ["soldier", "patient"].includes(p.role))
      .map((p) => {
        const acwr = acwrData.find((a) => a.personId === p.id);
        return {
          ...p,
          status: getStatus(p.metrics.readinessScore),
          acwr: acwr ? acwr.ratio : parseFloat((0.7 + (p.metrics.readinessScore / 100) * 0.9).toFixed(2)),
          acwrZone: acwr ? acwr.zone : "optimal",
        };
      });

    let filtered = filter === "all" ? list : list.filter((a) => a.status === filter);
    filtered.sort((a, b) => {
      if (sortBy === "readiness") return b.metrics.readinessScore - a.metrics.readinessScore;
      if (sortBy === "name") return a.name.localeCompare(b.name);
      return b.acwr - a.acwr;
    });
    return filtered;
  }, [filter, sortBy]);

  const stats = useMemo(() => {
    const all = persons.filter((p) => ["soldier", "patient"].includes(p.role));
    const avg = all.length > 0 ? Math.round(all.reduce((s, p) => s + p.metrics.readinessScore, 0) / all.length) : 0;
    const ready = all.filter((p) => p.metrics.readinessScore >= 70).length;
    const atRisk = all.filter((p) => p.metrics.readinessScore < 45).length;
    const avgAcwr = acwrData.length > 0 ? (acwrData.reduce((s, a) => s + a.ratio, 0) / acwrData.length).toFixed(2) : "N/A";
    return { total: all.length, avg, ready, atRisk, avgAcwr };
  }, []);

  const barChartData = useMemo(() => {
    return persons
      .filter((p) => ["soldier", "patient"].includes(p.role))
      .slice(0, 12)
      .map((p) => ({
        name: p.name.split(" ")[1] || p.name.split(" ")[0],
        readiness: p.metrics.readinessScore,
        fill: p.metrics.readinessScore >= 70 ? "#22c55e" : p.metrics.readinessScore >= 45 ? "#f59e0b" : "#ef4444",
      }));
  }, []);

  return (
    <main className="p-4 sm:p-6 overflow-auto">
      {/* KPI Row */}
      <AnimatedSection>
        <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
          <h1 className="text-xl sm:text-2xl font-bold">Team Overview</h1>
          <div className="flex items-center gap-4 sm:gap-6 text-sm">
            <div className="text-center">
              <div className="text-xl sm:text-2xl font-bold text-orange-400"><AnimatedNumber value={stats.total} /></div>
              <div className="text-gray-500 text-[10px] sm:text-xs uppercase tracking-wider">Athletes</div>
            </div>
            <div className="text-center">
              <div className="text-xl sm:text-2xl font-bold text-cyan-400"><AnimatedNumber value={stats.avg} />%</div>
              <div className="text-gray-500 text-[10px] sm:text-xs uppercase tracking-wider">Avg Ready</div>
            </div>
            <div className="text-center">
              <div className="text-xl sm:text-2xl font-bold text-emerald-400"><AnimatedNumber value={stats.ready} /></div>
              <div className="text-gray-500 text-[10px] sm:text-xs uppercase tracking-wider">Ready</div>
            </div>
            <div className="text-center">
              <div className="text-xl sm:text-2xl font-bold text-red-400"><AnimatedNumber value={stats.atRisk} /></div>
              <div className="text-gray-500 text-[10px] sm:text-xs uppercase tracking-wider">At Risk</div>
            </div>
          </div>
        </div>
      </AnimatedSection>

      {/* Readiness Chart */}
      <AnimatedSection delay={0.1}>
        <div className="rounded-xl bg-[#0d1117] border border-gray-800/50 p-4 sm:p-6 mb-6">
          <h2 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">Readiness by Athlete</h2>
          <div className="h-48 sm:h-56">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={barChartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
                <XAxis dataKey="name" tick={{ fill: "#6b7280", fontSize: 10 }} angle={-30} textAnchor="end" height={50} />
                <YAxis domain={[0, 100]} tick={{ fill: "#6b7280", fontSize: 10 }} />
                <Tooltip contentStyle={{ background: "#0d1117", border: "1px solid #1f2937", borderRadius: 8 }} />
                <Bar dataKey="readiness" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </AnimatedSection>

      {/* Filters */}
      <AnimatedSection delay={0.15}>
        <div className="flex flex-wrap items-center gap-2 sm:gap-3 mb-6">
          {["all", "ready", "monitor", "risk"].map((f) => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-3 sm:px-4 py-1.5 rounded-lg text-xs sm:text-sm font-medium transition-all ${
                filter === f
                  ? "bg-orange-500/20 text-orange-400 border border-orange-500/30"
                  : "bg-gray-800/50 text-gray-400 border border-gray-700/50 hover:text-white"
              }`}
            >
              {f === "all" ? "All" : statusColor[f]?.label}
            </button>
          ))}
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="ml-auto px-3 py-1.5 rounded-lg bg-gray-800/50 border border-gray-700/50 text-xs sm:text-sm text-gray-300"
          >
            <option value="readiness">Sort: Readiness</option>
            <option value="name">Sort: Name</option>
            <option value="acwr">Sort: ACWR</option>
          </select>
        </div>
      </AnimatedSection>

      {/* Empty state */}
      {athletes.length === 0 && (
        <AnimatedSection>
          <div className="text-center py-16 text-gray-500">
            <div className="text-4xl mb-4">🔍</div>
            <p className="text-lg font-medium">No athletes match this filter</p>
            <p className="text-sm mt-1">Try changing the filter or sort criteria</p>
          </div>
        </AnimatedSection>
      )}

      {/* Grid */}
      <StaggerContainer className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
        {athletes.map((athlete) => {
          const st = statusColor[athlete.status];
          return (
            <StaggerItem key={athlete.id}>
              <motion.button
                onClick={() => navigate(`/app/athlete/${athlete.id}`)}
                whileHover={{ scale: 1.02 }}
                className="w-full text-left rounded-xl bg-[#0d1117] border border-gray-800/50 p-4 sm:p-5 hover:border-orange-500/40 hover:shadow-lg hover:shadow-orange-500/5 transition-all duration-200 group"
              >
                <div className="flex items-start justify-between mb-3">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-gradient-to-br from-orange-500/20 to-cyan-500/20 border border-gray-700 flex items-center justify-center text-sm font-bold text-orange-400">
                      {athlete.name.split(" ").map((n) => n[0]).join("")}
                    </div>
                    <div>
                      <div className="font-semibold text-white group-hover:text-orange-400 transition-colors text-sm sm:text-base">{athlete.name}</div>
                      <div className="text-[10px] sm:text-xs text-gray-500">{athlete.unit}</div>
                    </div>
                  </div>
                  <span className={`px-2 py-0.5 rounded-full text-[10px] sm:text-xs font-medium ${st.bg} ${st.text}`}>
                    <span className={`inline-block w-1.5 h-1.5 rounded-full ${st.dot} mr-1`} />
                    {st.label}
                  </span>
                </div>

                {/* Sparkline */}
                <div className="mb-3">
                  <Sparkline
                    data={athlete.history.readiness}
                    color={athlete.metrics.readinessScore >= 70 ? "#22c55e" : athlete.metrics.readinessScore >= 45 ? "#f59e0b" : "#ef4444"}
                  />
                </div>

                <div className="grid grid-cols-4 gap-2 text-center">
                  <div>
                    <div className="text-base sm:text-lg font-bold text-white">{athlete.metrics.readinessScore}</div>
                    <div className="text-[9px] sm:text-[10px] text-gray-500 uppercase">Ready</div>
                  </div>
                  <div>
                    <div className="text-base sm:text-lg font-bold text-cyan-400">{athlete.metrics.hrvScore}</div>
                    <div className="text-[9px] sm:text-[10px] text-gray-500 uppercase">HRV</div>
                  </div>
                  <div>
                    <div className="text-base sm:text-lg font-bold text-blue-400">{athlete.metrics.sleepScore}</div>
                    <div className="text-[9px] sm:text-[10px] text-gray-500 uppercase">Sleep</div>
                  </div>
                  <div>
                    <div className="text-base sm:text-lg font-bold text-amber-400">{athlete.acwr}</div>
                    <div className="text-[9px] sm:text-[10px] text-gray-500 uppercase">ACWR</div>
                  </div>
                </div>
              </motion.button>
            </StaggerItem>
          );
        })}
      </StaggerContainer>
    </main>
  );
}
