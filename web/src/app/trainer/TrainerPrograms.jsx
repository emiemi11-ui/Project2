import { useState, useMemo } from "react";
import { programs as mockPrograms } from "../../data/mockData";
import { AnimatedSection, StaggerContainer, StaggerItem, AnimatedNumber } from "../../components/AnimatedSection";
import { motion, AnimatePresence } from "framer-motion";

const localPrograms = [
  {
    id: 1, title: "Alpha Squad — Strength Phase", duration: "6 weeks", intensity: "High",
    athletes: 8, status: "active", startDate: "2026-02-10", endDate: "2026-03-23",
    description: "Progressive overload program focusing on compound lifts. 4 sessions/week.",
    exercises: ["Squat", "Deadlift", "Bench Press", "Pull-ups", "Rows"], completionRate: 65,
  },
  {
    id: 2, title: "Bravo Team — Endurance Block", duration: "4 weeks", intensity: "Moderate",
    athletes: 12, status: "active", startDate: "2026-02-24", endDate: "2026-03-23",
    description: "Aerobic base building with zone 2 running and interval sessions.",
    exercises: ["5K Tempo", "Interval 400m", "Long Run", "Recovery Jog", "Hill Repeats"], completionRate: 40,
  },
  {
    id: 3, title: "Recovery Protocol — Post-Injury", duration: "8 weeks", intensity: "Low",
    athletes: 3, status: "active", startDate: "2026-01-27", endDate: "2026-03-23",
    description: "Gradual return-to-activity program with mobility focus.",
    exercises: ["Mobility Drills", "Bodyweight Squats", "Resistance Bands", "Light Cardio"], completionRate: 78,
  },
  {
    id: 4, title: "Pre-Deployment Readiness", duration: "3 weeks", intensity: "Very High",
    athletes: 22, status: "upcoming", startDate: "2026-03-24", endDate: "2026-04-14",
    description: "Peak readiness program combining strength, endurance, and tactical drills.",
    exercises: ["Ruck March", "Combat PT", "Obstacle Course", "Sprint Intervals", "Team Carries"], completionRate: 0,
  },
  {
    id: 5, title: "Winter Conditioning", duration: "5 weeks", intensity: "Moderate",
    athletes: 15, status: "completed", startDate: "2026-01-06", endDate: "2026-02-09",
    description: "Cold-weather adaptation and base fitness maintenance.",
    exercises: ["Indoor Circuit", "Swimming", "Rowing", "Core Work", "Stretching"], completionRate: 100,
  },
];

const statusStyles = {
  active: { bg: "bg-emerald-500/15", text: "text-emerald-400", label: "Active", border: "border-emerald-500/30" },
  upcoming: { bg: "bg-cyan-500/15", text: "text-cyan-400", label: "Upcoming", border: "border-cyan-500/30" },
  completed: { bg: "bg-gray-500/15", text: "text-gray-400", label: "Completed", border: "border-gray-500/30" },
};

const intensityColors = {
  Low: "text-blue-400", Moderate: "text-amber-400", High: "text-orange-400", "Very High": "text-red-400",
};

