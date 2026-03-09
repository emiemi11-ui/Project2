import { useMemo, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getPersonById, acwrData } from "../../data/mockData";
import {
  ResponsiveContainer, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar,
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, BarChart, Bar,
} from "recharts";
import { AnimatedSection, StaggerContainer, StaggerItem, AnimatedNumber } from "../../components/AnimatedSection";
import { motion } from "framer-motion";

function getAcwrZone(acwr) {
  if (acwr < 0.8) return { label: "Under-trained", color: "#3b82f6", bg: "bg-blue-500/15", text: "text-blue-400" };
  if (acwr <= 1.3) return { label: "Optimal", color: "#22c55e", bg: "bg-emerald-500/15", text: "text-emerald-400" };
  if (acwr <= 1.5) return { label: "Warning", color: "#f59e0b", bg: "bg-amber-500/15", text: "text-amber-400" };
  return { label: "Danger", color: "#ef4444", bg: "bg-red-500/15", text: "text-red-400" };
}

function AcwrGauge({ value }) {
  const zone = getAcwrZone(value);
  const pct = Math.min(100, Math.max(0, ((value - 0.4) / 1.6) * 100));
  return (
    <div className="relative w-full h-8 rounded-full overflow-hidden bg-gray-800/50">
      <div className="absolute inset-0 flex">
        <div className="h-full bg-blue-500/20" style={{ width: "25%" }} />
        <div className="h-full bg-emerald-500/20" style={{ width: "31.25%" }} />
        <div className="h-full bg-amber-500/20" style={{ width: "12.5%" }} />
        <div className="h-full bg-red-500/20" style={{ width: "31.25%" }} />
      </div>
      <motion.div
        initial={{ left: 0 }}
        animate={{ left: `${pct}%` }}
        transition={{ duration: 1.2, ease: "easeOut" }}
        className="absolute top-0 bottom-0 w-1 rounded-full"
        style={{ backgroundColor: zone.color }}
      />
      <motion.div
        initial={{ left: 0 }}
        animate={{ left: `calc(${pct}% - 12px)` }}
        transition={{ duration: 1.2, ease: "easeOut" }}
        className="absolute -top-7 text-xs font-bold px-1.5 py-0.5 rounded"
        style={{ color: zone.color }}
      >
        {value}
      </motion.div>
    </div>
  );
}

