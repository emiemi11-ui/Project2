import { useState, useEffect, useRef } from "react";
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip } from "recharts";
import { AnimatedSection, StaggerContainer, StaggerItem, AnimatedNumber } from "../../components/AnimatedSection";
import { motion } from "framer-motion";

const exercises = [
  { id: 1, name: "WARM-UP JOG", duration: "5:00", sets: "1x", reps: "800m", completed: true, targetSets: 1, completedSets: 1 },
  { id: 2, name: "PUSH-UPS", duration: "—", sets: "3x", reps: "40", completed: true, targetSets: 3, completedSets: 3 },
  { id: 3, name: "PULL-UPS", duration: "—", sets: "3x", reps: "15", completed: true, targetSets: 3, completedSets: 3 },
  { id: 4, name: "SQUATS", duration: "—", sets: "4x", reps: "20", completed: false, targetSets: 4, completedSets: 2 },
  { id: 5, name: "PLANK HOLD", duration: "1:30", sets: "3x", reps: "90s", completed: false, targetSets: 3, completedSets: 0 },
  { id: 6, name: "SPRINT INTERVALS", duration: "—", sets: "6x", reps: "200m", completed: false, targetSets: 6, completedSets: 0 },
  { id: 7, name: "BURPEES", duration: "—", sets: "3x", reps: "20", completed: false, targetSets: 3, completedSets: 0 },
  { id: 8, name: "COOL-DOWN STRETCH", duration: "5:00", sets: "1x", reps: "—", completed: false, targetSets: 1, completedSets: 0 },
];

function Timer({ initialSeconds }) {
  const [seconds, setSeconds] = useState(initialSeconds);
  const [running, setRunning] = useState(false);
  const intervalRef = useRef(null);

  useEffect(() => {
    if (running && seconds > 0) {
      intervalRef.current = setInterval(() => setSeconds((s) => s - 1), 1000);
    }
    return () => clearInterval(intervalRef.current);
  }, [running, seconds]);

  useEffect(() => {
    if (seconds === 0) setRunning(false);
  }, [seconds]);

  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  const pct = ((initialSeconds - seconds) / initialSeconds) * 100;

  return (
    <div className="text-center">
      {/* Circular progress */}
      <div className="relative w-36 h-36 sm:w-44 sm:h-44 mx-auto mb-4">
        <svg className="w-full h-full -rotate-90" viewBox="0 0 100 100">
          <circle cx="50" cy="50" r="45" fill="none" stroke="#1f2937" strokeWidth="4" />
          <motion.circle
            cx="50" cy="50" r="45" fill="none" stroke="#00d4ff" strokeWidth="4"
            strokeDasharray={`${pct * 2.827} 283`}
            strokeLinecap="round"
            transition={{ duration: 0.3 }}
          />
        </svg>
        <div className="absolute inset-0 flex items-center justify-center">
          <div className="text-3xl sm:text-4xl font-bold font-mono text-cyan-400">
            {String(mins).padStart(2, "0")}:{String(secs).padStart(2, "0")}
          </div>
        </div>
      </div>
      <div className="flex items-center justify-center gap-3">
        <motion.button
          whileTap={{ scale: 0.95 }}
          onClick={() => setRunning(!running)}
          className={`px-6 py-2 rounded text-xs uppercase tracking-widest font-bold transition-colors ${
            running
              ? "bg-amber-500/20 text-amber-400 border border-amber-500/30"
              : "bg-[#84cc16]/20 text-[#84cc16] border border-[#84cc16]/30"
          }`}
        >
          {running ? "PAUSE" : seconds === initialSeconds ? "START" : "RESUME"}
        </motion.button>
        <motion.button
          whileTap={{ scale: 0.95 }}
          onClick={() => { setRunning(false); setSeconds(initialSeconds); }}
          className="px-4 py-2 rounded text-xs uppercase tracking-widest text-gray-500 border border-gray-700/50 hover:text-white transition-colors"
        >
          RESET
        </motion.button>
      </div>
    </div>
  );
}

