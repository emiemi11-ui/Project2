import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { motion } from "framer-motion";

export default function PatientLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useAuth();

  const tabs = [
    { label: "Acasa", path: "/app/my-health", icon: "🏠" },
    { label: "Medicamente", path: "/app/medications", icon: "💊" },
    { label: "Mesaje", path: "/app/messages", icon: "💬" },
  ];

  return (
    <div className="min-h-screen bg-[#fafbfc] flex flex-col">
      {/* Top nav */}
      <nav className="bg-white border-b border-gray-200 px-4 sm:px-6 py-3 sticky top-0 z-40">
        <div className="max-w-4xl mx-auto flex items-center justify-between">
          <button onClick={() => navigate("/app/dashboard")} className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-[#3b82f6] to-[#10b981] flex items-center justify-center font-bold text-white text-sm">V</div>
            <span className="font-bold text-gray-800 hidden sm:inline">VitaNova</span>
          </button>
          <div className="flex items-center gap-1">
            <span className="text-[10px] bg-blue-50 text-blue-600 px-2 py-0.5 rounded font-semibold mr-2 hidden sm:inline">Patient</span>
            {tabs.map((tab) => {
              const active = location.pathname === tab.path;
              return (
                <button
                  key={tab.label}
                  onClick={() => navigate(tab.path)}
                  className={`relative text-sm px-3 py-1.5 rounded-lg transition-all flex items-center gap-1.5 ${
                    active ? "bg-blue-50 text-blue-600 font-medium" : "text-gray-500 hover:text-gray-700 hover:bg-gray-50"
                  }`}
                >
                  <span className="text-base">{tab.icon}</span>
                  <span className="hidden sm:inline">{tab.label}</span>
                  {active && (
                    <motion.div layoutId="patient-tab" className="absolute bottom-0 left-2 right-2 h-0.5 bg-blue-500 rounded-full" />
                  )}
                </button>
              );
            })}
            <button onClick={logout} className="text-sm text-gray-400 hover:text-red-500 ml-3 transition-colors">Logout</button>
          </div>
        </div>
      </nav>

      {/* Content */}
      <main className="flex-1 overflow-auto">
        <motion.div key={location.pathname} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.25 }}>
          <Outlet />
        </motion.div>
      </main>

      {/* Emergency Button */}
      <button className="fixed bottom-6 right-6 w-14 h-14 sm:w-16 sm:h-16 rounded-full bg-red-500 hover:bg-red-600 text-white shadow-lg shadow-red-500/30 flex items-center justify-center transition-all hover:scale-110 z-50">
        <span className="text-xs font-bold">SOS</span>
      </button>
    </div>
  );
}
