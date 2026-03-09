import { useState, useMemo } from "react";
import { sessionLogs, getPersonById } from "../../data/mockData";
import { AnimatedSection, StaggerContainer, StaggerItem, AnimatedNumber } from "../../components/AnimatedSection";
import { motion, AnimatePresence } from "framer-motion";

const localSessions = [
  {
    id: 1, patient: "Liam O'Brien", patientId: "person-011", date: "2026-03-10", time: "09:00",
    status: "upcoming", type: "Follow-up",
    briefing: "Stres crescut (78%), somn scazut (35%). A raportat insomnie si anxietate legata de antrenament. Recomandat CBT Session 4.",
    notes: "",
  },
  {
    id: 2, patient: "Jasmine Lee", patientId: "person-016", date: "2026-03-10", time: "11:00",
    status: "upcoming", type: "Assessment",
    briefing: "Prima evaluare psihologica. Archetype struggling. HRV sub medie (42). Stress 62%. Recomandat screening PHQ-9.",
    notes: "",
  },
  {
    id: 3, patient: "Tyler Brooks", patientId: "person-008", date: "2026-03-09", time: "14:00",
    status: "completed", type: "Regular",
    briefing: "Sesiune regulata. Stres 58%, somn imbunatatit (52-61). Progres pe tehnicile de relaxare.",
    notes: "Pacientul a raportat imbunatatiri in calitatea somnului dupa tehnicile de respiratie. Continuam cu exercitii de mindfulness.",
  },
  {
    id: 4, patient: "Sofia Ramirez", patientId: "person-012", date: "2026-03-07", time: "10:00",
    status: "completed", type: "Crisis",
    briefing: "Sesiune de criza. Scor readiness 28, stres 82%. Raport de anxietate acuta.",
    notes: "Situatia stabilizata. Am implementat plan de siguranta. Urmatoarea sesiune in 2 zile.",
  },
  {
    id: 5, patient: "Derek Washington", patientId: "person-010", date: "2026-03-12", time: "15:00",
    status: "upcoming", type: "Regular",
    briefing: "Sesiune regulata de monitorizare. Stres moderat (45%). Somn 65%. Continuare program well-being.",
    notes: "",
  },
  {
    id: 6, patient: "Mia Thompson", patientId: "person-020", date: "2026-03-06", time: "09:00",
    status: "completed", type: "Trauma Processing",
    briefing: "Continuare EMDR. SUD scazut de la 7 la 4. Progres bun.",
    notes: "Pacientul raspunde bine la tratament. Continuam saptamana viitoare.",
  },
];

const statusStyles = {
  upcoming: { bg: "bg-[#a78bfa]/15", text: "text-[#a78bfa]", label: "Programata", dot: "bg-[#a78bfa]" },
  completed: { bg: "bg-emerald-500/15", text: "text-emerald-400", label: "Finalizata", dot: "bg-emerald-400" },
  cancelled: { bg: "bg-red-500/15", text: "text-red-400", label: "Anulata", dot: "bg-red-400" },
};

