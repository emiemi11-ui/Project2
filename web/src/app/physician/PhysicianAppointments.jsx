import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { appointments as mockAppts } from "../../data/mockData";
import { AnimatedSection, StaggerContainer, StaggerItem, AnimatedNumber } from "../../components/AnimatedSection";
import { motion } from "framer-motion";

const localAppointments = [
  { id: 1, date: "2026-03-10", time: "08:30", patient: "Sofia Ramirez", patientId: "person-012", reason: "Control periodic", status: "upcoming" },
  { id: 2, date: "2026-03-10", time: "10:00", patient: "Liam O'Brien", patientId: "person-011", reason: "Evaluare post-accidentare", status: "upcoming" },
  { id: 3, date: "2026-03-10", time: "11:30", patient: "Tyler Brooks", patientId: "person-008", reason: "Revizuire tratament", status: "upcoming" },
  { id: 4, date: "2026-03-11", time: "09:00", patient: "Jasmine Lee", patientId: "person-016", reason: "Simptome respiratorii", status: "upcoming" },
  { id: 5, date: "2026-03-11", time: "14:00", patient: "Brandon Yates", patientId: "person-019", reason: "Screening anual", status: "upcoming" },
  { id: 6, date: "2026-03-09", time: "09:00", patient: "Ryan Foster", patientId: "person-006", reason: "Follow-up genunchi", status: "completed" },
  { id: 7, date: "2026-03-09", time: "10:30", patient: "Aisha Patel", patientId: "person-007", reason: "Analize laborator", status: "completed" },
  { id: 8, date: "2026-03-07", time: "15:00", patient: "Derek Washington", patientId: "person-010", reason: "Control tensiune", status: "completed" },
  { id: 9, date: "2026-03-12", time: "08:00", patient: "Ethan Morales", patientId: "person-015", reason: "Screening cardiac", status: "upcoming" },
  { id: 10, date: "2026-03-05", time: "11:00", patient: "Sofia Ramirez", patientId: "person-012", reason: "Urgenta — dureri acute", status: "cancelled" },
];

const statusStyles = {
  upcoming: { bg: "bg-blue-50", text: "text-blue-600", border: "border-blue-200", label: "Programat", dot: "bg-blue-500" },
  completed: { bg: "bg-emerald-50", text: "text-emerald-600", border: "border-emerald-200", label: "Finalizat", dot: "bg-emerald-500" },
  cancelled: { bg: "bg-red-50", text: "text-red-600", border: "border-red-200", label: "Anulat", dot: "bg-red-500" },
};

