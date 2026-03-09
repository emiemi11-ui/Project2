import { useState } from "react";
import { AnimatedSection, StaggerContainer, StaggerItem } from "../../components/AnimatedSection";
import { motion, AnimatePresence } from "framer-motion";

const roleColors = {
  Medic: { bg: "bg-blue-100", text: "text-blue-600", border: "border-blue-200" },
  Psiholog: { bg: "bg-purple-100", text: "text-purple-600", border: "border-purple-200" },
  Antrenor: { bg: "bg-orange-100", text: "text-orange-600", border: "border-orange-200" },
};

function timeAgo(timestamp) {
  if (timestamp.startsWith("Azi")) return timestamp;
  if (timestamp.startsWith("Ieri")) return timestamp;
  return timestamp;
}

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
    contextVitals: { hr: 72, bp: "120/78", spo2: 98 },
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
    contextVitals: { hr: 78, bp: "125/82", stress: 45 },
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
    contextVitals: null,
  },
];

export default function PatientMessages() {
  const [selectedConv, setSelectedConv] = useState(null);
  const [replyText, setReplyText] = useState("");

  const unreadCount = conversations.filter((c) => c.unread).length;

  return (
    <main className="max-w-4xl mx-auto p-4 sm:p-6">
      <AnimatedSection>
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-xl sm:text-2xl font-bold text-gray-800">Mesaje</h1>
          {unreadCount > 0 && (
            <span className="px-2.5 py-1 rounded-full bg-blue-100 text-blue-600 text-xs font-semibold">
              {unreadCount} necitite
            </span>
          )}
        </div>
      </AnimatedSection>

      {conversations.length === 0 && (
        <AnimatedSection>
          <div className="text-center py-16 text-gray-400">
            <div className="text-4xl mb-4">💬</div>
            <p className="text-lg font-medium">Niciun mesaj</p>
            <p className="text-sm mt-1">Mesajele de la specialisti vor aparea aici</p>
          </div>
        </AnimatedSection>
      )}

      <AnimatedSection delay={0.1}>
        <div className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
          <StaggerContainer className="divide-y divide-gray-100">
            {conversations.map((conv) => {
              const colors = roleColors[conv.role] || roleColors.Medic;
              const isSelected = selectedConv === conv.id;
              return (
                <StaggerItem key={conv.id}>
                  <div>
                    <motion.button
                      onClick={() => setSelectedConv(isSelected ? null : conv.id)}
                      whileHover={{ backgroundColor: "rgba(0,0,0,0.02)" }}
                      className="w-full text-left p-3 sm:p-4 transition-colors"
                    >
                      <div className="flex items-start gap-3">
                        <div className={`w-10 h-10 sm:w-11 sm:h-11 rounded-full flex items-center justify-center text-sm font-bold shrink-0 ${colors.bg} ${colors.text}`}>
                          {conv.avatar}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center justify-between">
                            <div className="flex items-center gap-2">
                              <span className={`text-sm font-semibold ${conv.unread ? "text-gray-800" : "text-gray-500"}`}>
                                {conv.from}
                              </span>
                              <span className={`text-[10px] px-1.5 py-0.5 rounded ${colors.bg} ${colors.text}`}>{conv.role}</span>
                              {conv.unread && (
                                <motion.div
                                  animate={{ scale: [1, 1.2, 1] }}
                                  transition={{ repeat: Infinity, duration: 2 }}
                                  className="w-2 h-2 rounded-full bg-blue-500"
                                />
                              )}
                            </div>
                            <span className="text-[10px] sm:text-xs text-gray-400 whitespace-nowrap ml-2">{timeAgo(conv.timestamp)}</span>
                          </div>
                          <p className={`text-xs sm:text-sm mt-1 truncate ${conv.unread ? "text-gray-700 font-medium" : "text-gray-400"}`}>
                            {conv.lastMessage}
                          </p>
                        </div>
                      </div>
                    </motion.button>

                    <AnimatePresence>
                      {isSelected && (
                        <motion.div
                          initial={{ height: 0, opacity: 0 }}
                          animate={{ height: "auto", opacity: 1 }}
                          exit={{ height: 0, opacity: 0 }}
                          transition={{ duration: 0.3 }}
                          className="overflow-hidden"
                        >
                          <div className="px-3 sm:px-4 pb-4 bg-gray-50 border-t border-gray-100">
                            {/* Context Vitals */}
                            {conv.contextVitals && (
                              <div className="flex items-center gap-3 py-3 border-b border-gray-100 mb-3">
                                <span className="text-[10px] text-gray-400 uppercase tracking-wider">Vitals la moment:</span>
                                {conv.contextVitals.hr && <span className="text-xs text-rose-500">❤️ {conv.contextVitals.hr} BPM</span>}
                                {conv.contextVitals.bp && <span className="text-xs text-blue-500">💉 {conv.contextVitals.bp}</span>}
                                {conv.contextVitals.spo2 && <span className="text-xs text-cyan-500">🫁 {conv.contextVitals.spo2}%</span>}
                                {conv.contextVitals.stress && <span className="text-xs text-amber-500">Stres: {conv.contextVitals.stress}</span>}
                              </div>
                            )}

                            {/* Messages */}
                            <div className="space-y-2.5 py-2 max-h-72 overflow-y-auto">
                              {conv.messages.map((msg, i) => (
                                <motion.div
                                  key={i}
                                  initial={{ opacity: 0, y: 10 }}
                                  animate={{ opacity: 1, y: 0 }}
                                  transition={{ delay: i * 0.05 }}
                                  className={`flex ${msg.sender === "patient" ? "justify-end" : "justify-start"}`}
                                >
                                  <div className={`max-w-[85%] sm:max-w-[75%] rounded-2xl px-3.5 py-2.5 ${
                                    msg.sender === "patient"
                                      ? "bg-blue-500 text-white"
                                      : "bg-white border border-gray-200 text-gray-700"
                                  }`}>
                                    <p className="text-sm">{msg.text}</p>
                                    <p className={`text-[10px] mt-1 ${msg.sender === "patient" ? "text-blue-200" : "text-gray-400"}`}>
                                      {msg.time}
                                    </p>
                                  </div>
                                </motion.div>
                              ))}
                            </div>

                            {/* Reply */}
                            <div className="flex items-center gap-2 mt-3 pt-3 border-t border-gray-100">
                              <input
                                type="text"
                                placeholder="Scrie un mesaj..."
                                value={replyText}
                                onChange={(e) => setReplyText(e.target.value)}
                                className="flex-1 px-3 py-2 rounded-xl bg-white border border-gray-200 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300 focus:border-blue-300"
                              />
                              <motion.button
                                whileHover={{ scale: 1.05 }}
                                whileTap={{ scale: 0.95 }}
                                className="px-4 py-2 rounded-xl bg-blue-500 text-white text-sm font-medium hover:bg-blue-600 transition-colors"
                                onClick={() => setReplyText("")}
                              >
                                Trimite
                              </motion.button>
                            </div>
                          </div>
                        </motion.div>
                      )}
                    </AnimatePresence>
                  </div>
                </StaggerItem>
              );
            })}
          </StaggerContainer>
        </div>
      </AnimatedSection>
    </main>
  );
}
