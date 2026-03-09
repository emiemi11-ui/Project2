import { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { persons } from "../../data/mockData";
import { useAuth } from "../../hooks/useAuth";
import {
  ResponsiveContainer, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ReferenceLine, ReferenceArea,
} from "recharts";

function generateAcwrHistory(seed) {
  const history = [];
  let acwr = 0.8 + (seed % 10) * 0.05;
  for (let i = 29; i >= 0; i--) {
    const d = new Date();
    d.setDate(d.getDate() - i);
    acwr += (Math.random() - 0.48) * 0.08;
    acwr = Math.max(0.4, Math.min(2.0, acwr));
    history.push({ date: d.toISOString().split("T")[0].slice(5), acwr: parseFloat(acwr.toFixed(2)) });
  }
  return history;
}

function getAcwrZone(acwr) {
  if (acwr < 0.8) return { label: "Under-trained", color: "#3b82f6" };
  if (acwr <= 1.3) return { label: "Optimal", color: "#22c55e" };
  if (acwr <= 1.5) return { label: "Warning", color: "#f59e0b" };
  return { label: "Danger", color: "#ef4444" };
}

export default function TrainerAcwr() {
  const navigate = useNavigate();
  const { logout } = useAuth();

  const athleteData = useMemo(() => {
    return persons
      .filter((p) => p.scores)
      .slice(0, 16)
      .map((p, i) => {
        const history = generateAcwrHistory(i * 7 + p.age);
        const current = history[history.length - 1].acwr;
        const prev = history[history.length - 2].acwr;
        return {
          ...p,
          history,
          currentAcwr: current,
          trend: current > prev ? "up" : current < prev ? "down" : "stable",
          zone: getAcwrZone(current),
        };
      })
      .sort((a, b) => b.currentAcwr - a.currentAcwr);
  }, []);

  const dangerAthletes = athleteData.filter((a) => a.currentAcwr > 1.5);
  const warningAthletes = athleteData.filter((a) => a.currentAcwr > 1.3 && a.currentAcwr <= 1.5);
  const selectedAthlete = athleteData[0];

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
            { label: "Team", path: "/app/team" },
            { label: "Programs", path: "/app/programs" },
            { label: "ACWR Monitor", path: "/app/acwr", active: true },
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
        <button onClick={logout} className="text-sm text-gray-500 hover:text-red-400 transition-colors px-3 py-2">Logout</button>
      </aside>

      <main className="flex-1 p-6 overflow-auto">
        <h1 className="text-2xl font-bold mb-6">ACWR Monitor</h1>

        {/* Alert Panel */}
        {(dangerAthletes.length > 0 || warningAthletes.length > 0) && (
          <div className="mb-6 space-y-3">
            {dangerAthletes.length > 0 && (
              <div className="rounded-xl bg-red-500/10 border border-red-500/30 p-4">
                <div className="flex items-center gap-2 mb-2">
                  <div className="w-2 h-2 rounded-full bg-red-400 animate-pulse" />
                  <span className="text-sm font-semibold text-red-400 uppercase tracking-wider">Danger Zone ({dangerAthletes.length})</span>
                </div>
                <div className="flex flex-wrap gap-2">
                  {dangerAthletes.map((a) => (
                    <span key={a.id} className="px-3 py-1 rounded-lg bg-red-500/15 text-xs text-red-300">
                      {a.name}: {a.currentAcwr}
                    </span>
                  ))}
                </div>
              </div>
            )}
            {warningAthletes.length > 0 && (
              <div className="rounded-xl bg-amber-500/10 border border-amber-500/30 p-4">
                <div className="flex items-center gap-2 mb-2">
                  <div className="w-2 h-2 rounded-full bg-amber-400" />
                  <span className="text-sm font-semibold text-amber-400 uppercase tracking-wider">Warning Zone ({warningAthletes.length})</span>
                </div>
                <div className="flex flex-wrap gap-2">
                  {warningAthletes.map((a) => (
                    <span key={a.id} className="px-3 py-1 rounded-lg bg-amber-500/15 text-xs text-amber-300">
                      {a.name}: {a.currentAcwr}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {/* ACWR Chart */}
        <div className="rounded-xl bg-[#0d1117] border border-gray-800/50 p-6 mb-6">
          <h2 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">
            ACWR Trend — {selectedAthlete.name}
          </h2>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={selectedAthlete.history}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
                <XAxis dataKey="date" tick={{ fill: "#6b7280", fontSize: 10 }} interval={4} />
                <YAxis domain={[0.4, 2.0]} tick={{ fill: "#6b7280", fontSize: 10 }} />
                <Tooltip contentStyle={{ background: "#0d1117", border: "1px solid #1f2937", borderRadius: 8 }} />
                <ReferenceArea y1={0.4} y2={0.8} fill="#3b82f6" fillOpacity={0.08} />
                <ReferenceArea y1={0.8} y2={1.3} fill="#22c55e" fillOpacity={0.08} />
                <ReferenceArea y1={1.3} y2={1.5} fill="#f59e0b" fillOpacity={0.08} />
                <ReferenceArea y1={1.5} y2={2.0} fill="#ef4444" fillOpacity={0.08} />
                <ReferenceLine y={0.8} stroke="#3b82f6" strokeDasharray="3 3" />
                <ReferenceLine y={1.3} stroke="#22c55e" strokeDasharray="3 3" />
                <ReferenceLine y={1.5} stroke="#f59e0b" strokeDasharray="3 3" />
                <Line type="monotone" dataKey="acwr" stroke="#f97316" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>
          <div className="flex items-center gap-6 mt-4 text-xs text-gray-500">
            <div className="flex items-center gap-1.5"><div className="w-3 h-2 bg-blue-500/30 rounded" /> Under ({"<"}0.8)</div>
            <div className="flex items-center gap-1.5"><div className="w-3 h-2 bg-emerald-500/30 rounded" /> Optimal (0.8–1.3)</div>
            <div className="flex items-center gap-1.5"><div className="w-3 h-2 bg-amber-500/30 rounded" /> Warning (1.3–1.5)</div>
            <div className="flex items-center gap-1.5"><div className="w-3 h-2 bg-red-500/30 rounded" /> Danger ({">"}1.5)</div>
          </div>
        </div>

        {/* Athletes Table */}
        <div className="rounded-xl bg-[#0d1117] border border-gray-800/50 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-gray-800/50">
                  <th className="text-left px-5 py-3 text-xs text-gray-500 uppercase tracking-wider">Athlete</th>
                  <th className="text-center px-4 py-3 text-xs text-gray-500 uppercase tracking-wider">ACWR</th>
                  <th className="text-center px-4 py-3 text-xs text-gray-500 uppercase tracking-wider">Zone</th>
                  <th className="text-center px-4 py-3 text-xs text-gray-500 uppercase tracking-wider">Trend</th>
                  <th className="text-left px-4 py-3 text-xs text-gray-500 uppercase tracking-wider">Recommendation</th>
                </tr>
              </thead>
              <tbody>
                {athleteData.map((athlete) => (
                  <tr key={athlete.id} className="border-b border-gray-800/30 hover:bg-gray-800/20">
                    <td className="px-5 py-3">
                      <button onClick={() => navigate(`/app/athlete/${athlete.id}`)} className="text-sm font-medium text-white hover:text-orange-400 transition-colors">
                        {athlete.name}
                      </button>
                      <div className="text-xs text-gray-500">{athlete.unit}</div>
                    </td>
                    <td className="text-center px-4 py-3">
                      <span className="text-sm font-bold" style={{ color: athlete.zone.color }}>{athlete.currentAcwr}</span>
                    </td>
                    <td className="text-center px-4 py-3">
                      <span className="text-xs font-medium" style={{ color: athlete.zone.color }}>{athlete.zone.label}</span>
                    </td>
                    <td className="text-center px-4 py-3">
                      <span className={`text-sm ${athlete.trend === "up" ? "text-red-400" : athlete.trend === "down" ? "text-emerald-400" : "text-gray-400"}`}>
                        {athlete.trend === "up" ? "↑" : athlete.trend === "down" ? "↓" : "→"}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-xs text-gray-400">
                      {athlete.currentAcwr > 1.5 ? "Reduce load immediately" :
                       athlete.currentAcwr > 1.3 ? "Monitor closely, consider deload" :
                       athlete.currentAcwr < 0.8 ? "Increase training volume gradually" :
                       "Maintain current program"}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
}
