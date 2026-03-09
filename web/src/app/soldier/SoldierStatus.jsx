import { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { getPersonById } from "../../data/mockData";
import { ResponsiveContainer, RadialBarChart, RadialBar } from "recharts";

function GaugeCard({ label, value, max, color, unit }) {
  const data = [{ value, fill: color }];
  return (
    <div className="bg-[#0d1117] rounded-xl border border-gray-800/50 p-6 text-center">
      <div className="w-32 h-32 mx-auto">
        <ResponsiveContainer width="100%" height="100%">
          <RadialBarChart cx="50%" cy="50%" innerRadius="70%" outerRadius="100%" startAngle={90} endAngle={-270} data={data}>
            <RadialBar background={{ fill: "#1f2937" }} dataKey="value" cornerRadius={10} max={max} />
          </RadialBarChart>
        </ResponsiveContainer>
      </div>
      <div className="mt-2">
        <span className="text-3xl font-bold font-mono" style={{ color }}>{value}</span>
        {unit && <span className="text-sm text-gray-500 ml-1">{unit}</span>}
      </div>
      <div className="text-xs text-gray-500 uppercase tracking-widest mt-1 font-mono">{label}</div>
    </div>
  );
}

export default function SoldierStatus() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const person = useMemo(() => {
    return user?.personId ? getPersonById(user.personId) : null;
  }, [user]);

  const scores = person?.scores || { readiness: 82, hrv: 71, sleep: 78, stress: 28 };
  const steps = person?.steps?.current || 8450;

  const nextTraining = {
    time: "0600",
    type: "PT SESSION",
    exercises: ["3 Mile Run", "Push-ups 3x40", "Pull-ups 3x15", "Plank 3x90s"],
  };

  return (
    <div className="min-h-screen bg-[#0a0d0f] text-white font-mono">
      {/* Top Bar */}
      <nav className="bg-[#0d1117] border-b border-gray-800/50 px-6 py-3">
        <div className="max-w-5xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded bg-olive-500 bg-gradient-to-br from-lime-500 to-cyan-500 flex items-center justify-center font-bold text-black text-sm">V</div>
            <span className="font-bold text-lg tracking-tight uppercase">VitaNova</span>
          </div>
          <div className="flex items-center gap-4">
            {[
              { label: "STATUS", path: "/app/my-readiness", active: true },
              { label: "TRAINING", path: "/app/my-training" },
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

      <main className="max-w-5xl mx-auto p-6">
        {/* Identity */}
        <div className="text-center mb-8">
          <div className="text-xs text-gray-600 uppercase tracking-[0.2em]">{person?.unit || "Alpha Company"}</div>
          <h1 className="text-xl font-bold uppercase tracking-wider mt-1">{user?.displayName || "CPL. FOSTER"}</h1>
        </div>

        {/* Main Readiness Score */}
        <div className="text-center mb-10">
          <div className="text-xs text-gray-600 uppercase tracking-[0.3em] mb-2">OPERATIONAL READINESS</div>
          <div className="text-7xl font-bold text-lime-400">{scores.readiness}</div>
          <div className="text-sm text-gray-500 mt-1">
            {scores.readiness >= 80 ? "COMBAT READY" : scores.readiness >= 60 ? "OPERATIONAL" : "RESTRICTED DUTY"}
          </div>
        </div>

        {/* Gauges */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
          <GaugeCard label="READINESS" value={scores.readiness} max={100} color="#84cc16" />
          <GaugeCard label="HRV" value={scores.hrv} max={100} color="#00d4ff" unit="ms" />
          <GaugeCard label="SLEEP QUALITY" value={scores.sleep} max={100} color="#818cf8" />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-8">
          {/* Stats */}
          <div className="bg-[#0d1117] rounded-xl border border-gray-800/50 p-6">
            <h2 className="text-xs text-gray-600 uppercase tracking-[0.2em] mb-4">TODAY'S METRICS</h2>
            <div className="space-y-3">
              {[
                { label: "STEPS", value: steps.toLocaleString(), color: "text-lime-400" },
                { label: "STRESS LEVEL", value: `${scores.stress}%`, color: scores.stress < 40 ? "text-emerald-400" : "text-amber-400" },
                { label: "CALORIES", value: "2,340", color: "text-orange-400" },
                { label: "ACTIVE HOURS", value: "4.5h", color: "text-cyan-400" },
              ].map((stat) => (
                <div key={stat.label} className="flex items-center justify-between py-2 border-b border-gray-800/30">
                  <span className="text-xs text-gray-500 uppercase tracking-wider">{stat.label}</span>
                  <span className={`text-lg font-bold ${stat.color}`}>{stat.value}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Next Training */}
          <div className="bg-[#0d1117] rounded-xl border border-gray-800/50 p-6">
            <h2 className="text-xs text-gray-600 uppercase tracking-[0.2em] mb-4">NEXT TRAINING</h2>
            <div className="text-center mb-4">
              <div className="text-4xl font-bold text-cyan-400">{nextTraining.time}</div>
              <div className="text-sm text-gray-400 uppercase tracking-wider mt-1">{nextTraining.type}</div>
            </div>
            <div className="space-y-2">
              {nextTraining.exercises.map((ex) => (
                <div key={ex} className="flex items-center gap-2 text-sm text-gray-400">
                  <div className="w-1.5 h-1.5 rounded-full bg-lime-500" />
                  {ex}
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Unit Rank */}
        <div className="bg-[#0d1117] rounded-xl border border-gray-800/50 p-6 text-center">
          <h2 className="text-xs text-gray-600 uppercase tracking-[0.2em] mb-3">UNIT RANKING</h2>
          <div className="text-2xl font-bold">
            <span className="text-lime-400">3</span>
            <span className="text-gray-600">/22</span>
          </div>
          <div className="text-xs text-gray-500 uppercase tracking-wider mt-1">IN {person?.unit?.toUpperCase() || "ALPHA COMPANY"}</div>
        </div>
      </main>
    </div>
  );
}