export default function TrainerPrograms() {
  const [filter, setFilter] = useState("all");
  const [expanded, setExpanded] = useState(null);

  const programs = localPrograms;
  const filtered = filter === "all" ? programs : programs.filter((p) => p.status === filter);

  const stats = useMemo(() => ({
    total: programs.length,
    active: programs.filter((p) => p.status === "active").length,
    totalAthletes: programs.filter((p) => p.status === "active").reduce((s, p) => s + p.athletes, 0),
    avgCompletion: Math.round(
      programs.filter((p) => p.status === "active").reduce((s, p) => s + p.completionRate, 0) /
      (programs.filter((p) => p.status === "active").length || 1)
    ),
  }), []);

  return (
    <main className="p-4 sm:p-6 overflow-auto">
      <AnimatedSection>
        <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
          <h1 className="text-xl sm:text-2xl font-bold">Training Programs</h1>
          <div className="flex items-center gap-4 sm:gap-6">
            <div className="text-center">
              <div className="text-xl font-bold text-orange-400"><AnimatedNumber value={stats.active} /></div>
              <div className="text-[10px] text-gray-500 uppercase tracking-wider">Active</div>
            </div>
            <div className="text-center">
              <div className="text-xl font-bold text-cyan-400"><AnimatedNumber value={stats.totalAthletes} /></div>
              <div className="text-[10px] text-gray-500 uppercase tracking-wider">Athletes</div>
            </div>
            <div className="text-center">
              <div className="text-xl font-bold text-emerald-400"><AnimatedNumber value={stats.avgCompletion} />%</div>
              <div className="text-[10px] text-gray-500 uppercase tracking-wider">Avg Complete</div>
            </div>
          </div>
        </div>
      </AnimatedSection>

      <AnimatedSection delay={0.1}>
        <div className="flex flex-wrap items-center gap-2 sm:gap-3 mb-6">
          {["all", "active", "upcoming", "completed"].map((f) => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-3 sm:px-4 py-1.5 rounded-lg text-xs sm:text-sm font-medium transition-all ${
                filter === f
                  ? "bg-orange-500/20 text-orange-400 border border-orange-500/30"
                  : "bg-gray-800/50 text-gray-400 border border-gray-700/50 hover:text-white"
              }`}
            >
              {f.charAt(0).toUpperCase() + f.slice(1)}
              {f !== "all" && <span className="ml-1.5 text-[10px] opacity-60">({programs.filter((p) => f === "all" || p.status === f).length})</span>}
            </button>
          ))}
        </div>
      </AnimatedSection>

      {filtered.length === 0 && (
        <AnimatedSection>
          <div className="text-center py-16 text-gray-500">
            <div className="text-4xl mb-4">📋</div>
            <p className="text-lg font-medium">No programs match this filter</p>
          </div>
        </AnimatedSection>
      )}

      <StaggerContainer className="space-y-4">
        {filtered.map((prog) => {
          const st = statusStyles[prog.status];
          const isExpanded = expanded === prog.id;
          return (
            <StaggerItem key={prog.id}>
              <motion.div
                layout
                className={`rounded-xl bg-[#0d1117] border ${isExpanded ? "border-orange-500/30" : "border-gray-800/50"} overflow-hidden hover:border-orange-500/20 transition-all`}
              >
                <button
                  onClick={() => setExpanded(isExpanded ? null : prog.id)}
                  className="w-full text-left p-4 sm:p-5 flex items-start sm:items-center justify-between hover:bg-gray-800/10 transition-colors"
                >
                  <div className="flex-1">
                    <div className="flex flex-wrap items-center gap-2 mb-1">
                      <h3 className="font-semibold text-white text-sm sm:text-base">{prog.title}</h3>
                      <span className={`px-2 py-0.5 rounded-full text-[10px] sm:text-xs font-medium ${st.bg} ${st.text}`}>{st.label}</span>
                      <span className={`text-[10px] sm:text-xs font-medium ${intensityColors[prog.intensity]}`}>{prog.intensity}</span>
                    </div>
                    <p className="text-xs sm:text-sm text-gray-500">{prog.description}</p>
                    {/* Progress bar */}
                    {prog.status !== "upcoming" && (
                      <div className="mt-3 max-w-xs">
                        <div className="flex items-center justify-between text-[10px] text-gray-500 mb-1">
                          <span>Completion</span>
                          <span>{prog.completionRate}%</span>
                        </div>
                        <div className="h-1.5 bg-gray-800 rounded-full overflow-hidden">
                          <motion.div
                            initial={{ width: 0 }}
                            animate={{ width: `${prog.completionRate}%` }}
                            transition={{ duration: 0.8, delay: 0.3 }}
                            className="h-full rounded-full bg-gradient-to-r from-orange-500 to-cyan-500"
                          />
                        </div>
                      </div>
                    )}
                  </div>
                  <div className="flex items-center gap-3 ml-4">
                    <div className="flex -space-x-2">
                      {Array.from({ length: Math.min(4, prog.athletes) }).map((_, i) => (
                        <div key={i} className="w-6 h-6 rounded-full bg-gradient-to-br from-orange-500/30 to-cyan-500/30 border border-gray-700 flex items-center justify-center text-[8px] text-gray-400">
                          {i + 1}
                        </div>
                      ))}
                      {prog.athletes > 4 && (
                        <div className="w-6 h-6 rounded-full bg-gray-800 border border-gray-700 flex items-center justify-center text-[8px] text-gray-400">
                          +{prog.athletes - 4}
                        </div>
                      )}
                    </div>
                    <motion.svg
                      animate={{ rotate: isExpanded ? 180 : 0 }}
                      className="w-5 h-5 text-gray-500"
                      fill="none" stroke="currentColor" viewBox="0 0 24 24"
                    >
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </motion.svg>
                  </div>
                </button>

                <AnimatePresence>
                  {isExpanded && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: "auto", opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.3 }}
                      className="overflow-hidden"
                    >
                      <div className="px-4 sm:px-5 pb-5 border-t border-gray-800/30 pt-4">
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4 mb-4">
                          <div className="bg-[#0a0f14] rounded-lg p-3 border border-gray-800/30">
                            <div className="text-xs text-gray-500 uppercase tracking-wider">Duration</div>
                            <div className="text-sm font-medium mt-1">{prog.duration}</div>
                          </div>
                          <div className="bg-[#0a0f14] rounded-lg p-3 border border-gray-800/30">
                            <div className="text-xs text-gray-500 uppercase tracking-wider">Intensity</div>
                            <div className={`text-sm font-medium mt-1 ${intensityColors[prog.intensity]}`}>{prog.intensity}</div>
                          </div>
                          <div className="bg-[#0a0f14] rounded-lg p-3 border border-gray-800/30">
                            <div className="text-xs text-gray-500 uppercase tracking-wider">Athletes</div>
                            <div className="text-sm font-medium mt-1">{prog.athletes}</div>
                          </div>
                          <div className="bg-[#0a0f14] rounded-lg p-3 border border-gray-800/30">
                            <div className="text-xs text-gray-500 uppercase tracking-wider">Period</div>
                            <div className="text-sm font-medium mt-1">{prog.startDate.slice(5)} → {prog.endDate.slice(5)}</div>
                          </div>
                        </div>
                        <div>
                          <div className="text-xs text-gray-500 uppercase tracking-wider mb-2">Exercises</div>
                          <div className="flex flex-wrap gap-2">
                            {prog.exercises.map((ex) => (
                              <span key={ex} className="px-3 py-1.5 rounded-lg bg-gray-800/50 border border-gray-700/50 text-xs text-gray-300 hover:border-orange-500/30 transition-colors">{ex}</span>
                            ))}
                          </div>
                        </div>
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </motion.div>
            </StaggerItem>
          );
        })}
      </StaggerContainer>
    </main>
  );
}
