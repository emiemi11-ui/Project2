import { useState, useMemo } from "react";
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip } from "recharts";
import { AnimatedSection, StaggerContainer, StaggerItem, AnimatedNumber } from "../../components/AnimatedSection";
import { motion, AnimatePresence } from "framer-motion";

const medications = [
  { id: 1, name: "Ibuprofen", dose: "400mg", frequency: "2x/zi", time: ["08:00", "20:00"], purpose: "Anti-inflamator" },
  { id: 2, name: "Omeprazol", dose: "20mg", frequency: "1x/zi", time: ["08:00"], purpose: "Protectie gastrica" },
  { id: 3, name: "Vitamin D", dose: "2000UI", frequency: "1x/zi", time: ["12:00"], purpose: "Supliment" },
  { id: 4, name: "Magneziu", dose: "375mg", frequency: "1x/zi", time: ["20:00"], purpose: "Supliment mineral" },
  { id: 5, name: "Paracetamol", dose: "500mg", frequency: "La nevoie", time: [], purpose: "Analgezic" },
];

function generateCalendar() {
  const days = [];
  const today = new Date();
  for (let i = 29; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(d.getDate() - i);
    const seed = d.getDate() * 31 + d.getMonth() * 7;
    const rand = ((seed * 16807) % 2147483647) / 2147483647;
    days.push({
      date: d,
      label: d.getDate(),
      weekday: d.toLocaleDateString("ro-RO", { weekday: "short" }),
      status: i === 0 ? "today" : rand > 0.15 ? "taken" : "missed",
    });
  }
  return days;
}

function ToggleSwitch({ checked, onChange }) {
  return (
    <button
      onClick={onChange}
      className={`relative w-12 h-7 rounded-full transition-colors ${checked ? "bg-emerald-500" : "bg-gray-300"}`}
    >
      <motion.div
        animate={{ x: checked ? 20 : 2 }}
        transition={{ type: "spring", stiffness: 500, damping: 30 }}
        className="absolute top-1 w-5 h-5 rounded-full bg-white shadow-md flex items-center justify-center"
      >
        {checked && (
          <motion.svg initial={{ scale: 0 }} animate={{ scale: 1 }} className="w-3 h-3 text-emerald-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
          </motion.svg>
        )}
      </motion.div>
    </button>
  );
}

