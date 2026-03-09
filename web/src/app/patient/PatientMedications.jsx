import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

const medications = [
  { id: 1, name: "Ibuprofen", dose: "400mg", frequency: "2x/zi", time: ["08:00", "20:00"] },
  { id: 2, name: "Omeprazol", dose: "20mg", frequency: "1x/zi", time: ["08:00"] },
  { id: 3, name: "Vitamin D", dose: "2000UI", frequency: "1x/zi", time: ["12:00"] },
  { id: 4, name: "Magneziu", dose: "375mg", frequency: "1x/zi", time: ["20:00"] },
  { id: 5, name: "Paracetamol", dose: "500mg", frequency: "La nevoie", time: [] },
];

function generateCalendar() {
  const days = [];
  const today = new Date();
  for (let i = 29; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(d.getDate() - i);
    const rand = Math.random();
    days.push({
      date: d,
      label: d.getDate(),
      weekday: d.toLocaleDateString("ro-RO", { weekday: "short" }),
      status: i === 0 ? "today" : rand > 0.15 ? "taken" : "missed",
    });
  }
  return days;
}

export default function PatientMedications() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [takenToday, setTakenToday] = useState({});
  const calendar = useMemo(() => generateCalendar(), []);

  const adherence = useMemo(() => {
    const taken = calendar.filter((d) => d.status === "taken").length;
    return Math.round((taken / 30) * 100);
  }, [calendar]);

  const toggleMed = (id) => {
    setTakenToday((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  return (
    <div className="min-h-screen bg-[#fafbfc]">
      {/* Top Nav */}
      <nav className="bg-white border-b border-gray-200 px-6 py-3">
        <div className="max-w-4xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-emerald-500 flex items-center justify-center font-bold text-white text-sm">V</div>
            <span className="font-bold text-gray-800">VitaNova</span>
          </div>
          <div className="flex items-center gap-4">
            {[
              { label: "Acasa", path: "/app/my-health" },
              { label: "Medicamente", path: "/app/medications", active: true },
              { label: "Mesaje", path: "/app/messages" },
            ].map((item) => (
              <button
                key={item.label}
                onClick={() => navigate(item.path)}
                className={`text-sm px-3 py-1.5 rounded-lg transition-colors ${
                  item.active ? "bg-blue-50 text-blue-600 font-medium" : "text-gray-500 hover:text-gray-700"
                }`}
              >
                {item.label}
              </button>
            ))}
            <button onClick={logout} className="text-sm text-gray-400 hover:text-red-500 ml-2">Logout</button>
          </div>
        </div>
      </nav>

      <main className="max-w-4xl mx-auto p-6">
        <h1 className="text-2xl font-bold text-gray-800 mb-6">Medicamente</h1>

        {/* Adherence Score */}
        <div className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm mb-6">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">Aderenta luna aceasta</h2>
              <div className="flex items-end gap-2 mt-2">
                <span className={`text-4xl font-bold ${adherence >= 80 ? "text-emerald-500" : adherence >= 60 ? "text-amber-500" : "text-red-500"}`}>
                  {adherence}%
                </span>
                <span className="text-sm text-gray-400 mb-1">din medicamente luate la timp</span>
              </div>
            </div>
            <div className="text-5xl">💊</div>
          </div>
        </div>

        {/* Calendar Grid */}
        <div className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm mb-6">
          <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">Ultimele 30 de zile</h2>
          <div className="grid grid-cols-7 gap-2">
            {["L", "Ma", "Mi", "J", "V", "S", "D"].map((d) => (
              <div key={d} className="text-center text-xs text-gray-400 font-medium py-1">{d}</div>
            ))}
            {/* Add padding for first day alignment */}
            {Array.from({ length: (calendar[0]?.date.getDay() + 6) % 7 }).map((_, i) => (
              <div key={`pad-${i}`} />
            ))}
            {calendar.map((day, i) => (
              <div
                key={i}
                className={`aspect-square rounded-lg flex items-center justify-center text-sm font-medium transition-colors ${
                  day.status === "today"
                    ? "bg-blue-100 text-blue-600 ring-2 ring-blue-300"
                    : day.status === "taken"
                    ? "bg-emerald-100 text-emerald-700"
                    : "bg-red-100 text-red-700"
                }`}
                title={day.status === "taken" ? "Luat" : day.status === "missed" ? "Ratat" : "Azi"}
              >
                {day.label}
              </div>
            ))}
          </div>
          <div className="flex items-center gap-4 mt-4 text-xs text-gray-500">
            <div className="flex items-center gap-1"><div className="w-3 h-3 rounded bg-emerald-100" /> Luat</div>
            <div className="flex items-center gap-1"><div className="w-3 h-3 rounded bg-red-100" /> Ratat</div>
            <div className="flex items-center gap-1"><div className="w-3 h-3 rounded bg-blue-100 ring-1 ring-blue-300" /> Azi</div>
          </div>
        </div>

        {/* Medication List */}
        <div className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
          <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">Lista medicamente</h2>
          <div className="space-y-3">
            {medications.map((med) => (
              <div key={med.id} className="flex items-center justify-between p-4 rounded-xl bg-gray-50 border border-gray-100">
                <div className="flex items-center gap-3">
                  <button
                    onClick={() => toggleMed(med.id)}
                    className={`w-7 h-7 rounded-full border-2 flex items-center justify-center transition-colors ${
                      takenToday[med.id] ? "bg-emerald-500 border-emerald-500" : "border-gray-300 hover:border-blue-400"
                    }`}
                  >
                    {takenToday[med.id] && (
                      <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                      </svg>
                    )}
                  </button>
                  <div>
                    <div className={`text-sm font-semibold ${takenToday[med.id] ? "text-gray-400 line-through" : "text-gray-800"}`}>
                      {med.name} — {med.dose}
                    </div>
                    <div className="text-xs text-gray-400">{med.frequency} {med.time.length > 0 && `(${med.time.join(", ")})`}</div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </main>

      {/* Emergency Button */}
      <button className="fixed bottom-6 right-6 w-16 h-16 rounded-full bg-red-500 hover:bg-red-600 text-white shadow-lg shadow-red-500/30 flex items-center justify-center transition-all hover:scale-110 z-50">
        <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192l-3.536 3.536M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-5 0a4 4 0 11-8 0 4 4 0 018 0z" />
        </svg>
      </button>
    </div>
  );
}
