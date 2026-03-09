import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { persons, acwrData } from "../../data/mockData";
import {
  ResponsiveContainer, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ReferenceLine, ReferenceArea,
} from "recharts";
import { AnimatedSection, StaggerContainer, StaggerItem, AnimatedNumber } from "../../components/AnimatedSection";
import { motion } from "framer-motion";

function generateAcwrHistory(seed) {
  const history = [];
  let acwr = 0.8 + (seed % 10) * 0.05;
  for (let i = 29; i >= 0; i--) {
    const d = new Date();
    d.setDate(d.getDate() - i);
    acwr += (Math.sin(seed + i * 0.3) * 0.04) + ((seed * 13 + i * 7) % 100 / 100 - 0.48) * 0.06;
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
  const [selectedId, setSelectedId] = useState(null);

  const athleteData = useMemo(() => {
    return persons
      .filter((p) => ["soldier", "patient"].includes(p.role))
      .slice(0, 16)
      .map((p, i) => {
        const acwr = acwrData.find((a) => a.personId === p.id);
        const history = generateAcwrHistory(i * 7 + p.age);
        const current = acwr ? acwr.ratio : history[history.length - 1].acwr;
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
  const selectedAthlete = selectedId ? athleteData.find((a) => a.id === selectedId) || athleteData[0] : athleteData[0];

  const stats = useMemo(() => {
    const avg = athleteData.length > 0 ? (athleteData.reduce((s, a) => s + a.currentAcwr, 0) / athleteData.length).toFixed(2) : "0";
    return {
      total: athleteData.length,
      danger: dangerAthletes.length,
      warning: warningAthletes.length,
      optimal: athleteData.filter((a) => a.currentAcwr >= 0.8 && a.currentAcwr <= 1.3).length,
      avg,
    };
  }, [athleteData, dangerAthletes, warningAthletes]);

  return (
    <main className="p-4 sm:p-6 overflow-auto">
      {/* Header + KPIs */}
      <AnimatedSection>
        <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
          <h1 className="text-xl sm:text-2xl font-bold">ACWR Monitor</h1>
          <div className="flex items-center gap-4 sm:gap-6">
            <div className="text-center">
              <div className="text-xl font-bold text-orange-400">{stats.avg}</div>
              <div className="text-[10px] text-gray-500 uppercase tracking-wider">Avg ACWR</div>
            </div>
            <div className="text-center">
              <div className="text-xl font-bold text-emerald-400"><AnimatedNumber value={stats.optimal} /></div>
              <div className="text-[10px] text-gray-500 uppercase tracking-wider">Optimal</div>
            </div>
            <div className="text-center">
              <div className="text-xl font-bold text-amber-400"><AnimatedNumber value={stats.warning} /></div>
              <div className="text-[10px] text-gray-500 uppercase tracking-wider">Warning</div>
            </div>
            <div className="text-center">
              <div className="text-xl font-bold text-red-400"><AnimatedNumber value={stats.danger} /></div>
              <div className="text-[10px] text-gray-500 uppercase tracking-wider">Danger</div>
            </div>
          </div>
        </div>
      </AnimatedSection>

      {/* Alert Panels */}
      {(dangerAthletes.length > 0 || warningAthletes.length > 0) && (
        <AnimatedSection delay={0.1}>
          <div className="mb-6 space-y-3">
            {dangerAthletes.length > 0 && (
              <motion.div
                initial={{ x: -20, opacity: 0 }} animate={{ x: 0, opacity: 1 }}
                className="rounded-xl bg-red-500/10 border border-red-500/30 p-4"
              >
                <div className="flex items-center gap-2 mb-2">
                  <div className="w-2 h-2 rounded-full bg-red-400 animate-pulse" />
                  <span className="text-sm font-semibold text-red-400 uppercase tracking-wider">Danger Zone ({dangerAthletes.length})</span>
                </div>
                <div className="flex flex-wrap gap-2">
                  {dangerAthletes.map((a) => (
                    <button
                      key={a.id}
                      onClick={() => navigate(`/app/athlete/${a.id}`)}
                      className="px-3 py-1.5 rounded-lg bg-red-500/15 text-xs text-red-300 hover:bg-red-500/25 transition-colors"
                    >
                      {a.name}: {a.currentAcwr}
                    </button>
                  ))}
                </div>
              </motion.div>
            )}
            {warningAthletes.length > 0 && (
              <motion.div
                initial={{ x: -20, opacity: 0 }} animate={{ x: 0, opacity: 1 }} transition={{ delay: 0.1 }}
                className="rounded-xl bg-amber-500/10 border border-amber-500/30 p-4"
              >
                <div className="flex items-center gap-2 mb-2">
                  <div className="w-2 h-2 rounded-full bg-amber-400" />
                  <span className="text-sm font-semibold text-amber-400 uppercase tracking-wider">Warning Zone ({warningAthletes.length})</span>
                </div>
                <div className="flex flex-wrap gap-2">
                  {warningAthletes.map((a) => (
                    <button
                      key={a.id}
                      onClick={() => navigate(`/app/athlete/${a.id}`)}
                      className="px-3 py-1.5 rounded-lg bg-amber-500/15 text-xs text-amber-300 hover:bg-amber-500/25 transition-colors"
                    >
                      {a.name}: {a.currentAcwr}
                    </button>
                  ))}
                </div>
              </motion.div>
            )}
          </div>
        </AnimatedSection>
      )}

      {/* Athlete selector + ACWR Chart */}
      <AnimatedSection delay={0.15}>
        <div className="rounded-xl bg-[#0d1117] border border-gray-800/50 p-4 sm:p-6 mb-6">
          <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
            <h2 className="text-sm font-semibold text-gray-400 uppercase tracking-wider">
              ACWR Trend — {selectedAthlete.name}
            </h2>
            <select
              value={selectedAthlete.id}
              onChange={(e) => setSelectedId(e.target.value)}
              className="px-3 py-1.5 rounded-lg bg-gray-800/50 border border-gray-700/50 text-xs sm:text-sm text-gray-300"
            >
              {athleteData.map((a) => (
                <option key={a.id} value={a.id}>{a.name} ({a.currentAcwr})</option>
              ))}
            </select>
          </div>
          <div className="h-56 sm:h-72">
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
          <div className="flex flex-wrap items-center gap-4 sm:gap-6 mt-4 text-[10px] sm:text-xs text-gray-500">
            <div className="flex items-center gap-1.5"><div className="w-3 h-2 bg-blue-500/30 rounded" /> Under ({"<"}0.8)</div>
            <div className="flex items-center gap-1.5"><div className="w-3 h-2 bg-emerald-500/30 rounded" /> Optimal (0.8–1.3)</div>
            <div className="flex items-center gap-1.5"><div className="w-3 h-2 bg-amber-500/30 rounded" /> Warning (1.3–1.5)</div>
            <div className="flex items-center gap-1.5"><div className="w-3 h-2 bg-red-500/30 rounded" /> Danger ({">"}1.5)</div>
          </div>
        </div>
      </AnimatedSection>

      {/* Athletes Table */}
      <AnimatedSection delay={0.2}>
        <div className="rounded-xl bg-[#0d1117] border border-gray-800/50 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full min-w-[600px]">
              <thead>
                <tr className="border-b border-gray-800/50">
                  <th className="text-left px-4 sm:px-5 py-3 text-xs text-gray-500 uppercase tracking-wider">Athlete</th>
                  <th className="text-center px-4 py-3 text-xs text-gray-500 uppercase tracking-wider">ACWR</th>
                  <th className="text-center px-4 py-3 text-xs text-gray-500 uppercase tracking-wider">Zone</th>
                  <th className="text-center px-4 py-3 text-xs text-gray-500 uppercase tracking-wider">Trend</th>
                  <th className="text-left px-4 py-3 text-xs text-gray-500 uppercase tracking-wider">Recommendation</th>
                </tr>
              </thead>
              <tbody>
                {athleteData.map((athlete, i) => (
                  <motion.tr
                    key={athlete.id}
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: 0.3 + i * 0.03 }}
                    className="border-b border-gray-800/30 hover:bg-gray-800/20 transition-colors"
                  >
                    <td className="px-4 sm:px-5 py-3">
                      <button onClick={() => navigate(`/app/athlete/${athlete.id}`)} className="text-sm font-medium text-white hover:text-orange-400 transition-colors">
                        {athlete.name}
                      </button>
                      <div className="text-[10px] sm:text-xs text-gray-500">{athlete.unit}</div>
                    </td>
                    <td className="text-center px-4 py-3">
                      <span className="text-sm font-bold" style={{ color: athlete.zone.color }}>{athlete.currentAcwr}</span>
                    </td>
                    <td className="text-center px-4 py-3">
                      <span className="text-xs font-medium px-2 py-0.5 rounded-full" style={{ color: athlete.zone.color, backgroundColor: athlete.zone.color + "15" }}>
                        {athlete.zone.label}
                      </span>
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
                  </motion.tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </AnimatedSection>
    </main>
  );
}
