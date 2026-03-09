import { useState, useMemo } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  ReferenceLine,
  ReferenceArea,
  Legend,
} from "recharts";
import { persons, alerts, appointments } from "../../data/mockData";
import { motion } from "framer-motion";
import { AnimatedSection } from "../../components/AnimatedSection";

/* ——————————————————————————————————————————————
   Helper: generate extended 30-day history
   —————————————————————————————————————————————— */
function seededRandom(seed) {
  let s = seed;
  return function next() {
    s = (s * 16807 + 0) % 2147483647;
    return (s - 1) / 2147483646;
  };
}

function clamp(val, min, max) {
  return Math.round(Math.min(max, Math.max(min, val)));
}

function generate30DayHistory(baseValue, variance, seed) {
  const rng = seededRandom(seed);
  const history = [];
  const today = new Date();
  for (let i = 29; i >= 0; i--) {
    const date = new Date(today);
    date.setDate(date.getDate() - i);
    const drift = (rng() - 0.5) * 2 * variance;
    history.push({
      date: date.toISOString().split("T")[0],
      value: clamp(baseValue + drift, 0, 200),
    });
  }
  return history;
}

function generateSleepNights(baseSleep, seed) {
  const rng = seededRandom(seed);
  const stages = ["Deep", "Light", "REM", "Awake"];
  const nights = [];
  const today = new Date();
  for (let i = 6; i >= 0; i--) {
    const date = new Date(today);
    date.setDate(date.getDate() - i);
    const totalHours = 5 + rng() * 4;
    const deepPct = 0.15 + rng() * 0.1;
    const remPct = 0.2 + rng() * 0.1;
    const awakePct = 0.02 + rng() * 0.08;
    const lightPct = 1 - deepPct - remPct - awakePct;
    nights.push({
      date: date.toISOString().split("T")[0],
      dateShort: date.toLocaleDateString("ro-RO", { weekday: "short", day: "numeric" }),
      total: parseFloat(totalHours.toFixed(1)),
      deep: parseFloat((totalHours * deepPct).toFixed(1)),
      light: parseFloat((totalHours * lightPct).toFixed(1)),
      rem: parseFloat((totalHours * remPct).toFixed(1)),
      awake: parseFloat((totalHours * awakePct).toFixed(1)),
      score: clamp(baseSleep + (rng() - 0.5) * 20, 10, 100),
    });
  }
  return nights;
}

/* ——————————————————————————————————————————————
   Mock Medications
   —————————————————————————————————————————————— */
const mockMedications = [
  { name: "Metoprolol", dose: "50 mg", frequency: "1x/zi", since: "2025-09-15", status: "activ" },
  { name: "Melatonină", dose: "3 mg", frequency: "seara", since: "2025-11-01", status: "activ" },
  { name: "Magneziu", dose: "400 mg", frequency: "1x/zi", since: "2025-10-20", status: "activ" },
  { name: "Ibuprofen", dose: "400 mg", frequency: "la nevoie", since: "2026-01-10", status: "la nevoie" },
];

/* ——————————————————————————————————————————————
   Mock Lab Results
   —————————————————————————————————————————————— */
const mockLabResults = [
  { test: "Hemoglobină", value: "14.2", unit: "g/dL", range: "13.0 - 17.5", date: "2026-02-15", status: "normal" },
  { test: "Glucoză", value: "98", unit: "mg/dL", range: "70 - 100", date: "2026-02-15", status: "normal" },
  { test: "Colesterol total", value: "215", unit: "mg/dL", range: "< 200", date: "2026-02-15", status: "ridicat" },
  { test: "TSH", value: "2.8", unit: "mUI/L", range: "0.4 - 4.0", date: "2026-02-15", status: "normal" },
  { test: "Vitamina D", value: "22", unit: "ng/mL", range: "30 - 100", date: "2026-02-15", status: "scăzut" },
  { test: "Cortizol (matinal)", value: "18.5", unit: "μg/dL", range: "6.2 - 19.4", date: "2026-01-20", status: "normal" },
  { test: "CRP", value: "0.8", unit: "mg/L", range: "< 3.0", date: "2026-01-20", status: "normal" },
  { test: "Fier seric", value: "65", unit: "μg/dL", range: "60 - 170", date: "2026-01-20", status: "normal" },
];

/* ——————————————————————————————————————————————
   Mock Doctor Notes / Annotations
   —————————————————————————————————————————————— */
