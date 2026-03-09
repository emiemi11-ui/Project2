import { useMemo, useState } from "react";
import { persons } from "../../data/mockData";
import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip } from "recharts";
import { AnimatedSection, StaggerContainer, StaggerItem, AnimatedNumber } from "../../components/AnimatedSection";
import { motion, AnimatePresence } from "framer-motion";

function generateStressGrid() {
  const grid = [];
  const today = new Date();
  for (let i = 27; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(d.getDate() - i);
    const dayOfWeek = d.getDay();
    const isWeekend = dayOfWeek === 0 || dayOfWeek === 6;
    const base = isWeekend ? 25 : 45;
    const seed = d.getDate() * 31 + d.getMonth() * 7 + i;
    const rand = ((seed * 16807) % 2147483647) / 2147483647;
    const stress = Math.min(100, Math.max(5, base + Math.round((rand - 0.4) * 40)));
    grid.push({
      date: d.toISOString().split("T")[0],
      dayLabel: d.getDate(),
      weekday: d.toLocaleDateString("en-US", { weekday: "short" }),
      stress,
      sleep: Math.round(40 + rand * 50),
      stressors: stress > 60 ? ["Deadline work", "Sleep deprivation", "Physical fatigue"] :
                 stress > 40 ? ["Moderate workload", "Social tension"] :
                 ["Relaxed", "Good recovery"],
    });
  }
  return grid;
}

function stressColor(level) {
  if (level < 25) return "bg-emerald-400/60";
  if (level < 40) return "bg-lime-400/50";
  if (level < 55) return "bg-amber-400/50";
  if (level < 70) return "bg-orange-400/60";
  return "bg-red-400/70";
}