function MiniCalendar({ appointments }) {
  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const firstDay = (new Date(year, month, 1).getDay() + 6) % 7;
  const apptDates = {};
  appointments.forEach((a) => {
    const day = parseInt(a.date.split("-")[2]);
    if (!apptDates[day]) apptDates[day] = [];
    apptDates[day].push(a.status);
  });

  return (
    <div className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
      <div className="text-sm font-semibold text-gray-700 mb-3 text-center">
        {today.toLocaleDateString("ro-RO", { month: "long", year: "numeric" })}
      </div>
      <div className="grid grid-cols-7 gap-1">
        {["L", "Ma", "Mi", "J", "V", "S", "D"].map((d) => (
          <div key={d} className="text-center text-[9px] text-gray-400 font-medium py-0.5">{d}</div>
        ))}
        {Array.from({ length: firstDay }).map((_, i) => <div key={`p-${i}`} />)}
        {Array.from({ length: daysInMonth }).map((_, i) => {
          const day = i + 1;
          const isToday = day === today.getDate();
          const appts = apptDates[day];
          return (
            <div
              key={day}
              className={`relative aspect-square rounded flex items-center justify-center text-[10px] font-medium ${
                isToday ? "bg-blue-100 text-blue-600 font-bold" :
                appts ? "bg-blue-50 text-blue-700" : "text-gray-400"
              }`}
            >
              {day}
              {appts && (
                <div className="absolute bottom-0.5 flex gap-0.5">
                  {appts.slice(0, 3).map((s, j) => (
                    <div key={j} className={`w-1 h-1 rounded-full ${statusStyles[s]?.dot || "bg-gray-400"}`} />
                  ))}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default function PhysicianAppointments() {
  const navigate = useNavigate();
  const [filter, setFilter] = useState("all");
  const [dateFilter, setDateFilter] = useState("");

  const filtered = localAppointments.filter((a) => {
    if (filter !== "all" && a.status !== filter) return false;
    if (dateFilter && a.date !== dateFilter) return false;
    return true;
  });

  const stats = useMemo(() => ({
    total: localAppointments.length,
    upcoming: localAppointments.filter((a) => a.status === "upcoming").length,
    completed: localAppointments.filter((a) => a.status === "completed").length,
    todayCount: localAppointments.filter((a) => a.date === new Date().toISOString().split("T")[0]).length,
  }), []);

  return (
    <main className="p-4 sm:p-6 overflow-auto">
      <AnimatedSection>
        <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
          <h1 className="text-xl sm:text-2xl font-bold text-gray-800">Programari</h1>
          <div className="flex items-center gap-4">
            <div className="text-center">
              <div className="text-xl font-bold text-blue-600"><AnimatedNumber value={stats.upcoming} /></div>
              <div className="text-[10px] text-gray-400 uppercase tracking-wider">Viitoare</div>
            </div>
            <div className="text-center">
              <div className="text-xl font-bold text-emerald-600"><AnimatedNumber value={stats.completed} /></div>
              <div className="text-[10px] text-gray-400 uppercase tracking-wider">Finalizate</div>
            </div>
            <div className="text-center">
              <div className="text-xl font-bold text-gray-800"><AnimatedNumber value={stats.todayCount} /></div>
              <div className="text-[10px] text-gray-400 uppercase tracking-wider">Azi</div>
            </div>
          </div>
        </div>
      </AnimatedSection>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        {/* Mini Calendar */}
        <AnimatedSection delay={0.05} className="lg:col-span-1">
          <MiniCalendar appointments={localAppointments} />
        </AnimatedSection>

        <div className="lg:col-span-3">
          {/* Filters */}
          <AnimatedSection delay={0.1}>
            <div className="flex flex-wrap items-center gap-2 sm:gap-3 mb-6">
              {["all", "upcoming", "completed", "cancelled"].map((f) => (
                <button
                  key={f}
                  onClick={() => setFilter(f)}
                  className={`px-3 sm:px-4 py-1.5 rounded-lg text-xs sm:text-sm font-medium transition-all border ${
                    filter === f
                      ? "bg-blue-50 text-blue-600 border-blue-200"
                      : "bg-white text-gray-500 border-gray-200 hover:text-gray-700"
                  }`}
                >
                  {f === "all" ? "Toate" : statusStyles[f]?.label}
                </button>
              ))}
              <input
                type="date"
                value={dateFilter}
                onChange={(e) => setDateFilter(e.target.value)}
                className="ml-auto px-3 py-1.5 rounded-lg border border-gray-200 text-xs sm:text-sm text-gray-600 bg-white"
              />
              {dateFilter && (
                <button onClick={() => setDateFilter("")} className="text-xs text-gray-400 hover:text-gray-600">Clear</button>
              )}
            </div>
          </AnimatedSection>

          {/* Table */}
          <AnimatedSection delay={0.15}>
            <div className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
              <div className="overflow-x-auto">
                <table className="w-full min-w-[550px]">
                  <thead>
                    <tr className="bg-gray-50 border-b border-gray-200">
                      <th className="text-left px-4 sm:px-5 py-3 text-xs text-gray-500 uppercase tracking-wider font-semibold">Data</th>
                      <th className="text-left px-3 sm:px-4 py-3 text-xs text-gray-500 uppercase tracking-wider font-semibold">Ora</th>
                      <th className="text-left px-3 sm:px-4 py-3 text-xs text-gray-500 uppercase tracking-wider font-semibold">Pacient</th>
                      <th className="text-left px-3 sm:px-4 py-3 text-xs text-gray-500 uppercase tracking-wider font-semibold">Motiv</th>
                      <th className="text-center px-3 sm:px-4 py-3 text-xs text-gray-500 uppercase tracking-wider font-semibold">Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filtered.map((apt, i) => {
                      const st = statusStyles[apt.status];
                      return (
                        <motion.tr
                          key={apt.id}
                          initial={{ opacity: 0, y: 5 }}
                          animate={{ opacity: 1, y: 0 }}
                          transition={{ delay: 0.2 + i * 0.03 }}
                          className="border-b border-gray-100 hover:bg-blue-50/30 transition-colors"
                        >
                          <td className="px-4 sm:px-5 py-3 text-xs sm:text-sm text-gray-700 font-medium">{apt.date}</td>
                          <td className="px-3 sm:px-4 py-3 text-xs sm:text-sm text-gray-600">{apt.time}</td>
                          <td className="px-3 sm:px-4 py-3">
                            <button
                              onClick={() => navigate(`/app/patient/${apt.patientId}`)}
                              className="text-xs sm:text-sm font-medium text-blue-600 hover:underline"
                            >
                              {apt.patient}
                            </button>
                          </td>
                          <td className="px-3 sm:px-4 py-3 text-xs sm:text-sm text-gray-500">{apt.reason}</td>
                          <td className="px-3 sm:px-4 py-3 text-center">
                            <span className={`inline-flex items-center gap-1 px-2 sm:px-2.5 py-0.5 rounded-full text-[10px] sm:text-xs font-medium ${st.bg} ${st.text} border ${st.border}`}>
                              <span className={`w-1.5 h-1.5 rounded-full ${st.dot}`} />
                              {st.label}
                            </span>
                          </td>
                        </motion.tr>
                      );
                    })}
                    {filtered.length === 0 && (
                      <tr>
                        <td colSpan={5} className="px-5 py-12 text-center text-gray-400 text-sm">
                          <div className="text-3xl mb-2">📋</div>
                          Nicio programare gasita cu filtrele selectate.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </AnimatedSection>
        </div>
      </div>
    </main>
  );
}
