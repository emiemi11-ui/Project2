import { useMemo } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getPersonById } from "../../data/mockData";
import {
  ResponsiveContainer, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar,
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
} from "recharts";

function getAcwrZone(acwr) {
  if (acwr < 0.8) return { label: "Under-trained", color: "#3b82f6", bg: "bg-blue-500/15" };
  if (acwr <= 1.3) return { label: "Optimal", color: "#22c55e", bg: "bg-emerald-500/15" };
  if (acwr <= 1.5) return { label: "Warning", color: "#f59e0b", bg: "bg-amber-500/15" };
  return { label: "Danger", color: "#ef4444", bg: "bg-red-500/15" };
}

export default function TrainerAthleteDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const person = getPersonById(id);

  const radarData = useMemo(() => {
    if (!person) return [];
    return [
      { subject: "Readiness", value: person.scores.readiness },
      { subject: "HRV", value: person.scores.hrv },
      { subject: "Sleep", value: person.scores.sleep },
      { subject: "Stress", value: 100 - person.scores.stress },
      { subject: "Fitness", value: Math.min(100, Math.round(person.steps.current / 150)) },
    ];
  }, [person]);

  const trendData = useMemo(() => {
    if (!person) return [];
    return person.history.readiness.slice(-30).map((entry) => ({
      date: entry.date.slice(5),
      readiness: entry.value,
    }));
  }, [person]);

  const acwr = useMemo(() => (0.7 + Math.random() * 0.9).toFixed(2), []);
  const acwrZone = getAcwrZone(parseFloat(acwr));

  const program = [
    { exercise: "Barbell Squat", sets: "4×8", load: "80kg", status: "done" },
    { exercise: "Deadlift", sets: "3×5", load: "100kg", status: "done" },
    { exercise: "Pull-ups", sets: "3×12", load: "BW", status: "pending" },
    { exercise: "Plank Hold", sets: "3×60s", load: "—", status: "pending" },
    { exercise: "400m Sprints", sets: "6×", load: "—", status: "pending" },
  ];

  if (!person) {
    return (
      <div className="min-h-screen bg-[#0a0f14] text-white flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-400 mb-4">Athlete not found</p>
          <button onClick={() => navigate("/app/team")} className="text-orange-400 hover:underline">Back to Team</button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#0a0f14] text-white p-6">
      {/* Back button */}
      <button onClick={() => navigate("/app/team")} className="flex items-center gap-2 text-gray-400 hover:text-orange-400 transition-colors mb-6 text-sm">
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" /></svg>
        Back to Team
      </button>

      {/* Header */}
      <div className="flex items-center gap-4 mb-8">
        <div className="w-16 h-16 rounded-full bg-gradient-to-br from-orange-500/30 to-cyan-500/30 border-2 border-orange-500/50 flex items-center justify-center text-xl font-bold text-orange-400">
          {person.name.split(" ").map((n) => n[0]).join("")}
        </div>
        <div>
          <h1 className="text-2xl font-bold">{person.name}</h1>
          <p className="text-sm text-gray-400">{person.unit} — Age {person.age}</p>
        </div>
        <span className={`ml-auto px-3 py-1 rounded-full text-sm font-medium ${acwrZone.bg}`} style={{ color: acwrZone.color }}>
          ACWR: {acwr} — {acwrZone.label}
        </span>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Radar Chart */}
        <div className="rounded-xl bg-[#0d1117] border border-gray-800/50 p-6">
          <h2 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">Performance Profile</h2>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <RadarChart data={radarData}>
                <PolarGrid stroke="#1f2937" />
                <PolarAngleAxis dataKey="subject" tick={{ fill: "#9ca3af", fontSize: 12 }} />
                <PolarRadiusAxis angle={30} domain={[0, 100]} tick={false} axisLine={false} />
                <Radar name="Score" dataKey="value" stroke="#f97316" fill="#f97316" fillOpacity={0.2} strokeWidth={2} />
              </RadarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Vital Signs */}
        <div className="rounded-xl bg-[#0d1117] border border-gray-800/50 p-6">
          <h2 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">Readiness Trend (30 days)</h2>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={trendData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
                <XAxis dataKey="date" tick={{ fill: "#6b7280", fontSize: 10 }} interval={4} />
                <YAxis domain={[0, 100]} tick={{ fill: "#6b7280", fontSize: 10 }} />
                <Tooltip contentStyle={{ background: "#0d1117", border: "1px solid #1f2937", borderRadius: 8 }} />
                <Line type="monotone" dataKey="readiness" stroke="#f97316" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Score Cards */}
        <div className="rounded-xl bg-[#0d1117] border border-gray-800/50 p-6">
          <h2 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">Current Scores</h2>
          <div className="grid grid-cols-2 gap-4">
            {[
              { label: "Readiness", value: person.scores.readiness, color: "text-orange-400" },
              { label: "HRV", value: person.scores.hrv, color: "text-cyan-400" },
              { label: "Sleep Quality", value: person.scores.sleep, color: "text-blue-400" },
              { label: "Stress", value: person.scores.stress, color: "text-red-400" },
              { label: "Cognitive", value: person.scores.cognitive, color: "text-purple-400" },
              { label: "Steps Today", value: person.steps.current.toLocaleString(), color: "text-emerald-400" },
            ].map((item) => (
              <div key={item.label} className="bg-[#0a0f14] rounded-lg p-4 border border-gray-800/30">
                <div className={`text-2xl font-bold ${item.color}`}>{item.value}</div>
                <div className="text-xs text-gray-500 uppercase tracking-wider mt-1">{item.label}</div>
              </div>
            ))}
          </div>
        </div>

        {/* Training Program */}
        <div className="rounded-xl bg-[#0d1117] border border-gray-800/50 p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-sm font-semibold text-gray-400 uppercase tracking-wider">Current Program</h2>
            <button className="text-xs text-orange-400 hover:underline">Adjust Program</button>
          </div>
          <div className="space-y-2">
            {program.map((ex) => (
              <div key={ex.exercise} className="flex items-center justify-between p-3 rounded-lg bg-[#0a0f14] border border-gray-800/30">
                <div className="flex items-center gap-3">
                  <div className={`w-2 h-2 rounded-full ${ex.status === "done" ? "bg-emerald-400" : "bg-gray-600"}`} />
                  <span className="text-sm font-medium">{ex.exercise}</span>
                </div>
                <div className="flex items-center gap-4 text-xs text-gray-500">
                  <span>{ex.sets}</span>
                  <span>{ex.load}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
