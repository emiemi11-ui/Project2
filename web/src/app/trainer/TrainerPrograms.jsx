import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

const programs = [
  {
    id: 1,
    title: "Alpha Squad — Strength Phase",
    duration: "6 weeks",
    intensity: "High",
    athletes: 8,
    status: "active",
    startDate: "2026-02-10",
    endDate: "2026-03-23",
    description: "Progressive overload program focusing on compound lifts. 4 sessions/week.",
    exercises: ["Squat", "Deadlift", "Bench Press", "Pull-ups", "Rows"],
  },
  {
    id: 2,
    title: "Bravo Team — Endurance Block",
    duration: "4 weeks",
    intensity: "Moderate",
    athletes: 12,
    status: "active",
    startDate: "2026-02-24",
    endDate: "2026-03-23",
    description: "Aerobic base building with zone 2 running and interval sessions.",
    exercises: ["5K Tempo", "Interval 400m", "Long Run", "Recovery Jog", "Hill Repeats"],
  },
  {
    id: 3,
    title: "Recovery Protocol — Post-Injury",
    duration: "8 weeks",
    intensity: "Low",
    athletes: 3,
    status: "active",
    startDate: "2026-01-27",
    endDate: "2026-03-23",
    description: "Gradual return-to-activity program with mobility focus.",
    exercises: ["Mobility Drills", "Bodyweight Squats", "Resistance Bands", "Light Cardio"],
  },
  {
    id: 4,
    title: "Pre-Deployment Readiness",
    duration: "3 weeks",
    intensity: "Very High",
    athletes: 22,
    status: "upcoming",
    startDate: "2026-03-24",
    endDate: "2026-04-14",
    description: "Peak readiness program combining strength, endurance, and tactical drills.",
    exercises: ["Ruck March", "Combat PT", "Obstacle Course", "Sprint Intervals", "Team Carries"],
  },
  {
    id: 5,
    title: "Winter Conditioning",
    duration: "5 weeks",
    intensity: "Moderate",
    athletes: 15,
    status: "completed",
    startDate: "2026-01-06",
    endDate: "2026-02-09",
    description: "Cold-weather adaptation and base fitness maintenance.",
    exercises: ["Indoor Circuit", "Swimming", "Rowing", "Core Work", "Stretching"],
  },
];

const statusStyles = {
  active: { bg: "bg-emerald-500/15", text: "text-emerald-400", label: "Active" },
  upcoming: { bg: "bg-cyan-500/15", text: "text-cyan-400", label: "Upcoming" },
  completed: { bg: "bg-gray-500/15", text: "text-gray-400", label: "Completed" },
};

const intensityColors = {
  Low: "text-blue-400",
  Moderate: "text-amber-400",
  High: "text-orange-400",
  "Very High": "text-red-400",
};

export default function TrainerPrograms() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [filter, setFilter] = useState("all");
  const [expanded, setExpanded] = useState(null);

  const filtered = filter === "all" ? programs : programs.filter((p) => p.status === filter);

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
            { label: "Programs", path: "/app/programs", active: true },
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
        <button onClick={logout} className="text-sm text-gray-500 hover:text-red-400 transition-colors px-3 py-2">Logout</button>
      </aside>

      <main className="flex-1 p-6 overflow-auto">
        <h1 className="text-2xl font-bold mb-6">Training Programs</h1>

        <div className="flex items-center gap-3 mb-6">
          {["all", "active", "upcoming", "completed"].map((f) => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-4 py-1.5 rounded-lg text-sm font-medium transition-colors ${
                filter === f
                  ? "bg-orange-500/20 text-orange-400 border border-orange-500/30"
                  : "bg-gray-800/50 text-gray-400 border border-gray-700/50 hover:text-white"
              }`}
            >
              {f.charAt(0).toUpperCase() + f.slice(1)}
            </button>
          ))}
        </div>

        <div className="space-y-4">
          {filtered.map((prog) => {
            const st = statusStyles[prog.status];
            return (
              <div key={prog.id} className="rounded-xl bg-[#0d1117] border border-gray-800/50 overflow-hidden">
                <button
                  onClick={() => setExpanded(expanded === prog.id ? null : prog.id)}
                  className="w-full text-left p-5 flex items-center justify-between hover:bg-gray-800/20 transition-colors"
                >
                  <div className="flex items-center gap-4">
                    <div>
                      <h3 className="font-semibold text-white">{prog.title}</h3>
                      <p className="text-sm text-gray-500 mt-1">{prog.description}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-4">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${st.bg} ${st.text}`}>{st.label}</span>
                    <svg className={`w-5 h-5 text-gray-500 transition-transform ${expanded === prog.id ? "rotate-180" : ""}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </svg>
                  </div>
                </button>
                {expanded === prog.id && (
                  <div className="px-5 pb-5 border-t border-gray-800/30 pt-4">
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                      <div>
                        <div className="text-xs text-gray-500 uppercase tracking-wider">Duration</div>
                        <div className="text-sm font-medium mt-1">{prog.duration}</div>
                      </div>
                      <div>
                        <div className="text-xs text-gray-500 uppercase tracking-wider">Intensity</div>
                        <div className={`text-sm font-medium mt-1 ${intensityColors[prog.intensity]}`}>{prog.intensity}</div>
                      </div>
                      <div>
                        <div className="text-xs text-gray-500 uppercase tracking-wider">Athletes</div>
                        <div className="text-sm font-medium mt-1">{prog.athletes}</div>
                      </div>
                      <div>
                        <div className="text-xs text-gray-500 uppercase tracking-wider">Period</div>
                        <div className="text-sm font-medium mt-1">{prog.startDate.slice(5)} → {prog.endDate.slice(5)}</div>
                      </div>
                    </div>
                    <div>
                      <div className="text-xs text-gray-500 uppercase tracking-wider mb-2">Exercises</div>
                      <div className="flex flex-wrap gap-2">
                        {prog.exercises.map((ex) => (
                          <span key={ex} className="px-3 py-1 rounded-lg bg-gray-800/50 border border-gray-700/50 text-xs text-gray-300">{ex}</span>
                        ))}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </main>
    </div>
  );
}
