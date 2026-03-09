import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

const exercises = [
  { id: 1, name: "WARM-UP JOG", duration: "5:00", sets: "1x", reps: "800m", completed: true },
  { id: 2, name: "PUSH-UPS", duration: "—", sets: "3x", reps: "40", completed: true },
  { id: 3, name: "PULL-UPS", duration: "—", sets: "3x", reps: "15", completed: true },
  { id: 4, name: "SQUATS", duration: "—", sets: "4x", reps: "20", completed: false },
  { id: 5, name: "PLANK HOLD", duration: "1:30", sets: "3x", reps: "90s", completed: false },
  { id: 6, name: "SPRINT INTERVALS", duration: "—", sets: "6x", reps: "200m", completed: false },
  { id: 7, name: "BURPEES", duration: "—", sets: "3x", reps: "20", completed: false },
  { id: 8, name: "COOL-DOWN STRETCH", duration: "5:00", sets: "1x", reps: "—", completed: false },
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

  return (
    <div className="text-center">
      <div className="text-4xl font-bold font-mono text-cyan-400">
        {String(mins).padStart(2, "0")}:{String(secs).padStart(2, "0")}
      </div>
      <div className="flex items-center justify-center gap-3 mt-3">
        <button
          onClick={() => setRunning(!running)}
          className={`px-6 py-2 rounded text-xs uppercase tracking-widest font-bold transition-colors ${
            running ? "bg-amber-500/20 text-amber-400 border border-amber-500/30" : "bg-lime-500/20 text-lime-400 border border-lime-500/30"
          }`}
        >
          {running ? "PAUSE" : seconds === initialSeconds ? "START" : "RESUME"}
        </button>
        <button
          onClick={() => { setRunning(false); setSeconds(initialSeconds); }}
          className="px-4 py-2 rounded text-xs uppercase tracking-widest text-gray-500 border border-gray-700/50 hover:text-white transition-colors"
        >
          RESET
        </button>
      </div>
    </div>
  );
}

export default function SoldierTraining() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [exList, setExList] = useState(exercises);

  const toggleComplete = (id) => {
    setExList((prev) => prev.map((ex) => (ex.id === id ? { ...ex, completed: !ex.completed } : ex)));
  };

  const completedCount = exList.filter((e) => e.completed).length;

  return (
    <div className="min-h-screen bg-[#0a0d0f] text-white font-mono">
      {/* Top Bar */}
      <nav className="bg-[#0d1117] border-b border-gray-800/50 px-6 py-3">
        <div className="max-w-5xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded bg-gradient-to-br from-lime-500 to-cyan-500 flex items-center justify-center font-bold text-black text-sm">V</div>
            <span className="font-bold text-lg tracking-tight uppercase">VitaNova</span>
          </div>
          <div className="flex items-center gap-4">
            {[
              { label: "STATUS", path: "/app/my-readiness" },
              { label: "TRAINING", path: "/app/my-training", active: true },
            ].map((item) => (
              <button
                key={item.label}
                onClick={() => navigate(item.path)}
                className={`text-xs px-3 py-1.5 rounded tracking-widest transition-colors ${
                  item.active ? "bg-lime-500/15 text-lime-400" : "text-gray-500 hover:text-white"
                }`}
              >
                {item.label}
              </button>
            ))}
            <button onClick={logout} className="text-xs text-gray-600 hover:text-red-400 uppercase tracking-wider ml-2">Exit</button>
          </div>
        </div>
      </nav>

      <main className="max-w-3xl mx-auto p-6">
        <h1 className="text-xl font-bold uppercase tracking-wider mb-2">TODAY'S PROGRAM</h1>
        <p className="text-xs text-gray-600 uppercase tracking-[0.2em] mb-6">
          {new Date().toLocaleDateString("en-US", { weekday: "long", year: "numeric", month: "long", day: "numeric" }).toUpperCase()}
        </p>

        {/* Progress */}
        <div className="bg-[#0d1117] rounded-xl border border-gray-800/50 p-6 mb-6">
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs text-gray-500 uppercase tracking-widest">PROGRESS</span>
            <span className="text-lg font-bold">
              <span className="text-lime-400">{completedCount}</span>
              <span className="text-gray-600">/{exList.length}</span>
            </span>
          </div>
          <div className="w-full h-2 bg-gray-800 rounded-full overflow-hidden">
            <div
              className="h-full bg-gradient-to-r from-lime-500 to-cyan-500 rounded-full transition-all duration-500"
              style={{ width: `${(completedCount / exList.length) * 100}%` }}
            />
          </div>
        </div>

        {/* Timer */}
        <div className="bg-[#0d1117] rounded-xl border border-gray-800/50 p-6 mb-6">
          <h2 className="text-xs text-gray-600 uppercase tracking-[0.2em] mb-4 text-center">EXERCISE TIMER</h2>
          <Timer initialSeconds={90} />
        </div>

        {/* Exercise List */}
        <div className="space-y-2">
          {exList.map((ex) => (
            <div
              key={ex.id}
              className={`rounded-xl border p-4 flex items-center justify-between transition-colors ${
                ex.completed
                  ? "bg-lime-500/5 border-lime-500/20"
                  : "bg-[#0d1117] border-gray-800/50"
              }`}
            >
              <div className="flex items-center gap-3">
                <button
                  onClick={() => toggleComplete(ex.id)}
                  className={`w-7 h-7 rounded border-2 flex items-center justify-center transition-colors ${
                    ex.completed ? "bg-lime-500 border-lime-500" : "border-gray-600 hover:border-lime-500"
                  }`}
                >
                  {ex.completed && (
                    <svg className="w-4 h-4 text-black" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                    </svg>
                  )}
                </button>
                <div>
                  <div className={`text-sm font-bold uppercase tracking-wider ${ex.completed ? "text-gray-500 line-through" : "text-white"}`}>
                    {ex.name}
                  </div>
                  <div className="text-xs text-gray-600 mt-0.5">{ex.sets} — {ex.reps}</div>
                </div>
              </div>
              {ex.duration !== "—" && (
                <span className="text-xs text-cyan-400 font-mono">{ex.duration}</span>
              )}
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}
