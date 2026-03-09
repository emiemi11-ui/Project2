import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

const vitals = [
  { label: "Heart Rate", value: "72", unit: "BPM", icon: "❤️", color: "text-rose-500", trend: "stable" },
  { label: "Blood Pressure", value: "120/78", unit: "mmHg", icon: "💉", color: "text-blue-500", trend: "stable" },
  { label: "SpO2", value: "98", unit: "%", icon: "🫁", color: "text-cyan-500", trend: "up" },
  { label: "Temperature", value: "36.6", unit: "°C", icon: "🌡️", color: "text-amber-500", trend: "stable" },
];

const todayMeds = [
  { id: 1, name: "Ibuprofen 400mg", time: "08:00", taken: true },
  { id: 2, name: "Omeprazol 20mg", time: "08:00", taken: true },
  { id: 3, name: "Vitamin D 2000UI", time: "12:00", taken: false },
  { id: 4, name: "Magneziu 375mg", time: "20:00", taken: false },
];

const moods = [
  { emoji: "😊", label: "Bine" },
  { emoji: "😐", label: "Ok" },
  { emoji: "😔", label: "Trist" },
  { emoji: "😰", label: "Anxios" },
  { emoji: "😤", label: "Frustrat" },
];

export default function PatientHome() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [meds, setMeds] = useState(todayMeds);
  const [selectedMood, setSelectedMood] = useState(null);

  const toggleMed = (id) => {
    setMeds((prev) => prev.map((m) => (m.id === id ? { ...m, taken: !m.taken } : m)));
  };

  const hour = new Date().getHours();
  const greeting = hour < 12 ? "Buna dimineata" : hour < 18 ? "Buna ziua" : "Buna seara";

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
              { label: "Acasa", path: "/app/my-health", active: true },
              { label: "Medicamente", path: "/app/medications" },
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
        {/* Greeting */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-800">{greeting}, {user?.displayName?.split(" ").pop() || "Maria"}!</h1>
          <p className="text-gray-500 mt-1">Iata rezumatul tau de azi.</p>
        </div>

        {/* Vitals */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          {vitals.map((v) => (
            <div key={v.label} className="bg-white rounded-2xl border border-gray-200 p-4 text-center shadow-sm">
              <div className="text-2xl mb-2">{v.icon}</div>
              <div className={`text-2xl font-bold ${v.color}`}>{v.value}</div>
              <div className="text-xs text-gray-400 mt-1">{v.unit}</div>
              <div className="text-xs text-gray-500 mt-1">{v.label}</div>
            </div>
          ))}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          {/* Next Appointment */}
          <div className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
            <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">Urmatoarea programare</h2>
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-blue-50 flex items-center justify-center text-xl">👨‍⚕️</div>
              <div>
                <div className="font-semibold text-gray-800">Dr. Sarah Mitchell</div>
                <div className="text-sm text-gray-500">Joi, 13 Martie — 10:00</div>
                <div className="text-xs text-blue-500 mt-1">Control periodic</div>
              </div>
            </div>
          </div>

          {/* Unread Messages */}
          <div className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
            <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">Mesaje necitite</h2>
            <div className="space-y-3">
              <div className="flex items-center gap-3">
                <div className="w-2 h-2 rounded-full bg-blue-500" />
                <div>
                  <div className="text-sm font-medium text-gray-800">Dr. Mitchell</div>
                  <div className="text-xs text-gray-500">Rezultatele analizelor arata bine...</div>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="w-2 h-2 rounded-full bg-blue-500" />
                <div>
                  <div className="text-sm font-medium text-gray-800">Dr. Okonkwo</div>
                  <div className="text-xs text-gray-500">Cum te-ai simtit in ultima saptamana?</div>
                </div>
              </div>
            </div>
            <button onClick={() => navigate("/app/messages")} className="text-sm text-blue-500 hover:underline mt-3 block">
              Vezi toate mesajele →
            </button>
          </div>
        </div>

        {/* Meds Today */}
        <div className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm mb-8">
          <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">Medicamente azi</h2>
          <div className="space-y-3">
            {meds.map((med) => (
              <div key={med.id} className="flex items-center justify-between p-3 rounded-xl bg-gray-50 border border-gray-100">
                <div className="flex items-center gap-3">
                  <button
                    onClick={() => toggleMed(med.id)}
                    className={`w-6 h-6 rounded-full border-2 flex items-center justify-center transition-colors ${
                      med.taken ? "bg-emerald-500 border-emerald-500" : "border-gray-300 hover:border-blue-400"
                    }`}
                  >
                    {med.taken && (
                      <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                      </svg>
                    )}
                  </button>
                  <div>
                    <div className={`text-sm font-medium ${med.taken ? "text-gray-400 line-through" : "text-gray-800"}`}>{med.name}</div>
                    <div className="text-xs text-gray-400">{med.time}</div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Mood Selector */}
        <div className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm mb-8">
          <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">Cum te simti azi?</h2>
          <div className="flex items-center justify-center gap-4">
            {moods.map((mood) => (
              <button
                key={mood.label}
                onClick={() => setSelectedMood(mood.label)}
                className={`flex flex-col items-center gap-1 p-3 rounded-xl transition-all ${
                  selectedMood === mood.label
                    ? "bg-blue-50 border-2 border-blue-300 scale-110"
                    : "border-2 border-transparent hover:bg-gray-50"
                }`}
              >
                <span className="text-3xl">{mood.emoji}</span>
                <span className="text-xs text-gray-500">{mood.label}</span>
              </button>
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