function generateAnnotations(seed) {
  const rng = seededRandom(seed);
  const notes = [
    "Pacient raportat oboseală cronică",
    "Ajustat doza de Metoprolol",
    "Recomandat evaluare somn",
    "Rezultate lab revizuite - Vit D scăzut",
    "Monitorizare HRV intensificată",
  ];
  const annotations = [];
  const today = new Date();
  for (let i = 0; i < 3; i++) {
    const daysAgo = Math.floor(rng() * 25) + 2;
    const date = new Date(today);
    date.setDate(date.getDate() - daysAgo);
    annotations.push({
      date: date.toISOString().split("T")[0],
      note: notes[Math.floor(rng() * notes.length)],
    });
  }
  return annotations.sort((a, b) => a.date.localeCompare(b.date));
}

/* ——————————————————————————————————————————————
   Status helpers
   —————————————————————————————————————————————— */
const statusColors = {
  active: "bg-emerald-500",
  warning: "bg-amber-500",
  critical: "bg-red-500",
};

const statusLabels = {
  active: "Stabil",
  warning: "Atenție",
  critical: "Critic",
};

/* ——————————————————————————————————————————————
   Custom Tooltip
   —————————————————————————————————————————————— */
function ChartTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-white border border-gray-200 rounded-lg shadow-lg px-3 py-2 text-sm">
      <div className="font-medium text-[#1A1A2E] mb-1">{label}</div>
      {payload.map((entry, i) => (
        <div key={i} className="flex items-center gap-2">
          <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: entry.color }} />
          <span className="text-gray-600">{entry.name}:</span>
          <span className="font-semibold text-[#1A1A2E]">{entry.value}</span>
        </div>
      ))}
    </div>
  );
}

/* ——————————————————————————————————————————————
   PatientDetail Component
   —————————————————————————————————————————————— */
