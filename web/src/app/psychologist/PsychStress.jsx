import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { persons } from "../../data/mockData";

function generateStressGrid() {
  const grid = [];
  const today = new Date();
  for (let i = 27; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(d.getDate() - i);
    const dayOfWeek = d.getDay();
    const isWeekend = dayOfWeek === 0 || dayOfWeek === 6;
    const base = isWeekend ? 25 : 45;
    const stress = Math.min(100, Math.max(5, base + Math.round((Math.random() - 0.4) * 40)));
    grid.push({
      date: d.toISOString().split("T")[0],
      dayLabel: d.getDate(),
      weekday: d.toLocaleDateString("en-US", { weekday: "short" }),
      stress,
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
  const navigate = useNavigate();
  const { logout } = useAuth();
  const grid = useMemo(() => generateStressGrid(), []);
  const [selectedDay, setSelectedDay] = useState(null);

  const patients = useMemo(() => {
    return persons
      .filter((p) => p.scores && p.scores.stress > 50)
      .sort((a, b) => b.scores.stress - a.scores.stress)
      .slice(0, 8);
  }, []);

  const patterns = [
    "Nivelul de stres creste constant Luni-Miercuri si scade spre weekend.",
    "Pacientii din Charlie Company au stres mediu cu 23% mai mare decat media.",
    "Corelatia somn-stres: calitatea somnului sub 60 se asociaza cu stres >55.",
  ];

  return (
    <div className="min-h-screen bg-[#12100e] text-white">
      {/* Sidebar */}
      <div className="flex">
        <aside className="w-60 bg-[#16140f] border-r border-[#2a2520] flex flex-col p-4 min-h-screen hidden lg:flex">
          <div className="flex items-center gap-2 mb-8">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-violet-400 to-amber-300 flex items-center justify-center font-bold text-black text-sm">V</div>
            <span className="font-bold text-lg tracking-tight">Vita<span className="text-violet-400">Nova</span></span>
          </div>
          <nav className="flex-1 space-y-1">
            {[
              { label: "Overview", path: "/app/mood" },
              { label: "Mood", path: "/app/mood" },
              { label: "Stress", path: "/app/stress", active: true },
              { label: "Sessions", path: "/app/sessions" },
            ].map((item) => (
              <button
                key={item.label}
                onClick={() => navigate(item.path)}
                className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors ${
                  item.active ? "bg-violet-500/15 text-violet-400" : "text-[#a09080] hover:text-white hover:bg-[#1e1c18]"
                }`}
              >
                {item.label}
              </button>
            ))}
          </nav>
          <button onClick={logout} className="text-sm text-[#5a5045] hover:text-red-400 transition-colors px-3 py-2">Logout</button>
        </aside>

        <main className="flex-1 p-6 overflow-auto">
          <h1 className="text-2xl font-bold mb-2 text-[#d4c5a9]">Stress Heatmap</h1>
          <p className="text-sm text-[#8a7a6a] mb-8">Pattern detection si analiza factorilor de stres</p>

          {/* Heatmap */}
          <div className="bg-[#16140f] rounded-xl border border-[#2a2520] p-6 mb-6">
            <h2 className="text-sm font-semibold text-[#8a7a6a] uppercase tracking-wider mb-4">Ultimele 28 de zile</h2>
            <div className="grid grid-cols-7 gap-2">
              {["L", "Ma", "Mi", "J", "V", "S", "D"].map((d) => (
                <div key={d} className="text-center text-xs text-[#5a5045] font-medium py-1">{d}</div>
              ))}
              {grid.map((day, i) => (
                <button
                  key={i}
                  onClick={() => setSelectedDay(selectedDay === i ? null : i)}
                  className={`aspect-square rounded-lg flex items-center justify-center text-xs font-medium transition-all hover:ring-2 hover:ring-violet-400/50 ${stressColor(day.stress)} ${
                    selectedDay === i ? "ring-2 ring-violet-400" : ""
                  }`}
                  title={`${day.date}: Stress ${day.stress}%`}
                >
                  {day.dayLabel}
                </button>
              ))}
            </div>
            <div className="flex items-center justify-between mt-4 text-xs text-[#5a5045]">
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 rounded bg-emerald-400/60" /> Low
                <div className="w-3 h-3 rounded bg-amber-400/50" /> Moderate
                <div className="w-3 h-3 rounded bg-red-400/70" /> High
              </div>
              <span>Click on a day for details</span>
            </div>
          </div>

          {/* Selected Day Details */}
          {selectedDay !== null && grid[selectedDay] && (
            <div className="bg-[#16140f] rounded-xl border border-violet-500/30 p-6 mb-6">
              <h3 className="text-sm font-semibold text-violet-400 mb-3">{grid[selectedDay].date} — Stress: {grid[selectedDay].stress}%</h3>
              <div className="flex flex-wrap gap-2">
                {grid[selectedDay].stressors.map((s) => (
                  <span key={s} className="px-3 py-1 rounded-lg bg-[#1e1c18] border border-[#2a2520] text-xs text-[#d4c5a9]">{s}</span>
                ))}
              </div>
            </div>
          )}

          {/* Pattern Detection */}
          <div className="bg-[#16140f] rounded-xl border border-[#2a2520] p-6 mb-6">
            <h2 className="text-sm font-semibold text-[#8a7a6a] uppercase tracking-wider mb-4">Pattern Detection</h2>
            <div className="space-y-3">
              {patterns.map((p, i) => (
                <div key={i} className="flex items-start gap-3 p-3 rounded-lg bg-[#1e1c18] border border-[#2a2520]">
                  <div className="w-6 h-6 rounded-full bg-violet-500/20 flex items-center justify-center text-xs text-violet-400 flex-shrink-0 mt-0.5">
                    {i + 1}
                  </div>
                  <p className="text-sm text-[#d4c5a9]">{p}</p>
                </div>
              ))}
            </div>
          </div>

          {/* High-Stress Patients */}
          <div className="bg-[#16140f] rounded-xl border border-[#2a2520] p-6">
            <h2 className="text-sm font-semibold text-[#8a7a6a] uppercase tracking-wider mb-4">High-Stress Individuals</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {patients.map((p) => (
                <div key={p.id} className="flex items-center justify-between p-3 rounded-lg bg-[#1e1c18] border border-[#2a2520]">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full bg-violet-500/20 flex items-center justify-center text-xs text-violet-400 font-bold">
                      {p.name.split(" ").map((n) => n[0]).join("")}
                    </div>
                    <div>
                      <div className="text-sm font-medium text-[#d4c5a9]">{p.name}</div>
                      <div className="text-xs text-[#5a5045]">{p.unit}</div>
                    </div>
                  </div>
                  <div className={`text-lg font-bold ${p.scores.stress > 65 ? "text-red-400" : "text-amber-400"}`}>
                    {p.scores.stress}%
                  </div>
                </div>
              ))}
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
