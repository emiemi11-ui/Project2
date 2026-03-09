import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { appointments, getPersonById } from "../../data/mockData";
import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, Tooltip } from "recharts";
import { AnimatedSection, StaggerContainer, StaggerItem, AnimatedNumber } from "../../components/AnimatedSection";
import { motion } from "framer-motion";

const vitals = [
  { label: "Heart Rate", value: 72, unit: "BPM", icon: "❤️", color: "text-rose-500", trend: "stable", history: [68, 71, 73, 70, 72, 74, 72] },
  { label: "Blood Pressure", value: "120/78", unit: "mmHg", icon: "💉", color: "text-blue-500", trend: "stable", history: [118, 121, 119, 122, 120, 119, 120] },
  { label: "SpO2", value: 98, unit: "%", icon: "🫁", color: "text-cyan-500", trend: "up", history: [97, 97, 98, 97, 98, 98, 98] },
  { label: "Temperature", value: "36.6", unit: "°C", icon: "🌡️", color: "text-amber-500", trend: "stable", history: [36.5, 36.7, 36.6, 36.8, 36.5, 36.6, 36.6] },
];

const todayMeds = [
  { id: 1, name: "Ibuprofen 400mg", time: "08:00", taken: true },
  { id: 2, name: "Omeprazol 20mg", time: "08:00", taken: true },
  { id: 3, name: "Vitamin D 2000UI", time: "12:00", taken: true },
  { id: 4, name: "Magneziu 375mg", time: "20:00", taken: false },
];

const moods = [
  { emoji: "😊", label: "Bine", color: "bg-emerald-50 border-emerald-300" },
  { emoji: "😐", label: "Ok", color: "bg-blue-50 border-blue-300" },
  { emoji: "😔", label: "Trist", color: "bg-purple-50 border-purple-300" },
  { emoji: "😰", label: "Anxios", color: "bg-amber-50 border-amber-300" },
  { emoji: "😤", label: "Frustrat", color: "bg-red-50 border-red-300" },
];