function MiniCalendar({ sessions }) {
  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const firstDay = (new Date(year, month, 1).getDay() + 6) % 7;

  const sessionDates = new Set(sessions.map((s) => parseInt(s.date.split("-")[2])));

  return (
    <div>
      <div className="text-xs text-[#8a7a6a] font-medium mb-2 text-center">
        {today.toLocaleDateString("ro-RO", { month: "long", year: "numeric" })}
      </div>
      <div className="grid grid-cols-7 gap-1">
        {["L", "Ma", "Mi", "J", "V", "S", "D"].map((d) => (
          <div key={d} className="text-center text-[8px] text-[#5a5045] py-0.5">{d}</div>
        ))}
        {Array.from({ length: firstDay }).map((_, i) => <div key={`p-${i}`} />)}
        {Array.from({ length: daysInMonth }).map((_, i) => {
          const day = i + 1;
          const isToday = day === today.getDate();
          const hasSession = sessionDates.has(day);
          return (
            <div
              key={day}
              className={`aspect-square rounded flex items-center justify-center text-[10px] ${
                isToday ? "bg-[#a78bfa]/30 text-[#a78bfa] font-bold" :
                hasSession ? "bg-[#a78bfa]/10 text-[#d4c5a9]" :
                "text-[#5a5045]"
              }`}
            >
              {day}
              {hasSession && <span className="absolute bottom-0 w-1 h-1 rounded-full bg-[#a78bfa]" />}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default function PsychSessions() {
  const [sessionList, setSessionList] = useState(localSessions);
  const [selectedId, setSelectedId] = useState(null);

  const updateNotes = (id, notes) => {
    setSessionList((prev) => prev.map((s) => (s.id === id ? { ...s, notes } : s)));
  };

  const stats = useMemo(() => ({
    total: sessionList.length,
    upcoming: sessionList.filter((s) => s.status === "upcoming").length,
    completed: sessionList.filter((s) => s.status === "completed").length,
  }), [sessionList]);

  return (
    <main className="p-4 sm:p-6 overflow-auto">
      <AnimatedSection>
        <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
          <div>
            <h1 className="text-xl sm:text-2xl font-bold text-[#d4c5a9]">Sedinte</h1>
            <p className="text-xs sm:text-sm text-[#8a7a6a]">Calendar sedinte, pregatire si note</p>
          </div>
          <div className="flex items-center gap-4">
            <div className="text-center">
              <div className="text-xl font-bold text-[#a78bfa]"><AnimatedNumber value={stats.upcoming} /></div>
              <div className="text-[10px] text-[#5a5045] uppercase tracking-wider">Viitoare</div>
            </div>
            <div className="text-center">
              <div className="text-xl font-bold text-emerald-400"><AnimatedNumber value={stats.completed} /></div>
              <div className="text-[10px] text-[#5a5045] uppercase tracking-wider">Finalizate</div>
            </div>
          </div>
        </div>
      </AnimatedSection>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        {/* Calendar sidebar */}
        <AnimatedSection delay={0.05} className="lg:col-span-1">
          <div className="bg-[#16140f] rounded-xl border border-[#2a2520] p-4">
            <MiniCalendar sessions={sessionList} />
          </div>
        </AnimatedSection>

        {/* Sessions List */}
        <div className="lg:col-span-3">
          <StaggerContainer className="space-y-3 sm:space-y-4">
            {sessionList.map((session) => {
              const st = statusStyles[session.status];
              const isSelected = selectedId === session.id;
              const patientData = getPersonById(session.patientId);

              return (
                <StaggerItem key={session.id}>
                  <motion.div
                    layout
                    className={`rounded-xl bg-[#16140f] border ${isSelected ? "border-[#a78bfa]/30" : "border-[#2a2520]"} overflow-hidden hover:border-[#a78bfa]/20 transition-all`}
                  >
                    <button
                      onClick={() => setSelectedId(isSelected ? null : session.id)}
                      className="w-full text-left p-4 sm:p-5 flex items-center justify-between hover:bg-[#1e1c18]/50 transition-colors"
                    >
                      <div className="flex items-center gap-3 sm:gap-4">
                        <div className="text-center min-w-[50px] sm:min-w-[60px]">
                          <div className="text-base sm:text-lg font-bold text-[#a78bfa]">{session.date.slice(8)}</div>
                          <div className="text-[10px] sm:text-xs text-[#5a5045]">{new Date(session.date + "T00:00:00").toLocaleDateString("ro-RO", { month: "short" })}</div>
                        </div>
                        <div>
                          <div className="font-semibold text-[#d4c5a9] text-sm sm:text-base">{session.patient}</div>
                          <div className="text-[10px] sm:text-xs text-[#8a7a6a] mt-0.5">{session.time} — {session.type}</div>
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className={`px-2 py-0.5 rounded-full text-[10px] sm:text-xs font-medium ${st.bg} ${st.text}`}>
                          <span className={`inline-block w-1.5 h-1.5 rounded-full ${st.dot} mr-1`} />
                          {st.label}
                        </span>
                        <motion.svg
                          animate={{ rotate: isSelected ? 180 : 0 }}
                          className="w-4 h-4 text-[#5a5045]"
                          fill="none" stroke="currentColor" viewBox="0 0 24 24"
                        >
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                        </motion.svg>
                      </div>
                    </button>

                    <AnimatePresence>
                      {isSelected && (
                        <motion.div
                          initial={{ height: 0, opacity: 0 }}
                          animate={{ height: "auto", opacity: 1 }}
                          exit={{ height: 0, opacity: 0 }}
                          transition={{ duration: 0.3 }}
                          className="overflow-hidden"
                        >
                          <div className="px-4 sm:px-5 pb-5 border-t border-[#2a2520] pt-4 space-y-4">
                            {/* Session prep: patient data */}
                            {patientData && (
                              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                                {[
                                  { label: "Readiness", value: patientData.metrics.readinessScore, color: "text-orange-400" },
                                  { label: "Stress", value: `${patientData.metrics.stressScore}%`, color: "text-red-400" },
                                  { label: "Sleep", value: patientData.metrics.sleepScore, color: "text-blue-400" },
                                  { label: "HRV", value: patientData.metrics.hrvScore, color: "text-cyan-400" },
                                ].map((m) => (
                                  <div key={m.label} className="bg-[#1e1c18] rounded-lg p-2.5 border border-[#2a2520] text-center">
                                    <div className={`text-base font-bold ${m.color}`}>{m.value}</div>
                                    <div className="text-[9px] text-[#5a5045] uppercase tracking-wider">{m.label}</div>
                                  </div>
                                ))}
                              </div>
                            )}

                            {/* Briefing */}
                            <div>
                              <h3 className="text-xs text-[#8a7a6a] uppercase tracking-wider mb-2">Auto-Briefing</h3>
                              <div className="p-3 sm:p-4 rounded-lg bg-[#1e1c18] border border-[#2a2520]">
                                <p className="text-xs sm:text-sm text-[#d4c5a9] leading-relaxed">{session.briefing}</p>
                              </div>
                            </div>

                            {/* Notes */}
                            <div>
                              <h3 className="text-xs text-[#8a7a6a] uppercase tracking-wider mb-2">Note sedinta</h3>
                              <textarea
                                value={session.notes}
                                onChange={(e) => updateNotes(session.id, e.target.value)}
                                placeholder="Adauga note despre sedinta..."
                                className="w-full h-24 sm:h-28 p-3 sm:p-4 rounded-lg bg-[#1e1c18] border border-[#2a2520] text-xs sm:text-sm text-[#d4c5a9] placeholder-[#5a5045] resize-none focus:outline-none focus:border-[#a78bfa]/50 transition-colors"
                              />
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
        </div>
      </div>
    </main>
  );
}
