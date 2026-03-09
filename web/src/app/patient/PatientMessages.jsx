import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

const conversations = [
  {
    id: 1,
    from: "Dr. Sarah Mitchell",
    role: "Medic",
    avatar: "SM",
    unread: true,
    lastMessage: "Rezultatele analizelor arata bine. Valorile sunt in parametri normali.",
    timestamp: "Azi, 09:15",
    messages: [
      { sender: "doctor", text: "Buna ziua! Am primit rezultatele analizelor tale.", time: "Azi, 09:10" },
      { sender: "doctor", text: "Toate valorile sunt in parametri normali. Hemoglobina e la 13.5, leucocitele la 7200.", time: "Azi, 09:12" },
      { sender: "doctor", text: "Rezultatele analizelor arata bine. Valorile sunt in parametri normali. Continuam cu tratamentul curent.", time: "Azi, 09:15" },
      { sender: "patient", text: "Multumesc mult, doctore! Ma simt mai linistita acum.", time: "Azi, 09:20" },
    ],
  },
  {
    id: 2,
    from: "Dr. David Okonkwo",
    role: "Psiholog",
    avatar: "DO",
    unread: true,
    lastMessage: "Cum te-ai simtit in ultima saptamana? Am observat cateva schimbari in datele tale.",
    timestamp: "Ieri, 16:30",
    messages: [
      { sender: "doctor", text: "Buna! Am analizat datele tale din ultima saptamana.", time: "Ieri, 16:25" },
      { sender: "doctor", text: "Am observat ca nivelul de stres a crescut putin. Vrei sa discutam despre asta?", time: "Ieri, 16:28" },
      { sender: "doctor", text: "Cum te-ai simtit in ultima saptamana? Am observat cateva schimbari in datele tale.", time: "Ieri, 16:30" },
    ],
  },
  {
    id: 3,
    from: "Coach James Rodriguez",
    role: "Antrenor",
    avatar: "JR",
    unread: false,
    lastMessage: "Am actualizat programul tau de recuperare. Ia-o usor cu exercitiile de maine.",
    timestamp: "05 Mar, 11:00",
    messages: [
      { sender: "doctor", text: "Salut! Am ajustat programul tau pe baza datelor HRV.", time: "05 Mar, 10:55" },
      { sender: "doctor", text: "Am actualizat programul tau de recuperare. Ia-o usor cu exercitiile de maine.", time: "05 Mar, 11:00" },
      { sender: "patient", text: "Am inteles, multumesc! Voi face doar exercitiile usoare.", time: "05 Mar, 11:15" },
    ],
  },
];

export default function PatientMessages() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [selectedConv, setSelectedConv] = useState(null);

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
              { label: "Medicamente", path: "/app/medications" },
              { label: "Mesaje", path: "/app/messages", active: true },
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
        <h1 className="text-2xl font-bold text-gray-800 mb-6">Mesaje</h1>

        <div className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
          {/* Conversation List */}
          <div className="divide-y divide-gray-100">
            {conversations.map((conv) => (
              <div key={conv.id}>
                <button
                  onClick={() => setSelectedConv(selectedConv === conv.id ? null : conv.id)}
                  className="w-full text-left p-4 hover:bg-gray-50 transition-colors"
                >
                  <div className="flex items-start gap-3">
                    <div className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold ${
                      conv.unread ? "bg-blue-100 text-blue-600" : "bg-gray-100 text-gray-500"
                    }`}>
                      {conv.avatar}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <span className={`text-sm font-semibold ${conv.unread ? "text-gray-800" : "text-gray-600"}`}>
                            {conv.from}
                          </span>
                          <span className="text-xs text-gray-400">({conv.role})</span>
                          {conv.unread && <div className="w-2 h-2 rounded-full bg-blue-500" />}
                        </div>
                        <span className="text-xs text-gray-400">{conv.timestamp}</span>
                      </div>
                      <p className={`text-sm mt-1 truncate ${conv.unread ? "text-gray-700" : "text-gray-400"}`}>
                        {conv.lastMessage}
                      </p>
                    </div>
                  </div>
                </button>

                {/* Expanded Messages */}
                {selectedConv === conv.id && (
                  <div className="px-4 pb-4 bg-gray-50 border-t border-gray-100">
                    <div className="space-y-3 py-4 max-h-80 overflow-y-auto">
                      {conv.messages.map((msg, i) => (
                        <div key={i} className={`flex ${msg.sender === "patient" ? "justify-end" : "justify-start"}`}>
                          <div className={`max-w-[80%] rounded-2xl px-4 py-2.5 ${
                            msg.sender === "patient"
                              ? "bg-blue-500 text-white"
                              : "bg-white border border-gray-200 text-gray-700"
                          }`}>
                            <p className="text-sm">{msg.text}</p>
                            <p className={`text-[10px] mt-1 ${msg.sender === "patient" ? "text-blue-200" : "text-gray-400"}`}>
                              {msg.time}
                            </p>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
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