export default function PatientMedications() {
  const [takenToday, setTakenToday] = useState({ 1: true, 2: true });
  const [expandedMed, setExpandedMed] = useState(null);
  const calendar = useMemo(() => generateCalendar(), []);

  const adherence = useMemo(() => {
    const taken = calendar.filter((d) => d.status === "taken").length;
    return Math.round((taken / 30) * 100);
  }, [calendar]);

  const weeklyData = useMemo(() => {
    const weeks = [];
    for (let w = 0; w < 4; w++) {
      const start = w * 7;
      const weekDays = calendar.slice(start, start + 7);
      const taken = weekDays.filter((d) => d.status === "taken").length;
      weeks.push({ week: `S${w + 1}`, taken, missed: 7 - taken });
    }
    return weeks;
  }, [calendar]);

  const toggleMed = (id) => {
    setTakenToday((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  return (
    <main className="max-w-4xl mx-auto p-4 sm:p-6">
      <AnimatedSection>
        <h1 className="text-xl sm:text-2xl font-bold text-gray-800 mb-6">Medicamente</h1>
      </AnimatedSection>

      {/* Adherence Score */}
      <AnimatedSection delay={0.05}>
        <div className="bg-white rounded-2xl border border-gray-200 p-4 sm:p-6 shadow-sm mb-6">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">Aderenta luna aceasta</h2>
              <div className="flex items-end gap-2 mt-2">
                <span className={`text-3xl sm:text-4xl font-bold ${adherence >= 80 ? "text-emerald-500" : adherence >= 60 ? "text-amber-500" : "text-red-500"}`}>
                  <AnimatedNumber value={adherence} />%
                </span>
                <span className="text-xs sm:text-sm text-gray-400 mb-1">din medicamente luate la timp</span>
              </div>
              {/* Adherence bar */}
              <div className="h-2 w-full max-w-xs bg-gray-100 rounded-full overflow-hidden mt-3">
                <motion.div
                  initial={{ width: 0 }}
                  animate={{ width: `${adherence}%` }}
                  transition={{ duration: 1, delay: 0.3 }}
                  className={`h-full rounded-full ${adherence >= 80 ? "bg-emerald-500" : adherence >= 60 ? "bg-amber-500" : "bg-red-500"}`}
                />
              </div>
            </div>
            <div className="text-4xl sm:text-5xl">💊</div>
          </div>
        </div>
      </AnimatedSection>

      {/* Weekly Chart */}
      <AnimatedSection delay={0.1}>
        <div className="bg-white rounded-2xl border border-gray-200 p-4 sm:p-6 shadow-sm mb-6">
          <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">Aderenta pe saptamani</h2>
          <div className="h-36 sm:h-44">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={weeklyData}>
                <XAxis dataKey="week" tick={{ fill: "#9ca3af", fontSize: 12 }} />
                <YAxis domain={[0, 7]} tick={{ fill: "#9ca3af", fontSize: 11 }} />
                <Tooltip contentStyle={{ borderRadius: 12, border: "1px solid #e5e7eb" }} />
                <Bar dataKey="taken" fill="#22c55e" radius={[4, 4, 0, 0]} name="Luate" />
                <Bar dataKey="missed" fill="#fca5a5" radius={[4, 4, 0, 0]} name="Ratate" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </AnimatedSection>

      {/* Calendar Grid */}
      <AnimatedSection delay={0.15}>
        <div className="bg-white rounded-2xl border border-gray-200 p-4 sm:p-6 shadow-sm mb-6">
          <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">Ultimele 30 de zile</h2>
          <div className="grid grid-cols-7 gap-1.5 sm:gap-2">
            {["L", "Ma", "Mi", "J", "V", "S", "D"].map((d) => (
              <div key={d} className="text-center text-[10px] sm:text-xs text-gray-400 font-medium py-1">{d}</div>
            ))}
            {Array.from({ length: (calendar[0]?.date.getDay() + 6) % 7 }).map((_, i) => (
              <div key={`pad-${i}`} />
            ))}
            {calendar.map((day, i) => (
              <motion.div
                key={i}
                initial={{ scale: 0.5, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                transition={{ delay: 0.2 + i * 0.01 }}
                className={`aspect-square rounded-lg flex items-center justify-center text-xs sm:text-sm font-medium transition-all hover:scale-110 cursor-default ${
                  day.status === "today"
                    ? "bg-blue-100 text-blue-600 ring-2 ring-blue-300"
                    : day.status === "taken"
                    ? "bg-emerald-100 text-emerald-700"
                    : "bg-red-100 text-red-700"
                }`}
                title={day.status === "taken" ? "Luat" : day.status === "missed" ? "Ratat" : "Azi"}
              >
                {day.label}
              </motion.div>
            ))}
          </div>
          <div className="flex items-center gap-4 mt-4 text-[10px] sm:text-xs text-gray-500">
            <div className="flex items-center gap-1"><div className="w-3 h-3 rounded bg-emerald-100" /> Luat</div>
            <div className="flex items-center gap-1"><div className="w-3 h-3 rounded bg-red-100" /> Ratat</div>
            <div className="flex items-center gap-1"><div className="w-3 h-3 rounded bg-blue-100 ring-1 ring-blue-300" /> Azi</div>
          </div>
        </div>
      </AnimatedSection>

      {/* Medication List */}
      <AnimatedSection delay={0.2}>
        <div className="bg-white rounded-2xl border border-gray-200 p-4 sm:p-6 shadow-sm">
          <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">Lista medicamente</h2>
          <StaggerContainer className="space-y-3">
            {medications.map((med) => (
              <StaggerItem key={med.id}>
                <div className="rounded-xl bg-gray-50 border border-gray-100 overflow-hidden hover:border-blue-200 transition-colors">
                  <div className="flex items-center justify-between p-3 sm:p-4">
                    <div className="flex items-center gap-3">
                      <ToggleSwitch checked={!!takenToday[med.id]} onChange={() => toggleMed(med.id)} />
                      <div>
                        <div className={`text-sm font-semibold ${takenToday[med.id] ? "text-gray-400 line-through" : "text-gray-800"}`}>
                          {med.name} — {med.dose}
                        </div>
                        <div className="text-xs text-gray-400">{med.frequency} {med.time.length > 0 && `(${med.time.join(", ")})`}</div>
                      </div>
                    </div>
                    <button
                      onClick={() => setExpandedMed(expandedMed === med.id ? null : med.id)}
                      className="text-xs text-blue-500 hover:underline"
                    >
                      {expandedMed === med.id ? "Ascunde" : "Detalii"}
                    </button>
                  </div>
                  <AnimatePresence>
                    {expandedMed === med.id && (
                      <motion.div
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: "auto", opacity: 1 }}
                        exit={{ height: 0, opacity: 0 }}
                        className="overflow-hidden"
                      >
                        <div className="px-4 pb-4 border-t border-gray-100 pt-3">
                          <div className="grid grid-cols-2 gap-3 text-xs">
                            <div>
                              <span className="text-gray-400">Scop:</span>
                              <span className="ml-1 text-gray-700">{med.purpose}</span>
                            </div>
                            <div>
                              <span className="text-gray-400">Doza:</span>
                              <span className="ml-1 text-gray-700">{med.dose}</span>
                            </div>
                            <div>
                              <span className="text-gray-400">Frecventa:</span>
                              <span className="ml-1 text-gray-700">{med.frequency}</span>
                            </div>
                            <div>
                              <span className="text-gray-400">Ora:</span>
                              <span className="ml-1 text-gray-700">{med.time.length > 0 ? med.time.join(", ") : "La nevoie"}</span>
                            </div>
                          </div>
                        </div>
                      </motion.div>
                    )}
                  </AnimatePresence>
                </div>
              </StaggerItem>
            ))}
          </StaggerContainer>
        </div>
      </AnimatedSection>
    </main>
  );
}
