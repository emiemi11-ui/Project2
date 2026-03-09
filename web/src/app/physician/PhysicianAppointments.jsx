import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

const appointments = [
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
  upcoming: { bg: "bg-blue-50", text: "text-blue-600", border: "border-blue-200", label: "Programat" },
  completed: { bg: "bg-emerald-50", text: "text-emerald-600", border: "border-emerald-200", label: "Finalizat" },
  cancelled: { bg: "bg-red-50", text: "text-red-600", border: "border-red-200", label: "Anulat" },
};

export default function PhysicianAppointments() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [filter, setFilter] = useState("all");
  const [dateFilter, setDateFilter] = useState("");

  const filtered = appointments.filter((a) => {
    if (filter !== "all" && a.status !== filter) return false;
    if (dateFilter && a.date !== dateFilter) return false;
    return true;
  });

  return (
    <div className="min-h-screen bg-[#f8fafc]">
      {/* Sidebar */}
      <div className="flex">
        <aside className="w-60 bg-white border-r border-gray-200 flex flex-col p-4 min-h-screen hidden lg:flex">
          <div className="flex items-center gap-2 mb-8">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-emerald-500 flex items-center justify-center font-bold text-white text-sm">V</div>
            <span className="font-bold text-lg text-gray-800 tracking-tight">Vita<span className="text-blue-600">Nova</span></span>
          </div>
          <nav className="flex-1 space-y-1">
            {[
              { label: "Patients", path: "/app/patients" },
              { label: "Appointments", path: "/app/appointments", active: true },
              { label: "Alerts", path: "/app/alerts" },
            ].map((item) => (
              <button
                key={item.label}
                onClick={() => navigate(item.path)}
                className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors ${
                  item.active ? "bg-blue-50 text-blue-600 font-medium" : "text-gray-500 hover:text-gray-700 hover:bg-gray-50"
                }`}
              >
                {item.label}
              </button>
            ))}
          </nav>
          <button onClick={logout} className="text-sm text-gray-400 hover:text-red-500 transition-colors px-3 py-2">Logout</button>
        </aside>

        <main className="flex-1 p-6 overflow-auto">
          <h1 className="text-2xl font-bold text-gray-800 mb-6">Programari</h1>

          {/* Filters */}
          <div className="flex flex-wrap items-center gap-3 mb-6">
            {["all", "upcoming", "completed", "cancelled"].map((f) => (
              <button
                key={f}
                onClick={() => setFilter(f)}
                className={`px-4 py-1.5 rounded-lg text-sm font-medium transition-colors border ${
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
              className="ml-auto px-3 py-1.5 rounded-lg border border-gray-200 text-sm text-gray-600 bg-white"
            />
            {dateFilter && (
              <button onClick={() => setDateFilter("")} className="text-xs text-gray-400 hover:text-gray-600">Clear</button>
            )}
          </div>

          {/* Table */}
          <div className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="bg-gray-50 border-b border-gray-200">
                    <th className="text-left px-5 py-3 text-xs text-gray-500 uppercase tracking-wider font-semibold">Data</th>
                    <th className="text-left px-4 py-3 text-xs text-gray-500 uppercase tracking-wider font-semibold">Ora</th>
                    <th className="text-left px-4 py-3 text-xs text-gray-500 uppercase tracking-wider font-semibold">Pacient</th>
                    <th className="text-left px-4 py-3 text-xs text-gray-500 uppercase tracking-wider font-semibold">Motiv</th>
                    <th className="text-center px-4 py-3 text-xs text-gray-500 uppercase tracking-wider font-semibold">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((apt) => {
                    const st = statusStyles[apt.status];
                    return (
                      <tr key={apt.id} className="border-b border-gray-100 hover:bg-gray-50/50 transition-colors">
                        <td className="px-5 py-3 text-sm text-gray-700 font-medium">{apt.date}</td>
                        <td className="px-4 py-3 text-sm text-gray-600">{apt.time}</td>
                        <td className="px-4 py-3">
                          <button
                            onClick={() => navigate(`/app/patient/${apt.patientId}`)}
                            className="text-sm font-medium text-blue-600 hover:underline"
                          >
                            {apt.patient}
                          </button>
                        </td>
                        <td className="px-4 py-3 text-sm text-gray-500">{apt.reason}</td>
                        <td className="px-4 py-3 text-center">
                          <span className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-medium ${st.bg} ${st.text} border ${st.border}`}>
                            {st.label}
                          </span>
                        </td>
                      </tr>
                    );
                  })}
                  {filtered.length === 0 && (
                    <tr>
                      <td colSpan={5} className="px-5 py-12 text-center text-gray-400 text-sm">
                        Nicio programare gasita cu filtrele selectate.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