export default function PsychStress() {
  const grid = useMemo(() => generateStressGrid(), []);
  const [selectedDay, setSelectedDay] = useState(null);

  const patients = useMemo(() => {
    return persons
      .filter((p) => p.metrics.stressScore > 50)
      .sort((a, b) => b.metrics.stressScore - a.metrics.stressScore)
      .slice(0, 8);
  }, []);

  const trendData = useMemo(() => {
    return grid.map((d) => ({
      date: d.date.slice(5),
      stress: d.stress,
      avg: Math.round(grid.reduce((s, g) => s + g.stress, 0) / grid.length),
    }));
  }, [grid]);

  const avgStress = useMemo(() => Math.round(grid.reduce((s, d) => s + d.stress, 0) / grid.length), [grid]);
  const highDays = grid.filter((d) => d.stress > 60).length;
  const sleepStressCorrelation = useMemo(() => {
    const highStressDays = grid.filter((d) => d.stress > 60);
    const lowSleepCount = highStressDays.filter((d) => d.sleep < 50).length;
    return highStressDays.length > 0 ? Math.round((lowSleepCount / highStressDays.length) * 100) : 0;
  }, [grid]);

  const patterns = [
    "Nivelul de stres creste constant Luni-Miercuri si scade spre weekend.",
    "Pacientii din Charlie Company au stres mediu cu 23% mai mare decat media.",
    `Zilele cu stres >60 coincid cu somn <50 in ${sleepStressCorrelation}% din cazuri.`,
  ];

  return (
    <main className="p-4 sm:p-6 overflow-auto">
      <AnimatedSection>
        <h1 className="text-xl sm:text-2xl font-bold mb-1 text-[#d4c5a9]">Stress Heatmap</h1>
        <p className="text-xs sm:text-sm text-[#8a7a6a] mb-6">Pattern detection si analiza factorilor de stres</p>
      </AnimatedSection>

      {/* KPIs */}
      <AnimatedSection delay={0.05}>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-6">
          {[
            { label: "Stres Mediu", value: avgStress, suffix: "%", color: avgStress > 50 ? "text-amber-400" : "text-emerald-400" },
            { label: "Zile Stres Ridicat", value: highDays, suffix: "", color: "text-red-400" },
            { label: "Pacienti >50%", value: patients.length, suffix: "", color: "text-violet-400" },
            { label: "Corelare Somn", value: sleepStressCorrelation, suffix: "%", color: "text-cyan-400" },
          ].map((kpi) => (
            <motion.div
              key={kpi.label}
              whileHover={{ scale: 1.02 }}
              className="bg-[#16140f] rounded-xl border border-[#2a2520] p-3 sm:p-4 text-center hover:border-[#a78bfa]/30 transition-all"
            >
              <div className={`text-xl sm:text-2xl font-bold ${kpi.color}`}><AnimatedNumber value={kpi.value} />{kpi.suffix}</div>
              <div className="text-[9px] sm:text-[10px] text-[#5a5045] uppercase tracking-wider mt-1">{kpi.label}</div>
            </motion.div>
          ))}
        </div>
      </AnimatedSection>

      {/* Heatmap */}
      <AnimatedSection delay={0.1}>
        <div className="bg-[#16140f] rounded-xl border border-[#2a2520] p-4 sm:p-6 mb-6">
          <h2 className="text-sm font-semibold text-[#8a7a6a] uppercase tracking-wider mb-4">Ultimele 28 de zile</h2>
          <div className="grid grid-cols-7 gap-1.5 sm:gap-2">
            {["L", "Ma", "Mi", "J", "V", "S", "D"].map((d) => (
              <div key={d} className="text-center text-[10px] sm:text-xs text-[#5a5045] font-medium py-1">{d}</div>
            ))}
            {grid.map((day, i) => (
              <motion.button
                key={i}
                initial={{ scale: 0.5, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                transition={{ delay: 0.15 + i * 0.01 }}
                onClick={() => setSelectedDay(selectedDay === i ? null : i)}
                className={`aspect-square rounded-lg flex items-center justify-center text-[10px] sm:text-xs font-medium transition-all hover:ring-2 hover:ring-[#a78bfa]/50 ${stressColor(day.stress)} ${
                  selectedDay === i ? "ring-2 ring-[#a78bfa]" : ""
                }`}
                title={`${day.date}: Stress ${day.stress}%`}
              >
                {day.dayLabel}
              </motion.button>
            ))}
          </div>
          <div className="flex items-center justify-between mt-4 text-[10px] sm:text-xs text-[#5a5045]">
            <div className="flex items-center gap-2 flex-wrap">
              <div className="flex items-center gap-1"><div className="w-3 h-3 rounded bg-emerald-400/60" /> Low</div>
              <div className="flex items-center gap-1"><div className="w-3 h-3 rounded bg-amber-400/50" /> Mod</div>
              <div className="flex items-center gap-1"><div className="w-3 h-3 rounded bg-red-400/70" /> High</div>
            </div>
            <span className="hidden sm:inline">Click on a day for details</span>
          </div>
        </div>
      </AnimatedSection>

      {/* Selected Day Details */}
      <AnimatePresence>
        {selectedDay !== null && grid[selectedDay] && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: "auto", opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            className="mb-6 overflow-hidden"
          >
            <div className="bg-[#16140f] rounded-xl border border-[#a78bfa]/30 p-4 sm:p-6">
              <h3 className="text-sm font-semibold text-[#a78bfa] mb-3">{grid[selectedDay].date} — Stress: {grid[selectedDay].stress}% | Sleep: {grid[selectedDay].sleep}%</h3>
              <div className="flex flex-wrap gap-2">
                {grid[selectedDay].stressors.map((s) => (
                  <span key={s} className="px-3 py-1.5 rounded-lg bg-[#1e1c18] border border-[#2a2520] text-xs text-[#d4c5a9]">{s}</span>
                ))}
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Trend Chart */}
      <AnimatedSection delay={0.15}>
        <div className="bg-[#16140f] rounded-xl border border-[#2a2520] p-4 sm:p-6 mb-6">
          <h2 className="text-sm font-semibold text-[#8a7a6a] uppercase tracking-wider mb-4">Trend Stres 28 Zile</h2>
          <div className="h-44 sm:h-56">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={trendData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#2a2520" />
                <XAxis dataKey="date" tick={{ fill: "#5a5045", fontSize: 10 }} interval={3} />
                <YAxis domain={[0, 100]} tick={{ fill: "#5a5045", fontSize: 10 }} />
                <Tooltip contentStyle={{ background: "#16140f", border: "1px solid #2a2520", borderRadius: 8 }} />
                <Line type="monotone" dataKey="stress" stroke="#a78bfa" strokeWidth={2} dot={false} name="Stress" />
                <Line type="monotone" dataKey="avg" stroke="#5a5045" strokeWidth={1} strokeDasharray="5 5" dot={false} name="Media" />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </AnimatedSection>

      {/* Pattern Detection */}
      <AnimatedSection delay={0.2}>
        <div className="bg-[#16140f] rounded-xl border border-[#2a2520] p-4 sm:p-6 mb-6">
          <h2 className="text-sm font-semibold text-[#8a7a6a] uppercase tracking-wider mb-4">Pattern Detection</h2>
          <div className="space-y-3">
            {patterns.map((p, i) => (
              <motion.div
                key={i}
                initial={{ opacity: 0, x: -10 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.3 + i * 0.1 }}
                className="flex items-start gap-3 p-3 rounded-lg bg-[#1e1c18] border border-[#2a2520] hover:border-[#a78bfa]/20 transition-colors"
              >
                <div className="w-6 h-6 rounded-full bg-[#a78bfa]/20 flex items-center justify-center text-xs text-[#a78bfa] shrink-0 mt-0.5">
                  {i + 1}
                </div>
                <p className="text-xs sm:text-sm text-[#d4c5a9]">{p}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </AnimatedSection>

      {/* High-Stress Patients */}
      <AnimatedSection delay={0.25}>
        <div className="bg-[#16140f] rounded-xl border border-[#2a2520] p-4 sm:p-6">
          <h2 className="text-sm font-semibold text-[#8a7a6a] uppercase tracking-wider mb-4">High-Stress Individuals</h2>
          {patients.length === 0 ? (
            <div className="text-center py-8 text-[#5a5045]">
              <div className="text-3xl mb-2">✨</div>
              <p className="text-sm">No high-stress individuals detected</p>
            </div>
          ) : (
            <StaggerContainer className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {patients.map((p) => (
                <StaggerItem key={p.id}>
                  <motion.div
                    whileHover={{ scale: 1.02 }}
                    className="flex items-center justify-between p-3 rounded-lg bg-[#1e1c18] border border-[#2a2520] hover:border-[#a78bfa]/20 transition-all"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-[#a78bfa]/20 flex items-center justify-center text-xs text-[#a78bfa] font-bold">
                        {p.name.split(" ").map((n) => n[0]).join("")}
                      </div>
                      <div>
                        <div className="text-xs sm:text-sm font-medium text-[#d4c5a9]">{p.name}</div>
                        <div className="text-[10px] sm:text-xs text-[#5a5045]">{p.unit}</div>
                      </div>
                    </div>
                    <div className={`text-base sm:text-lg font-bold ${p.metrics.stressScore > 65 ? "text-red-400" : "text-amber-400"}`}>
                      {p.metrics.stressScore}%
                    </div>
                  </motion.div>
                </StaggerItem>
              ))}
            </StaggerContainer>
          )}
        </div>
      </AnimatedSection>
    </main>
  );
}