export default function PatientDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [recommendation, setRecommendation] = useState("");
  const [activeChart, setActiveChart] = useState("hr");

  const patient = useMemo(() => persons.find((p) => p.id === id), [id]);

  const patientAlerts = useMemo(() => {
    return alerts
      .filter((a) => a.personId === id)
      .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
  }, [id]);

  const patientAppointments = useMemo(() => {
    return appointments
      .filter((a) => a.patientId === id)
      .sort((a, b) => new Date(b.date) - new Date(a.date));
  }, [id]);

  // Generate 30-day histories
  const hrHistory = useMemo(() => {
    if (!patient) return [];
    const base = 60 + Math.round((patient.metrics.stressScore / 100) * 40);
    return generate30DayHistory(base, 8, patient.id.charCodeAt(8) * 7);
  }, [patient]);

  const hrvHistory = useMemo(() => {
    if (!patient) return [];
    return generate30DayHistory(patient.metrics.hrvScore, 12, patient.id.charCodeAt(8) * 13);
  }, [patient]);

  const sleepHistory = useMemo(() => {
    if (!patient) return [];
    return generate30DayHistory(patient.metrics.sleepScore, 10, patient.id.charCodeAt(8) * 19);
  }, [patient]);

  const stressHistory = useMemo(() => {
    if (!patient) return [];
    return generate30DayHistory(patient.metrics.stressScore, 8, patient.id.charCodeAt(8) * 23);
  }, [patient]);

  const sleepNights = useMemo(() => {
    if (!patient) return [];
    return generateSleepNights(patient.metrics.sleepScore, patient.id.charCodeAt(8) * 31);
  }, [patient]);

  const annotations = useMemo(() => {
    if (!patient) return [];
    return generateAnnotations(patient.id.charCodeAt(8) * 41);
  }, [patient]);

  // Combine chart data with annotations
  const chartData = useMemo(() => {
    const source =
      activeChart === "hr" ? hrHistory :
      activeChart === "hrv" ? hrvHistory :
      activeChart === "sleep" ? sleepHistory :
      stressHistory;

    return source.map((entry) => {
      const annotation = annotations.find((a) => a.date === entry.date);
      return {
        ...entry,
        dateShort: new Date(entry.date).toLocaleDateString("ro-RO", { day: "numeric", month: "short" }),
        annotation: annotation ? annotation.note : undefined,
      };
    });
  }, [activeChart, hrHistory, hrvHistory, sleepHistory, stressHistory, annotations]);

  const chartConfig = {
    hr: { label: "Heart Rate (bpm)", color: "#E11D48", normalMin: 55, normalMax: 85 },
    hrv: { label: "HRV (ms)", color: "#2563EB", normalMin: 50, normalMax: 90 },
    sleep: { label: "Sleep Score", color: "#6366F1", normalMin: 60, normalMax: 95 },
    stress: { label: "Nivel Stres", color: "#F59E0B", normalMin: 10, normalMax: 45 },
  };

  const currentConfig = chartConfig[activeChart];

  if (!patient) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-[#F8F9FA]">
        <div className="text-center">
          <p className="text-gray-500 text-lg">Pacientul nu a fost găsit.</p>
          <button
            onClick={() => navigate(-1)}
            className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700"
          >
            Înapoi
          </button>
        </div>
      </div>
    );
  }

  const mockWeight = 70 + (patient.id.charCodeAt(8) % 30);
  const mockHeight = 165 + (patient.id.charCodeAt(7) % 25);
  const mockConditions = patient.status === "critical"
    ? ["Hipertensiune", "Insomnie cronică", "Anxietate"]
    : patient.status === "warning"
    ? ["Deficit Vitamina D", "Stres moderat"]
    : ["Fără condiții semnificative"];

  return (
    <div className="min-h-screen bg-[#F8F9FA]">
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="max-w-7xl mx-auto px-6 py-6 space-y-6"
      >
        {/* Back button */}
        <button
          onClick={() => navigate(-1)}
          className="inline-flex items-center gap-2 text-sm text-gray-500 hover:text-blue-600 transition-colors"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Înapoi la pacienți
        </button>

        {/* ——— Patient Header ——— */}
        <div className="bg-white rounded-xl border border-gray-200 p-6 shadow-sm">
          <div className="flex items-start gap-5">
            <img
              src={patient.avatar}
              alt={patient.name}
              className="w-20 h-20 rounded-full bg-gray-100 flex-shrink-0"
            />
            <div className="flex-1">
              <div className="flex items-center gap-3 flex-wrap">
                <h1 className="text-2xl font-bold text-[#1A1A2E]">{patient.name}</h1>
                <span
                  className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${
                    patient.status === "critical"
                      ? "bg-red-100 text-red-700"
                      : patient.status === "warning"
                      ? "bg-amber-100 text-amber-700"
                      : "bg-emerald-100 text-emerald-700"
                  }`}
                >
                  <span className={`w-1.5 h-1.5 rounded-full ${statusColors[patient.status]}`} />
                  {statusLabels[patient.status]}
                </span>
              </div>
              <div className="flex items-center gap-4 mt-2 text-sm text-gray-500 flex-wrap">
                <span>{patient.age} ani</span>
                <span className="text-gray-300">|</span>
                <span>{mockWeight} kg</span>
                <span className="text-gray-300">|</span>
                <span>{mockHeight} cm</span>
                <span className="text-gray-300">|</span>
                <span>{patient.unit}</span>
                <span className="text-gray-300">|</span>
                <span>ID: {patient.id}</span>
              </div>
              <div className="flex items-center gap-2 mt-3 flex-wrap">
                {mockConditions.map((cond, i) => (
                  <span
                    key={i}
                    className="px-2.5 py-1 bg-gray-100 text-gray-600 text-xs font-medium rounded-full"
                  >
                    {cond}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* ——— Vital Signs Timeline ——— */}
        <div className="bg-white rounded-xl border border-gray-200 p-6 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-bold text-[#1A1A2E]">Semne Vitale - Ultimele 30 Zile</h2>
            <div className="flex gap-1 bg-gray-100 rounded-lg p-1">
              {Object.entries(chartConfig).map(([key, cfg]) => (
                <button
                  key={key}
                  onClick={() => setActiveChart(key)}
                  className={`px-3 py-1.5 text-xs font-medium rounded-md transition-colors ${
                    activeChart === key
                      ? "bg-white text-[#1A1A2E] shadow-sm"
                      : "text-gray-500 hover:text-gray-700"
                  }`}
                >
                  {key === "hr" ? "HR" : key === "hrv" ? "HRV" : key === "sleep" ? "Sleep" : "Stres"}
                </button>
              ))}
            </div>
          </div>

          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                <defs>
                  <linearGradient id="chartGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor={currentConfig.color} stopOpacity={0.15} />
                    <stop offset="95%" stopColor={currentConfig.color} stopOpacity={0.02} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" />
                <XAxis
                  dataKey="dateShort"
                  tick={{ fontSize: 11, fill: "#9CA3AF" }}
                  tickLine={false}
                  axisLine={{ stroke: "#E5E7EB" }}
                  interval={4}
                />
                <YAxis
                  tick={{ fontSize: 11, fill: "#9CA3AF" }}
                  tickLine={false}
                  axisLine={false}
                  width={40}
                />
                <Tooltip content={<ChartTooltip />} />

                {/* Normal range green zone */}
                <ReferenceArea
                  y1={currentConfig.normalMin}
                  y2={currentConfig.normalMax}
                  fill="#10B981"
                  fillOpacity={0.08}
                  stroke="#10B981"
                  strokeOpacity={0.2}
                  strokeDasharray="3 3"
                />
                <ReferenceLine
                  y={currentConfig.normalMin}
                  stroke="#10B981"
                  strokeDasharray="4 4"
                  strokeOpacity={0.5}
                  label={{ value: "Min", position: "insideTopLeft", fontSize: 10, fill: "#10B981" }}
                />
                <ReferenceLine
                  y={currentConfig.normalMax}
                  stroke="#10B981"
                  strokeDasharray="4 4"
                  strokeOpacity={0.5}
                  label={{ value: "Max", position: "insideBottomLeft", fontSize: 10, fill: "#10B981" }}
                />

                {/* Annotation markers */}
                {chartData
                  .filter((d) => d.annotation)
                  .map((d, i) => (
                    <ReferenceLine
                      key={i}
                      x={d.dateShort}
                      stroke="#6366F1"
                      strokeDasharray="2 2"
                      label={{
                        value: "\u270E",
                        position: "top",
                        fontSize: 14,
                        fill: "#6366F1",
                      }}
                    />
                  ))}

                <Area
                  type="monotone"
                  dataKey="value"
                  name={currentConfig.label}
                  stroke={currentConfig.color}
                  strokeWidth={2}
                  fill="url(#chartGradient)"
                  dot={false}
                  activeDot={{ r: 5, fill: currentConfig.color, stroke: "#fff", strokeWidth: 2 }}
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>

          {/* Annotations legend */}
          {annotations.length > 0 && (
            <div className="mt-4 border-t border-gray-100 pt-3">
              <h4 className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
                Adnotări medic
              </h4>
              <div className="space-y-1">
                {annotations.map((a, i) => (
                  <div key={i} className="flex items-center gap-2 text-sm">
                    <span className="w-2 h-2 rounded-full bg-indigo-500 flex-shrink-0" />
                    <span className="text-gray-400 text-xs font-mono">{a.date}</span>
                    <span className="text-gray-600">{a.note}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* ——— Two Column: Medications + Lab Results ——— */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Medications */}
          <div className="bg-white rounded-xl border border-gray-200 p-6 shadow-sm">
            <h2 className="text-lg font-bold text-[#1A1A2E] mb-4">Medicație curentă</h2>
            <div className="space-y-3">
              {mockMedications.map((med, i) => (
                <div
                  key={i}
                  className="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
                >
                  <div>
                    <div className="text-sm font-semibold text-[#1A1A2E]">{med.name}</div>
                    <div className="text-xs text-gray-500 mt-0.5">
                      {med.dose} &middot; {med.frequency}
                    </div>
                  </div>
                  <div className="text-right">
                    <span
                      className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${
                        med.status === "activ"
                          ? "bg-emerald-100 text-emerald-700"
                          : "bg-blue-100 text-blue-700"
                      }`}
                    >
                      {med.status}
                    </span>
                    <div className="text-xs text-gray-400 mt-1">din {med.since}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Lab Results */}
          <div className="bg-white rounded-xl border border-gray-200 p-6 shadow-sm">
            <h2 className="text-lg font-bold text-[#1A1A2E] mb-4">Rezultate laborator</h2>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-200">
                    <th className="text-left py-2 pr-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">
                      Test
                    </th>
                    <th className="text-left py-2 pr-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">
                      Valoare
                    </th>
                    <th className="text-left py-2 pr-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">
                      Ref.
                    </th>
                    <th className="text-left py-2 text-xs font-semibold text-gray-500 uppercase tracking-wide">
                      Status
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {mockLabResults.map((lab, i) => (
                    <tr key={i} className="border-b border-gray-50">
                      <td className="py-2.5 pr-3 text-[#1A1A2E] font-medium">{lab.test}</td>
                      <td className="py-2.5 pr-3 text-gray-700">
                        {lab.value} <span className="text-gray-400">{lab.unit}</span>
                      </td>
                      <td className="py-2.5 pr-3 text-gray-400 text-xs">{lab.range}</td>
                      <td className="py-2.5">
                        <span
                          className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${
                            lab.status === "normal"
                              ? "bg-emerald-100 text-emerald-700"
                              : lab.status === "ridicat"
                              ? "bg-amber-100 text-amber-700"
                              : "bg-red-100 text-red-700"
                          }`}
                        >
                          {lab.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* ——— Sleep Analysis: Hypnogram-style bars ——— */}
        <div className="bg-white rounded-xl border border-gray-200 p-6 shadow-sm">
          <h2 className="text-lg font-bold text-[#1A1A2E] mb-1">Analiza Somnului - Ultimele 7 Nopți</h2>
          <p className="text-sm text-gray-400 mb-4">Distribuția fazelor de somn și scorul nocturn</p>

          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={sleepNights} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" vertical={false} />
                <XAxis
                  dataKey="dateShort"
                  tick={{ fontSize: 11, fill: "#9CA3AF" }}
                  tickLine={false}
                  axisLine={{ stroke: "#E5E7EB" }}
                />
                <YAxis
                  tick={{ fontSize: 11, fill: "#9CA3AF" }}
                  tickLine={false}
                  axisLine={false}
                  width={35}
                  label={{
                    value: "ore",
                    angle: -90,
                    position: "insideLeft",
                    style: { fontSize: 11, fill: "#9CA3AF" },
                  }}
                />
                <Tooltip content={<SleepTooltip />} />
                <Legend
                  verticalAlign="top"
                  align="right"
                  iconType="circle"
                  iconSize={8}
                  wrapperStyle={{ fontSize: 12, color: "#6B7280" }}
                />
                <Bar dataKey="deep" name="Somn profund" stackId="sleep" fill="#1E40AF" radius={[0, 0, 0, 0]} />
                <Bar dataKey="light" name="Somn ușor" stackId="sleep" fill="#60A5FA" radius={[0, 0, 0, 0]} />
                <Bar dataKey="rem" name="REM" stackId="sleep" fill="#818CF8" radius={[0, 0, 0, 0]} />
                <Bar dataKey="awake" name="Treaz" stackId="sleep" fill="#FCA5A5" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          {/* Sleep score row */}
          <div className="flex gap-3 mt-4 overflow-x-auto">
            {sleepNights.map((night, i) => (
              <div
                key={i}
                className="flex-1 min-w-[80px] text-center p-2 bg-gray-50 rounded-lg"
              >
                <div className="text-xs text-gray-400">{night.dateShort}</div>
                <div
                  className={`text-lg font-bold mt-1 ${
                    night.score >= 70
                      ? "text-emerald-600"
                      : night.score >= 50
                      ? "text-amber-600"
                      : "text-red-600"
                  }`}
                >
                  {night.score}
                </div>
                <div className="text-xs text-gray-400">{night.total}h total</div>
              </div>
            ))}
          </div>
        </div>

        {/* ——— Recommendations ——— */}
        <div className="bg-white rounded-xl border border-gray-200 p-6 shadow-sm">
          <h2 className="text-lg font-bold text-[#1A1A2E] mb-4">Recomandări medic</h2>
          <textarea
            value={recommendation}
            onChange={(e) => setRecommendation(e.target.value)}
            placeholder="Scrieți recomandările pentru pacient..."
            className="w-full h-32 p-4 bg-gray-50 border border-gray-200 rounded-lg text-sm text-[#1A1A2E] placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
          />
          <div className="flex items-center justify-between mt-3">
            <p className="text-xs text-gray-400">
              Recomandările vor fi adăugate la dosarul pacientului
            </p>
            <button
              onClick={() => {
                if (recommendation.trim()) {
                  alert("Recomandare salvată cu succes!");
                  setRecommendation("");
                }
              }}
              className="px-5 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={!recommendation.trim()}
            >
              Salvează recomandarea
            </button>
          </div>
        </div>

        {/* ——— Alert History ——— */}
        <div className="bg-white rounded-xl border border-gray-200 p-6 shadow-sm">
          <h2 className="text-lg font-bold text-[#1A1A2E] mb-4">Istoric alerte</h2>
          {patientAlerts.length === 0 ? (
            <div className="text-center py-8">
              <svg className="w-12 h-12 mx-auto text-gray-300 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p className="text-gray-400 text-sm">Nicio alertă pentru acest pacient</p>
            </div>
          ) : (
            <div className="space-y-3">
              {patientAlerts.map((alert) => (
                <div
                  key={alert.id}
                  className={`flex items-start gap-4 p-4 rounded-lg border ${
                    alert.type === "critical"
                      ? "bg-red-50 border-red-200"
                      : alert.type === "warning"
                      ? "bg-amber-50 border-amber-200"
                      : "bg-blue-50 border-blue-200"
                  }`}
                >
                  <span
                    className={`mt-1 w-3 h-3 rounded-full flex-shrink-0 ${
                      alert.type === "critical"
                        ? "bg-red-500"
                        : alert.type === "warning"
                        ? "bg-amber-500"
                        : "bg-blue-500"
                    }`}
                  />
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-semibold text-[#1A1A2E]">{alert.title}</span>
                      <span
                        className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                          alert.type === "critical"
                            ? "bg-red-200 text-red-800"
                            : alert.type === "warning"
                            ? "bg-amber-200 text-amber-800"
                            : "bg-blue-200 text-blue-800"
                        }`}
                      >
                        {alert.type}
                      </span>
                      {alert.acknowledged && (
                        <span className="px-2 py-0.5 bg-gray-200 text-gray-600 rounded-full text-xs font-medium">
                          confirmat
                        </span>
                      )}
                    </div>
                    <p className="text-sm text-gray-600 mt-1">{alert.message}</p>
                    <p className="text-xs text-gray-400 mt-1">
                      {new Date(alert.timestamp).toLocaleString("ro-RO")}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* ——— Recent Appointments for this patient ——— */}
        {patientAppointments.length > 0 && (
          <div className="bg-white rounded-xl border border-gray-200 p-6 shadow-sm">
            <h2 className="text-lg font-bold text-[#1A1A2E] mb-4">Programări recente</h2>
            <div className="space-y-3">
              {patientAppointments.map((appt) => (
                <div key={appt.id} className="flex items-center gap-4 p-3 bg-gray-50 rounded-lg">
                  <div
                    className={`w-2 h-10 rounded-full flex-shrink-0 ${
                      appt.status === "scheduled"
                        ? "bg-blue-500"
                        : appt.status === "completed"
                        ? "bg-emerald-500"
                        : "bg-gray-400"
                    }`}
                  />
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-semibold text-[#1A1A2E]">{appt.type}</span>
                      <span
                        className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                          appt.status === "scheduled"
                            ? "bg-blue-100 text-blue-700"
                            : appt.status === "completed"
                            ? "bg-emerald-100 text-emerald-700"
                            : "bg-gray-100 text-gray-600"
                        }`}
                      >
                        {appt.status}
                      </span>
                    </div>
                    <div className="text-xs text-gray-500 mt-0.5">
                      {new Date(appt.date).toLocaleString("ro-RO")} &middot; {appt.duration} min &middot;{" "}
                      {appt.providerName}
                    </div>
                    {appt.notes && (
                      <div className="text-xs text-gray-400 mt-0.5 italic">{appt.notes}</div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </motion.div>
    </div>
  );
}

/* ——————————————————————————————————————————————
   Sleep Tooltip
   —————————————————————————————————————————————— */
function SleepTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null;
  const colorMap = {
    "Somn profund": "#1E40AF",
    "Somn ușor": "#60A5FA",
    REM: "#818CF8",
    Treaz: "#FCA5A5",
  };
  return (
    <div className="bg-white border border-gray-200 rounded-lg shadow-lg px-3 py-2 text-sm">
      <div className="font-medium text-[#1A1A2E] mb-1">{label}</div>
      {payload.map((entry, i) => (
        <div key={i} className="flex items-center gap-2">
          <span
            className="w-2.5 h-2.5 rounded-full"
            style={{ backgroundColor: colorMap[entry.name] || entry.color }}
          />
          <span className="text-gray-600">{entry.name}:</span>
          <span className="font-semibold text-[#1A1A2E]">{entry.value}h</span>
        </div>
      ))}
    </div>
  );
}