export default function SoldierTraining() {
  const [exList, setExList] = useState(exercises);

  const toggleComplete = (id) => {
    setExList((prev) => prev.map((ex) => (ex.id === id ? { ...ex, completed: !ex.completed } : ex)));
  };

  const completedCount = exList.filter((e) => e.completed).length;
  const totalExercises = exList.length;

  const perfData = exList.map((ex) => ({
    name: ex.name.split(" ")[0],
    target: ex.targetSets,
    done: ex.completedSets,
  }));

  return (
    <main className="max-w-3xl mx-auto p-4 sm:p-6">
      <AnimatedSection>
        <h1 className="text-lg sm:text-xl font-bold uppercase tracking-wider mb-1">TODAY'S PROGRAM</h1>
        <p className="text-[10px] sm:text-xs text-gray-600 uppercase tracking-[0.2em] mb-6 font-mono">
          {new Date().toLocaleDateString("en-US", { weekday: "long", year: "numeric", month: "long", day: "numeric" }).toUpperCase()}
        </p>
      </AnimatedSection>

      {/* Progress */}
      <AnimatedSection delay={0.1}>
        <div className="bg-[#0d1117] rounded-xl border border-gray-800/50 p-4 sm:p-6 mb-6">
          <div className="flex items-center justify-between mb-3">
            <span className="text-[10px] sm:text-xs text-gray-500 uppercase tracking-widest font-mono">PROGRESS</span>
            <span className="text-base sm:text-lg font-bold font-mono">
              <span className="text-[#84cc16]"><AnimatedNumber value={completedCount} /></span>
              <span className="text-gray-600">/{totalExercises}</span>
            </span>
          </div>
          <div className="w-full h-3 bg-gray-800 rounded-full overflow-hidden">
            <motion.div
              initial={{ width: 0 }}
              animate={{ width: `${(completedCount / totalExercises) * 100}%` }}
              transition={{ duration: 0.8, delay: 0.3 }}
              className="h-full bg-gradient-to-r from-[#84cc16] to-cyan-500 rounded-full"
            />
          </div>
          <div className="text-[10px] text-gray-600 mt-2 uppercase tracking-wider font-mono text-center">
            {completedCount === totalExercises ? "ALL EXERCISES COMPLETED — OUTSTANDING" :
             completedCount > totalExercises / 2 ? "KEEP PUSHING — PAST HALFWAY" :
             "MISSION IN PROGRESS"}
          </div>
        </div>
      </AnimatedSection>

      {/* Timer */}
      <AnimatedSection delay={0.15}>
        <div className="bg-[#0d1117] rounded-xl border border-gray-800/50 p-4 sm:p-6 mb-6">
          <h2 className="text-[10px] sm:text-xs text-gray-600 uppercase tracking-[0.2em] mb-4 text-center font-mono">EXERCISE TIMER</h2>
          <Timer initialSeconds={90} />
        </div>
      </AnimatedSection>

      {/* Performance Chart */}
      <AnimatedSection delay={0.2}>
        <div className="bg-[#0d1117] rounded-xl border border-gray-800/50 p-4 sm:p-6 mb-6">
          <h2 className="text-[10px] sm:text-xs text-gray-600 uppercase tracking-[0.2em] mb-4 font-mono">SETS: TARGET vs COMPLETED</h2>
          <div className="h-36 sm:h-44">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={perfData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
                <XAxis dataKey="name" tick={{ fill: "#6b7280", fontSize: 9 }} angle={-30} textAnchor="end" height={40} />
                <YAxis tick={{ fill: "#6b7280", fontSize: 10 }} />
                <Tooltip contentStyle={{ background: "#0d1117", border: "1px solid #1f2937", borderRadius: 8, fontFamily: "monospace" }} />
                <Bar dataKey="target" fill="#374151" radius={[2, 2, 0, 0]} name="Target" />
                <Bar dataKey="done" fill="#84cc16" radius={[2, 2, 0, 0]} name="Done" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </AnimatedSection>

      {/* Exercise List */}
      <AnimatedSection delay={0.25}>
        <StaggerContainer className="space-y-2">
          {exList.map((ex) => (
            <StaggerItem key={ex.id}>
              <motion.div
                whileHover={{ x: 4 }}
                className={`rounded-xl border p-3 sm:p-4 flex items-center justify-between transition-all ${
                  ex.completed
                    ? "bg-[#84cc16]/5 border-[#84cc16]/20"
                    : "bg-[#0d1117] border-gray-800/50 hover:border-[#84cc16]/20"
                }`}
              >
                <div className="flex items-center gap-3">
                  <motion.button
                    onClick={() => toggleComplete(ex.id)}
                    whileTap={{ scale: 0.9 }}
                    className={`w-6 h-6 sm:w-7 sm:h-7 rounded border-2 flex items-center justify-center transition-colors ${
                      ex.completed ? "bg-[#84cc16] border-[#84cc16]" : "border-gray-600 hover:border-[#84cc16]"
                    }`}
                  >
                    {ex.completed && (
                      <motion.svg initial={{ scale: 0 }} animate={{ scale: 1 }} className="w-3.5 h-3.5 sm:w-4 sm:h-4 text-black" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                      </motion.svg>
                    )}
                  </motion.button>
                  <div>
                    <div className={`text-xs sm:text-sm font-bold uppercase tracking-wider font-mono ${ex.completed ? "text-gray-500 line-through" : "text-white"}`}>
                      {ex.name}
                    </div>
                    <div className="text-[10px] sm:text-xs text-gray-600 mt-0.5 font-mono">{ex.sets} — {ex.reps}</div>
                  </div>
                </div>
                {ex.duration !== "—" && (
                  <span className="text-[10px] sm:text-xs text-cyan-400 font-mono">{ex.duration}</span>
                )}
              </motion.div>
            </StaggerItem>
          ))}
        </StaggerContainer>
      </AnimatedSection>
    </main>
  );
}