export default function TrainerAthleteDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const person = getPersonById(id);
  const [activeTab, setActiveTab] = useState("overview");

  const athleteAcwr = useMemo(() => {
    if (!person) return null;
    return acwrData.find((a) => a.personId === person.id);
  }, [person]);

  const acwrValue = athleteAcwr ? athleteAcwr.ratio : 1.05;
  const acwrZone = getAcwrZone(acwrValue);

  const radarData = useMemo(() => {
    if (!person) return [];
    return [
      { subject: "Readiness", value: person.metrics.readinessScore },
      { subject: "HRV", value: person.metrics.hrvScore },
      { subject: "Sleep", value: person.metrics.sleepScore },
      { subject: "Stress Inv.", value: 100 - person.metrics.stressScore },
      { subject: "Cognitive", value: person.metrics.cognitiveScore },
    ];
  }, [person]);

  const trendData = useMemo(() => {
    if (!person) return [];
    return person.history.readiness.map((entry, i) => ({
      date: entry.date.slice(5),
      readiness: entry.value,
      hrv: person.history.hrv[i]?.value || 0,
    }));
  }, [person]);

  const correlations = useMemo(() => {
    if (!person) return [];
    const sleep = person.history.sleep.map((h) => h.value);
    const readiness = person.history.readiness.map((h) => h.value);
    const avgSleep = sleep.reduce((a, b) => a + b, 0) / sleep.length;
    const avgReady = readiness.reduce((a, b) => a + b, 0) / readiness.length;
    let num = 0, denA = 0, denB = 0;
    for (let i = 0; i < sleep.length; i++) {
      num += (sleep[i] - avgSleep) * (readiness[i] - avgReady);
      denA += (sleep[i] - avgSleep) ** 2;
      denB += (readiness[i] - avgReady) ** 2;
    }
    const r = denA && denB ? (num / Math.sqrt(denA * denB)).toFixed(2) : "N/A";
    return [
      { label: "Sleep → Readiness", value: r, description: `Sleep quality correlates with readiness at r=${r}` },
      { label: "Stress → HRV", value: person.metrics.stressScore > 50 ? "-0.71" : "-0.45", description: "Higher stress linked with lower HRV" },
    ];
  }, [person]);

  const program = [
    { exercise: "Barbell Squat", sets: "4×8", load: "80kg", status: "done" },
    { exercise: "Deadlift", sets: "3×5", load: "100kg", status: "done" },
    { exercise: "Pull-ups", sets: "3×12", load: "BW", status: "pending" },
    { exercise: "Plank Hold", sets: "3×60s", load: "—", status: "pending" },
    { exercise: "400m Sprints", sets: "6×", load: "—", status: "pending" },
  ];

  if (!person) {
    return (
      <div className="flex-1 flex items-center justify-center p-6">
        <div className="text-center">
          <div className="text-4xl mb-4">🔍</div>
          <p className="text-gray-400 mb-4">Athlete not found</p>
          <button onClick={() => navigate("/app/team")} className="text-orange-400 hover:underline text-sm">Back to Team</button>
        </div>
      </div>
    );
  }

  return (
    <div className="p-4 sm:p-6 overflow-auto">
      {/* Back button */}
      <AnimatedSection>
        <button onClick={() => navigate("/app/team")} className="flex items-center gap-2 text-gray-400 hover:text-orange-400 transition-colors mb-6 text-sm">
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" /></svg>
          Back to Team
        </button>
      </AnimatedSection>

      {/* Header */}
      <AnimatedSection delay={0.05}>
        <div className="flex flex-wrap items-center gap-4 mb-6">
          <div className="w-14 h-14 sm:w-16 sm:h-16 rounded-full bg-gradient-to-br from-orange-500/30 to-cyan-500/30 border-2 border-orange-500/50 flex items-center justify-center text-lg sm:text-xl font-bold text-orange-400">
            {person.name.split(" ").map((n) => n[0]).join("")}
          </div>
          <div>
            <h1 className="text-xl sm:text-2xl font-bold">{person.name}</h1>
            <p className="text-xs sm:text-sm text-gray-400">{person.unit} — Age {person.age}</p>
          </div>
          <div className="ml-auto flex items-center gap-3">
            <span className={`px-3 py-1 rounded-full text-xs sm:text-sm font-medium ${acwrZone.bg} ${acwrZone.text}`}>
              ACWR: {acwrValue} — {acwrZone.label}
            </span>
          </div>
        </div>
      </AnimatedSection>

      {/* ACWR Gauge */}
      <AnimatedSection delay={0.1}>
        <div className="rounded-xl bg-[#0d1117] border border-gray-800/50 p-4 sm:p-6 mb-6">
          <h2 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-6">ACWR Gauge</h2>
          <AcwrGauge value={acwrValue} />
          <div className="flex items-center justify-between mt-3 text-[10px] text-gray-500">
            <span>Under ({"<"}0.8)</span>
            <span>Optimal (0.8-1.3)</span>
            <span>Warning (1.3-1.5)</span>
            <span>Danger ({">"}1.5)</span>
          </div>
        </div>
      </AnimatedSection>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Radar Chart */}
        <AnimatedSection delay={0.15}>
          <div className="rounded-xl bg-[#0d1117] border border-gray-800/50 p-4 sm:p-6">
            <h2 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">Performance Profile</h2>
            <div className="h-64 sm:h-72">
              <ResponsiveContainer width="100%" height="100%">
                <RadarChart data={radarData}>
                  <PolarGrid stroke="#1f2937" />
                  <PolarAngleAxis dataKey="subject" tick={{ fill: "#9ca3af", fontSize: 11 }} />
                  <PolarRadiusAxis angle={30} domain={[0, 100]} tick={false} axisLine={false} />
                  <Radar name="Score" dataKey="value" stroke="#f97316" fill="#f97316" fillOpacity={0.2} strokeWidth={2} />
                </RadarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </AnimatedSection>

        {/* Trend Chart with dual axis */}
        <AnimatedSection delay={0.2}>
          <div className="rounded-xl bg-[#0d1117] border border-gray-800/50 p-4 sm:p-6">
            <h2 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">Readiness & HRV Trend</h2>
            <div className="h-64 sm:h-72">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={trendData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
                  <XAxis dataKey="date" tick={{ fill: "#6b7280", fontSize: 10 }} />
                  <YAxis domain={[0, 100]} tick={{ fill: "#6b7280", fontSize: 10 }} />
                  <Tooltip contentStyle={{ background: "#0d1117", border: "1px solid #1f2937", borderRadius: 8 }} />
                  <Line type="monotone" dataKey="readiness" stroke="#f97316" strokeWidth={2} dot={false} name="Readiness" />
                  <Line type="monotone" dataKey="hrv" stroke="#06b6d4" strokeWidth={2} dot={false} name="HRV" />
                </LineChart>
              </ResponsiveContainer>
            </div>
            <div className="flex items-center gap-4 mt-3 text-xs text-gray-500">
              <span className="flex items-center gap-1"><span className="w-3 h-0.5 bg-orange-500 inline-block" /> Readiness</span>
              <span className="flex items-center gap-1"><span className="w-3 h-0.5 bg-cyan-500 inline-block" /> HRV</span>
            </div>
          </div>
        </AnimatedSection>

        {/* Score Cards */}
        <AnimatedSection delay={0.25}>
          <div className="rounded-xl bg-[#0d1117] border border-gray-800/50 p-4 sm:p-6">
            <h2 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">Current Scores</h2>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
              {[
                { label: "Readiness", value: person.metrics.readinessScore, color: "text-orange-400" },
                { label: "HRV", value: person.metrics.hrvScore, color: "text-cyan-400" },
                { label: "Sleep", value: person.metrics.sleepScore, color: "text-blue-400" },
                { label: "Stress", value: person.metrics.stressScore, color: "text-red-400" },
                { label: "Cognitive", value: person.metrics.cognitiveScore, color: "text-purple-400" },
                { label: "Steps", value: person.metrics.steps.toLocaleString(), color: "text-emerald-400" },
              ].map((item) => (
                <motion.div
                  key={item.label}
                  whileHover={{ scale: 1.03 }}
                  className="bg-[#0a0f14] rounded-lg p-3 sm:p-4 border border-gray-800/30 transition-all hover:border-orange-500/20"
                >
                  <div className={`text-xl sm:text-2xl font-bold ${item.color}`}>{item.value}</div>
                  <div className="text-[10px] sm:text-xs text-gray-500 uppercase tracking-wider mt-1">{item.label}</div>
                </motion.div>
              ))}
            </div>
          </div>
        </AnimatedSection>

        {/* Training Program */}
        <AnimatedSection delay={0.3}>
          <div className="rounded-xl bg-[#0d1117] border border-gray-800/50 p-4 sm:p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-sm font-semibold text-gray-400 uppercase tracking-wider">Current Program</h2>
              <button className="text-xs text-orange-400 hover:underline px-3 py-1 rounded-lg bg-orange-500/10 hover:bg-orange-500/20 transition-colors">
                Adjust Intensity
              </button>
            </div>
            <div className="mb-3">
              <div className="flex items-center justify-between text-xs text-gray-500 mb-1">
                <span>Progress</span>
                <span>{program.filter((e) => e.status === "done").length}/{program.length}</span>
              </div>
              <div className="h-2 bg-gray-800 rounded-full overflow-hidden">
                <motion.div
                  initial={{ width: 0 }}
                  animate={{ width: `${(program.filter((e) => e.status === "done").length / program.length) * 100}%` }}
                  transition={{ duration: 1, delay: 0.5 }}
                  className="h-full bg-gradient-to-r from-orange-500 to-cyan-500 rounded-full"
                />
              </div>
            </div>
            <div className="space-y-2">
              {program.map((ex) => (
                <motion.div
                  key={ex.exercise}
                  whileHover={{ x: 4 }}
                  className="flex items-center justify-between p-3 rounded-lg bg-[#0a0f14] border border-gray-800/30 hover:border-orange-500/20 transition-colors"
                >
                  <div className="flex items-center gap-3">
                    <div className={`w-2 h-2 rounded-full ${ex.status === "done" ? "bg-emerald-400" : "bg-gray-600"}`} />
                    <span className={`text-sm font-medium ${ex.status === "done" ? "line-through text-gray-500" : "text-white"}`}>{ex.exercise}</span>
                  </div>
                  <div className="flex items-center gap-4 text-xs text-gray-500">
                    <span>{ex.sets}</span>
                    <span>{ex.load}</span>
                  </div>
                </motion.div>
              ))}
            </div>
          </div>
        </AnimatedSection>
      </div>

      {/* Correlations */}
      <AnimatedSection delay={0.35} className="mt-6">
        <div className="rounded-xl bg-[#0d1117] border border-gray-800/50 p-4 sm:p-6">
          <h2 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">Correlations</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {correlations.map((c) => (
              <div key={c.label} className="bg-[#0a0f14] rounded-lg p-4 border border-gray-800/30">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs text-gray-500 uppercase tracking-wider">{c.label}</span>
                  <span className="text-sm font-bold text-orange-400">r = {c.value}</span>
                </div>
                <p className="text-xs text-gray-400">{c.description}</p>
              </div>
            ))}
          </div>
        </div>
      </AnimatedSection>
    </div>
  );
}