function MiniSparkline({ data, color }) {
  const chartData = data.map((v, i) => ({ i, v }));
  return (
    <div className="h-8 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={chartData}>
          <Line type="monotone" dataKey="v" stroke={color} strokeWidth={1.5} dot={false} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

const trendArrow = { up: "↑", down: "↓", stable: "→" };
const trendColor = { up: "text-emerald-500", down: "text-red-500", stable: "text-gray-400" };

export default function PatientHome() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [meds, setMeds] = useState(todayMeds);
  const [selectedMood, setSelectedMood] = useState(null);

  const toggleMed = (id) => {
    setMeds((prev) => prev.map((m) => (m.id === id ? { ...m, taken: !m.taken } : m)));
  };

  const medsTaken = meds.filter((m) => m.taken).length;
  const medsTotal = meds.length;

  const nextAppt = useMemo(() => {
    const upcoming = appointments.filter((a) => a.status === "scheduled" && new Date(a.date) > new Date());
    upcoming.sort((a, b) => new Date(a.date) - new Date(b.date));
    return upcoming[0];
  }, []);

  const daysUntilAppt = nextAppt ? Math.ceil((new Date(nextAppt.date) - new Date()) / (1000 * 60 * 60 * 24)) : null;

  const hour = new Date().getHours();
  const greeting = hour < 12 ? "Buna dimineata" : hour < 18 ? "Buna ziua" : "Buna seara";

  const hrChartData = vitals[0].history.map((v, i) => {
    const d = new Date(); d.setDate(d.getDate() - (6 - i));
    return { day: d.toLocaleDateString("ro-RO", { weekday: "short" }), bpm: v };
  });

  return (
    <main className="max-w-4xl mx-auto p-4 sm:p-6">
      {/* Greeting */}
      <AnimatedSection>
        <div className="mb-6 sm:mb-8">
          <h1 className="text-xl sm:text-2xl font-bold text-gray-800">{greeting}, {user?.displayName?.split(" ").pop() || "Maria"}!</h1>
          <p className="text-gray-500 text-sm mt-1">Iata rezumatul tau de azi.</p>
        </div>
      </AnimatedSection>

      {/* Vitals */}
      <StaggerContainer className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4 mb-6 sm:mb-8">
        {vitals.map((v) => (
          <StaggerItem key={v.label}>
            <motion.div
              whileHover={{ scale: 1.03, y: -2 }}
              className="bg-white rounded-2xl border border-gray-200 p-3 sm:p-4 shadow-sm hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between mb-2">
                <span className="text-xl sm:text-2xl">{v.icon}</span>
                <span className={`text-xs font-medium ${trendColor[v.trend]}`}>{trendArrow[v.trend]}</span>
              </div>
              <div className={`text-xl sm:text-2xl font-bold ${v.color}`}>
                {typeof v.value === "number" ? <AnimatedNumber value={v.value} /> : v.value}
              </div>
              <div className="text-[10px] sm:text-xs text-gray-400 mt-0.5">{v.unit}</div>
              <MiniSparkline data={v.history} color={v.color.includes("rose") ? "#f43f5e" : v.color.includes("blue") ? "#3b82f6" : v.color.includes("cyan") ? "#06b6d4" : "#f59e0b"} />
              <div className="text-[10px] sm:text-xs text-gray-500 mt-1">{v.label}</div>
            </motion.div>
          </StaggerItem>
        ))}
      </StaggerContainer>

      {/* Heart Rate Chart */}
      <AnimatedSection delay={0.15}>
        <div className="bg-white rounded-2xl border border-gray-200 p-4 sm:p-6 shadow-sm mb-6 sm:mb-8">
          <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">Puls — ultimele 7 zile</h2>
          <div className="h-40 sm:h-48">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={hrChartData}>
                <XAxis dataKey="day" tick={{ fill: "#9ca3af", fontSize: 11 }} />
                <YAxis domain={[60, 85]} tick={{ fill: "#9ca3af", fontSize: 11 }} />
                <Tooltip contentStyle={{ borderRadius: 12, border: "1px solid #e5e7eb" }} />
                <Line type="monotone" dataKey="bpm" stroke="#f43f5e" strokeWidth={2} dot={{ fill: "#f43f5e", r: 3 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </AnimatedSection>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6 mb-6 sm:mb-8">
        {/* Next Appointment */}
        <AnimatedSection delay={0.2}>
          <motion.div whileHover={{ y: -2 }} className="bg-white rounded-2xl border border-gray-200 p-4 sm:p-6 shadow-sm hover:shadow-md transition-shadow h-full">
            <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">Urmatoarea programare</h2>
            {nextAppt ? (
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-full bg-blue-50 flex items-center justify-center text-xl">👨‍⚕️</div>
                <div className="flex-1">
                  <div className="font-semibold text-gray-800">{nextAppt.providerName}</div>
                  <div className="text-sm text-gray-500">{new Date(nextAppt.date).toLocaleDateString("ro-RO", { weekday: "long", day: "numeric", month: "long" })}</div>
                  <div className="text-xs text-blue-500 mt-1">{nextAppt.type}</div>
                </div>
                {daysUntilAppt !== null && (
                  <div className="text-center">
                    <div className="text-2xl font-bold text-blue-500"><AnimatedNumber value={daysUntilAppt} /></div>
                    <div className="text-[10px] text-gray-400">zile</div>
                  </div>
                )}
              </div>
            ) : (
              <p className="text-gray-400 text-sm">Nicio programare viitoare</p>
            )}
          </motion.div>
        </AnimatedSection>

        {/* Unread Messages */}
        <AnimatedSection delay={0.25}>
          <motion.div whileHover={{ y: -2 }} className="bg-white rounded-2xl border border-gray-200 p-4 sm:p-6 shadow-sm hover:shadow-md transition-shadow h-full">
            <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">Mesaje necitite</h2>
            <div className="space-y-3">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center text-xs font-bold">SM</div>
                <div>
                  <div className="text-sm font-medium text-gray-800">Dr. Mitchell</div>
                  <div className="text-xs text-gray-500 truncate max-w-[200px]">Rezultatele analizelor arata bine...</div>
                </div>
                <div className="w-2 h-2 rounded-full bg-blue-500 ml-auto animate-pulse" />
              </div>
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-full bg-purple-100 text-purple-600 flex items-center justify-center text-xs font-bold">DO</div>
                <div>
                  <div className="text-sm font-medium text-gray-800">Dr. Okonkwo</div>
                  <div className="text-xs text-gray-500 truncate max-w-[200px]">Cum te-ai simtit in ultima saptamana?</div>
                </div>
                <div className="w-2 h-2 rounded-full bg-blue-500 ml-auto animate-pulse" />
              </div>
            </div>
            <button onClick={() => navigate("/app/messages")} className="text-sm text-blue-500 hover:underline mt-3 block">
              Vezi toate mesajele →
            </button>
          </motion.div>
        </AnimatedSection>
      </div>

      {/* Meds Today */}
      <AnimatedSection delay={0.3}>
        <div className="bg-white rounded-2xl border border-gray-200 p-4 sm:p-6 shadow-sm mb-6 sm:mb-8">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">Medicamente azi</h2>
            <span className="text-sm font-medium text-emerald-500">{medsTaken}/{medsTotal} luate</span>
          </div>
          {/* Progress bar */}
          <div className="h-2 bg-gray-100 rounded-full overflow-hidden mb-4">
            <motion.div
              initial={{ width: 0 }}
              animate={{ width: `${(medsTaken / medsTotal) * 100}%` }}
              transition={{ duration: 0.8, delay: 0.5 }}
              className="h-full bg-gradient-to-r from-blue-500 to-emerald-500 rounded-full"
            />
          </div>
          <div className="space-y-2">
            {meds.map((med) => (
              <motion.div
                key={med.id}
                whileHover={{ x: 4 }}
                className="flex items-center justify-between p-3 rounded-xl bg-gray-50 border border-gray-100 hover:border-blue-200 transition-colors"
              >
                <div className="flex items-center gap-3">
                  <motion.button
                    onClick={() => toggleMed(med.id)}
                    whileTap={{ scale: 0.9 }}
                    className={`w-6 h-6 rounded-full border-2 flex items-center justify-center transition-colors ${
                      med.taken ? "bg-emerald-500 border-emerald-500" : "border-gray-300 hover:border-blue-400"
                    }`}
                  >
                    {med.taken && (
                      <motion.svg initial={{ scale: 0 }} animate={{ scale: 1 }} className="w-3.5 h-3.5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                      </motion.svg>
                    )}
                  </motion.button>
                  <div>
                    <div className={`text-sm font-medium ${med.taken ? "text-gray-400 line-through" : "text-gray-800"}`}>{med.name}</div>
                    <div className="text-xs text-gray-400">{med.time}</div>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </AnimatedSection>

      {/* Mood Selector */}
      <AnimatedSection delay={0.35}>
        <div className="bg-white rounded-2xl border border-gray-200 p-4 sm:p-6 shadow-sm">
          <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">Cum te simti azi?</h2>
          <div className="flex items-center justify-center gap-3 sm:gap-4">
            {moods.map((mood) => (
              <motion.button
                key={mood.label}
                onClick={() => setSelectedMood(mood.label)}
                whileHover={{ scale: 1.1 }}
                whileTap={{ scale: 0.95 }}
                className={`flex flex-col items-center gap-1 p-2 sm:p-3 rounded-xl transition-all ${
                  selectedMood === mood.label
                    ? `${mood.color} border-2 scale-110 shadow-md`
                    : "border-2 border-transparent hover:bg-gray-50"
                }`}
              >
                <span className="text-2xl sm:text-3xl">{mood.emoji}</span>
                <span className="text-[10px] sm:text-xs text-gray-500">{mood.label}</span>
              </motion.button>
            ))}
          </div>
          {selectedMood && (
            <motion.p
              initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }}
              className="text-center text-sm text-gray-500 mt-4"
            >
              Ai selectat: <strong>{selectedMood}</strong>. Multumim!
            </motion.p>
          )}
        </div>
      </AnimatedSection>
    </main>
  );
}
