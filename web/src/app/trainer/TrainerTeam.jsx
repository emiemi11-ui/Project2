import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { persons } from "../../data/mockData";
import { useAuth } from "../../hooks/useAuth";
import { ResponsiveContainer, RadialBarChart, RadialBar } from "recharts";

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

export default function TrainerTeam() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [filter, setFilter] = useState("all");
  const [sortBy, setSortBy] = useState("readiness");

  const athletes = useMemo(() => {
    const list = persons.filter((p) =>
      ["soldier", "patient"].includes(p.role) || p.archetype
    ).map((p) => ({
      ...p,
      status: getStatus(p.scores.readiness),
      acwr: (0.7 + Math.random() * 0.9).toFixed(2),
    }));

    let filtered = filter === "all" ? list : list.filter((a) => a.status === filter);
    filtered.sort((a, b) => {
      if (sortBy === "readiness") return b.scores.readiness - a.scores.readiness;
      if (sortBy === "name") return a.name.localeCompare(b.name);
      return parseFloat(b.acwr) - parseFloat(a.acwr);
    });
    return filtered;
  }, [filter, sortBy]);

  const stats = useMemo(() => {
    const all = persons.filter((p) => p.scores);
    const avg = Math.round(all.reduce((s, p) => s + p.scores.readiness, 0) / all.length);
    const ready = all.filter((p) => p.scores.readiness >= 70).length;
    const atRisk = all.filter((p) => p.scores.readiness < 45).length;
    return { total: all.length, avg, ready, atRisk };
  }, []);

  return (
    <div className="min-h-screen bg-[#0a0f14] text-white flex">
      {/* Sidebar */}
      <aside className="w-64 bg-[#0d1117] border-r border-gray-800/50 flex flex-col p-4 hidden lg:flex">
        <div className="flex items-center gap-2 mb-8">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-orange-400 to-cyan-500 flex items-center justify-center font-bold text-black text-sm">V</div>
          <span className="font-bold text-lg tracking-tight">Vita<span className="text-orange-400">Nova</span></span>
        </div>
        <nav className="flex-1 space-y-1">
          {[
            { label: "Team", path: "/app/team", active: true },
            { label: "Programs", path: "/app/programs" },
            { label: "ACWR Monitor", path: "/app/acwr" },
            { label: "Alerts", path: "/app/alerts" },
          ].map((item) => (
            <button
              key={item.label}
              onClick={() => navigate(item.path)}
              className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors ${
                item.active ? "bg-orange-500/15 text-orange-400" : "text-gray-400 hover:text-white hover:bg-gray-800/50"
              }`}
            >
              {item.label}
            </button>
          ))}
        </nav>
        <button onClick={logout} className="text-sm text-gray-500 hover:text-red-400 transition-colors px-3 py-2">
          Logout
        </button>
      </aside>

      {/* Main */}
      <main className="flex-1 p-6 overflow-auto">
        {/* Top bar */}
        <div className="flex flex-wrap items-center justify-between gap-4 mb-8">
          <h1 className="text-2xl font-bold">Team Overview</h1>
          <div className="flex items-center gap-6 text-sm">
            <div className="text-center">
              <div className="text-2xl font-bold text-orange-400">{stats.total}</div>
              <div className="text-gray-500 text-xs uppercase tracking-wider">Athletes</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-cyan-400">{stats.avg}%</div>
              <div className="text-gray-500 text-xs uppercase tracking-wider">Avg Readiness</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-emerald-400">{stats.ready}</div>
              <div className="text-gray-500 text-xs uppercase tracking-wider">Ready</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-red-400">{stats.atRisk}</div>
              <div className="text-gray-500 text-xs uppercase tracking-wider">At Risk</div>
            </div>
          </div>
        </div>

        {/* Filters */}
        <div className="flex flex-wrap items-center gap-3 mb-6">
          {["all", "ready", "monitor", "risk"].map((f) => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-4 py-1.5 rounded-lg text-sm font-medium transition-colors ${
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
            className="ml-auto px-3 py-1.5 rounded-lg bg-gray-800/50 border border-gray-700/50 text-sm text-gray-300"
          >
            <option value="readiness">Sort: Readiness</option>
            <option value="name">Sort: Name</option>
            <option value="acwr">Sort: ACWR</option>
          </select>
        </div>

        {/* Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {athletes.map((athlete) => {
            const st = statusColor[athlete.status];
            return (
              <button
                key={athlete.id}
                onClick={() => navigate(`/app/athlete/${athlete.id}`)}
                className="text-left rounded-xl bg-[#0d1117] border border-gray-800/50 p-5 hover:border-orange-500/30 transition-all group"
              >
                <div className="flex items-start justify-between mb-3">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-gradient-to-br from-orange-500/20 to-cyan-500/20 border border-gray-700 flex items-center justify-center text-sm font-bold text-orange-400">
                      {athlete.name.split(" ").map((n) => n[0]).join("")}
                    </div>
                    <div>
                      <div className="font-semibold text-white group-hover:text-orange-400 transition-colors">{athlete.name}</div>
                      <div className="text-xs text-gray-500">{athlete.unit}</div>
                    </div>
                  </div>
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${st.bg} ${st.text}`}>
                    {st.label}
                  </span>
                </div>
                <div className="grid grid-cols-4 gap-2 text-center">
                  <div>
                    <div className="text-lg font-bold text-white">{athlete.scores.readiness}</div>
                    <div className="text-[10px] text-gray-500 uppercase">Ready</div>
                  </div>
                  <div>
                    <div className="text-lg font-bold text-cyan-400">{athlete.scores.hrv}</div>
                    <div className="text-[10px] text-gray-500 uppercase">HRV</div>
                  </div>
                  <div>
                    <div className="text-lg font-bold text-blue-400">{athlete.scores.sleep}</div>
                    <div className="text-[10px] text-gray-500 uppercase">Sleep</div>
                  </div>
                  <div>
                    <div className="text-lg font-bold text-amber-400">{athlete.acwr}</div>
                    <div className="text-[10px] text-gray-500 uppercase">ACWR</div>
                  </div>
                </div>
              </button>
            );
          })}
        </div>
      </main>
    </div>
  );
}
