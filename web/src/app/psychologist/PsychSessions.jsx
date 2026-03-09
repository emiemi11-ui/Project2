import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

const sessions = [
  {
    id: 1,
    patient: "Liam O'Brien",
    date: "2026-03-10",
    time: "09:00",
    status: "upcoming",
    type: "Follow-up",
    briefing: "Stres crescut (78%), somn scazut (35%). A raportat insomnie si anxietate legata de antrenament. Recomandat CBT Session 4.",
    notes: "",
  },
  {
    id: 2,
    patient: "Jasmine Lee",
    date: "2026-03-10",
    time: "11:00",
    status: "upcoming",
    type: "Assessment",
    briefing: "Prima evaluare psihologica. Archetype struggling. HRV sub medie (42). Stress 62%. Recomandat screening PHQ-9.",
    notes: "",
  },
  {
    id: 3,
    patient: "Tyler Brooks",
    date: "2026-03-09",
    time: "14:00",
    status: "completed",
    type: "Regular",
    briefing: "Sesiune regulata. Stres 58%, somn imbunatatit (52→61). Progres pe tehnicile de relaxare.",
    notes: "Pacientul a raportat imbunatatiri in calitatea somnului dupa tehnicile de respiratie. Continuam cu exercitii de mindfulness.",
  },
  {
    id: 4,
    patient: "Sofia Ramirez",
    date: "2026-03-07",
    time: "10:00",
    status: "completed",
    type: "Crisis",
    briefing: "Sesiune de criza. Scor readiness 28, stres 82%. Raport de anxietate acuta.",
    notes: "Situatia stabilizata. Am implementat plan de siguranta. Urmatoarea sesiune in 2 zile. Coordonare cu Dr. Mitchell pentru medicatie.",
  },
  {
    id: 5,
    patient: "Derek Washington",
    date: "2026-03-12",
    time: "15:00",
    status: "upcoming",
    type: "Regular",
    briefing: "Sesiune regulata de monitorizare. Stres moderat (45%). Somn 65%. Continuare program well-being.",
    notes: "",
  },
];

const statusStyles = {
  upcoming: { bg: "bg-violet-500/15", text: "text-violet-400", label: "Programata" },
  completed: { bg: "bg-emerald-500/15", text: "text-emerald-400", label: "Finalizata" },
  cancelled: { bg: "bg-red-500/15", text: "text-red-400", label: "Anulata" },
};

export default function PsychSessions() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [sessionList, setSessionList] = useState(sessions);
  const [selectedId, setSelectedId] = useState(null);

  const updateNotes = (id, notes) => {
    setSessionList((prev) => prev.map((s) => (s.id === id ? { ...s, notes } : s)));
  };

  return (
    <div className="min-h-screen bg-[#12100e] text-white flex">
      {/* Sidebar */}
      <aside className="w-60 bg-[#16140f] border-r border-[#2a2520] flex flex-col p-4 hidden lg:flex">
        <div className="flex items-center gap-2 mb-8">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-violet-400 to-amber-300 flex items-center justify-center font-bold text-black text-sm">V</div>
          <span className="font-bold text-lg tracking-tight">Vita<span className="text-violet-400">Nova</span></span>
        </div>
        <nav className="flex-1 space-y-1">
          {[
            { label: "Overview", path: "/app/mood" },
            { label: "Mood", path: "/app/mood" },
            { label: "Stress", path: "/app/stress" },
            { label: "Sessions", path: "/app/sessions", active: true },
          ].map((item) => (
            <button
              key={item.label}
              onClick={() => navigate(item.path)}
              className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors ${
                item.active ? "bg-violet-500/15 text-violet-400" : "text-[#a09080] hover:text-white hover:bg-[#1e1c18]"
              }`}
            >
              {item.label}
            </button>
          ))}
        </nav>
        <button onClick={logout} className="text-sm text-[#5a5045] hover:text-red-400 transition-colors px-3 py-2">Logout</button>
      </aside>

      <main className="flex-1 p-6 overflow-auto">
        <h1 className="text-2xl font-bold mb-2 text-[#d4c5a9]">Sedinte</h1>
        <p className="text-sm text-[#8a7a6a] mb-8">Calendar sedinte, pregatire si note</p>

        {/* Sessions List */}
        <div className="space-y-4">
          {sessionList.map((session) => {
            const st = statusStyles[session.status];
            const isSelected = selectedId === session.id;

            return (
              <div key={session.id} className="rounded-xl bg-[#16140f] border border-[#2a2520] overflow-hidden">
                <button
                  onClick={() => setSelectedId(isSelected ? null : session.id)}
                  className="w-full text-left p-5 flex items-center justify-between hover:bg-[#1e1c18]/50 transition-colors"
                >
                  <div className="flex items-center gap-4">
                    <div className="text-center min-w-[60px]">
                      <div className="text-lg font-bold text-violet-400">{session.date.slice(8)}</div>
                      <div className="text-xs text-[#5a5045]">{new Date(session.date).toLocaleDateString("ro-RO", { month: "short" })}</div>
                    </div>
                    <div>
                      <div className="font-semibold text-[#d4c5a9]">{session.patient}</div>
                      <div className="text-xs text-[#8a7a6a] mt-0.5">{session.time} — {session.type}</div>
                    </div>
                  </div>
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${st.bg} ${st.text}`}>{st.label}</span>
                </button>

                {isSelected && (
                  <div className="px-5 pb-5 border-t border-[#2a2520] pt-4 space-y-4">
                    {/* Briefing */}
                    <div>
                      <h3 className="text-xs text-[#8a7a6a] uppercase tracking-wider mb-2">Session Prep — Auto-Briefing</h3>
                      <div className="p-4 rounded-lg bg-[#1e1c18] border border-[#2a2520]">
                        <p className="text-sm text-[#d4c5a9] leading-relaxed">{session.briefing}</p>
                      </div>
                    </div>

                    {/* Notes */}
                    <div>
                      <h3 className="text-xs text-[#8a7a6a] uppercase tracking-wider mb-2">Note sedinta</h3>
                      <textarea
                        value={session.notes}
                        onChange={(e) => updateNotes(session.id, e.target.value)}
                        placeholder="Adauga note despre sedinta..."
                        className="w-full h-28 p-4 rounded-lg bg-[#1e1c18] border border-[#2a2520] text-sm text-[#d4c5a9] placeholder-[#5a5045] resize-none focus:outline-none focus:border-violet-500/50"
                      />
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </main>
    </div>
  );
}
