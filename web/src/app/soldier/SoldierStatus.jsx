import { useMemo } from "react";
import { useAuth } from "../../hooks/useAuth";
import { getPersonById, persons } from "../../data/mockData";
import { ResponsiveContainer, RadialBarChart, RadialBar, LineChart, Line, XAxis, Tooltip, BarChart, Bar, YAxis } from "recharts";
import { AnimatedSection, StaggerContainer, StaggerItem, AnimatedNumber } from "../../components/AnimatedSection";
import { motion } from "framer-motion";

function GaugeCard({ label, value, max, color, unit }) {
  const data = [{ value, fill: color }];
  return (
    <motion.div
      whileHover={{ scale: 1.02, y: -2 }}
      className="bg-[#0d1117] rounded-xl border border-gray-800/50 p-4 sm:p-6 text-center hover:border-[#84cc16]/20 transition-all"
    >
      <div className="w-28 h-28 sm:w-32 sm:h-32 mx-auto">
        <ResponsiveContainer width="100%" height="100%">
          <RadialBarChart cx="50%" cy="50%" innerRadius="70%" outerRadius="100%" startAngle={90} endAngle={-270} data={data}>
            <RadialBar background={{ fill: "#1f2937" }} dataKey="value" cornerRadius={10} max={max} />
          </RadialBarChart>
        </ResponsiveContainer>
      </div>
      <div className="mt-2">
        <span className="text-2xl sm:text-3xl font-bold font-mono" style={{ color }}><AnimatedNumber value={value} /></span>
        {unit && <span className="text-xs sm:text-sm text-gray-500 ml-1">{unit}</span>}
      </div>
      <div className="text-[10px] sm:text-xs text-gray-500 uppercase tracking-widest mt-1 font-mono">{label}</div>
    </motion.div>
  );
}

function Sparkline({ data, color }) {
  const chartData = data.map((d) => ({ v: d.value }));
  return (
    <div className="h-6 w-20">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={chartData}>
          <Line type="monotone" dataKey="v" stroke={color} strokeWidth={1.5} dot={false} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

export default function SoldierStatus() {
  const { user } = useAuth();

  const person = useMemo(() => {
    return user?.personId ? getPersonById(user.personId) : null;
  }, [user]);

  const metrics = person?.metrics || { readinessScore: 82, hrvScore: 71, sleepScore: 78, stressScore: 28, steps: 8450, cognitiveScore: 75 };
  const history = person?.history;

  const nextTraining = {
    time: "0600",
    type: "PT SESSION",
    exercises: ["3 Mile Run", "Push-ups 3x40", "Pull-ups 3x15", "Plank 3x90s"],
  };

  const unitSoldiers = useMemo(() => {
    const unit = person?.unit || "Alpha Company";
    return persons
      .filter((p) => p.unit === unit && p.role === "soldier")
      .sort((a, b) => b.metrics.readinessScore - a.metrics.readinessScore);
  }, [person]);

  const rank = useMemo(() => {
    if (!person) return { pos: 3, total: 22 };
    const idx = unitSoldiers.findIndex((s) => s.id === person.id);
    return { pos: idx >= 0 ? idx + 1 : 3, total: unitSoldiers.length || 22 };
  }, [person, unitSoldiers]);

  const rankChartData = unitSoldiers.slice(0, 8).map((s) => ({
    name: s.name.split(" ")[1]?.[0] + "." || s.name[0],
    readiness: s.metrics.readinessScore,
    fill: s.id === person?.id ? "#84cc16" : "#374151",
  }));

  return (
    <main className="max-w-5xl mx-auto p-4 sm:p-6">
      {/* Identity */}
      <AnimatedSection>
        <div className="text-center mb-6 sm:mb-8">
          <div className="text-[10px] sm:text-xs text-gray-600 uppercase tracking-[0.2em]">{person?.unit || "ALPHA COMPANY"}</div>
          <h1 className="text-lg sm:text-xl font-bold uppercase tracking-wider mt-1">{user?.displayName || "CPL. FOSTER"}</h1>
        </div>
      </AnimatedSection>

      {/* Main Readiness Score */}
      <AnimatedSection delay={0.1}>
        <div className="text-center mb-8 sm:mb-10">
          <div className="text-[10px] sm:text-xs text-gray-600 uppercase tracking-[0.3em] mb-2">OPERATIONAL READINESS</div>
          <motion.div
            initial={{ scale: 0.5, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ duration: 0.8, ease: "easeOut" }}
          >
            <div className="text-6xl sm:text-7xl font-bold text-[#84cc16]">
              <AnimatedNumber value={metrics.readinessScore} duration={2} />
            </div>
          </motion.div>
          <div className="text-xs sm:text-sm text-gray-500 mt-1 uppercase tracking-wider">
            {metrics.readinessScore >= 80 ? "COMBAT READY" : metrics.readinessScore >= 60 ? "OPERATIONAL" : "RESTRICTED DUTY"}
          </div>
          {/* Status indicator */}
          <div className="flex items-center justify-center gap-2 mt-3">
            <div className={`w-2 h-2 rounded-full ${metrics.readinessScore >= 80 ? "bg-[#84cc16] animate-pulse" : metrics.readinessScore >= 60 ? "bg-amber-400" : "bg-red-400 animate-pulse"}`} />
            <span className="text-[10px] text-gray-500 uppercase tracking-wider">
              {metrics.readinessScore >= 80 ? "ALL SYSTEMS GREEN" : metrics.readinessScore >= 60 ? "MONITOR" : "ALERT"}
            </span>
          </div>
        </div>
      </AnimatedSection>

      {/* Gauges */}
      <StaggerContainer className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6 sm:mb-8">
        <StaggerItem>
          <GaugeCard label="READINESS" value={metrics.readinessScore} max={100} color="#84cc16" />
          {history && <div className="mt-2 flex justify-center"><Sparkline data={history.readiness} color="#84cc16" /></div>}
        </StaggerItem>
        <StaggerItem>
          <GaugeCard label="HRV" value={metrics.hrvScore} max={100} color="#00d4ff" unit="ms" />
          {history && <div className="mt-2 flex justify-center"><Sparkline data={history.hrv} color="#00d4ff" /></div>}
        </StaggerItem>
        <StaggerItem>
          <GaugeCard label="SLEEP QUALITY" value={metrics.sleepScore} max={100} color="#818cf8" />
          {history && <div className="mt-2 flex justify-center"><Sparkline data={history.sleep} color="#818cf8" /></div>}
        </StaggerItem>
      </StaggerContainer>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6 sm:mb-8">
        {/* Stats */}
        <AnimatedSection delay={0.3}>
          <div className="bg-[#0d1117] rounded-xl border border-gray-800/50 p-4 sm:p-6">
            <h2 className="text-[10px] sm:text-xs text-gray-600 uppercase tracking-[0.2em] mb-4">TODAY'S METRICS</h2>
            <div className="space-y-3">
              {[
                { label: "STEPS", value: metrics.steps.toLocaleString(), color: "text-[#84cc16]" },
                { label: "STRESS LEVEL", value: `${metrics.stressScore}%`, color: metrics.stressScore < 40 ? "text-emerald-400" : "text-amber-400" },
                { label: "COGNITIVE", value: `${metrics.cognitiveScore}%`, color: "text-purple-400" },
                { label: "SCREEN TIME", value: `${Math.round(metrics.screenTime / 60)}h ${metrics.screenTime % 60}m`, color: "text-cyan-400" },
              ].map((stat) => (
                <motion.div
                  key={stat.label}
                  whileHover={{ x: 4 }}
                  className="flex items-center justify-between py-2 border-b border-gray-800/30"
                >
                  <span className="text-[10px] sm:text-xs text-gray-500 uppercase tracking-wider">{stat.label}</span>
                  <span className={`text-base sm:text-lg font-bold font-mono ${stat.color}`}>{stat.value}</span>
                </motion.div>
              ))}
            </div>
          </div>
        </AnimatedSection>

        {/* Next Training */}
        <AnimatedSection delay={0.35}>
          <div className="bg-[#0d1117] rounded-xl border border-gray-800/50 p-4 sm:p-6">
            <h2 className="text-[10px] sm:text-xs text-gray-600 uppercase tracking-[0.2em] mb-4">NEXT TRAINING</h2>
            <div className="text-center mb-4">
              <motion.div
                initial={{ scale: 0.8 }}
                animate={{ scale: 1 }}
                transition={{ duration: 0.5 }}
                className="text-3xl sm:text-4xl font-bold text-cyan-400 font-mono"
              >
                {nextTraining.time}
              </motion.div>
              <div className="text-xs sm:text-sm text-gray-400 uppercase tracking-wider mt-1">{nextTraining.type}</div>
            </div>
            <div className="space-y-2">
              {nextTraining.exercises.map((ex, i) => (
                <motion.div
                  key={ex}
                  initial={{ opacity: 0, x: -10 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 0.5 + i * 0.1 }}
                  className="flex items-center gap-2 text-xs sm:text-sm text-gray-400"
                >
                  <div className="w-1.5 h-1.5 rounded-full bg-[#84cc16]" />
                  <span className="uppercase tracking-wider font-mono">{ex}</span>
                </motion.div>
              ))}
            </div>
          </div>
        </AnimatedSection>
      </div>

      {/* Unit Rank */}
      <AnimatedSection delay={0.4}>
        <div className="bg-[#0d1117] rounded-xl border border-gray-800/50 p-4 sm:p-6">
          <h2 className="text-[10px] sm:text-xs text-gray-600 uppercase tracking-[0.2em] mb-4 text-center">UNIT RANKING</h2>
          <div className="text-center mb-4">
            <div className="text-2xl sm:text-3xl font-bold font-mono">
              <span className="text-[#84cc16]"><AnimatedNumber value={rank.pos} /></span>
              <span className="text-gray-600">/{rank.total}</span>
            </div>
            <div className="text-[10px] sm:text-xs text-gray-500 uppercase tracking-wider mt-1">IN {(person?.unit || "ALPHA COMPANY").toUpperCase()}</div>
          </div>
          {rankChartData.length > 0 && (
            <div className="h-28 sm:h-36">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={rankChartData}>
                  <XAxis dataKey="name" tick={{ fill: "#6b7280", fontSize: 10 }} />
                  <YAxis domain={[0, 100]} tick={false} width={0} />
                  <Tooltip contentStyle={{ background: "#0d1117", border: "1px solid #1f2937", borderRadius: 8, fontFamily: "monospace" }} />
                  <Bar dataKey="readiness" radius={[3, 3, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>
      </AnimatedSection>
    </main>
  );
}
