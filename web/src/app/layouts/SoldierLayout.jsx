import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { motion } from "framer-motion";

export default function SoldierLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();

  const tabs = [
    { label: "STATUS", path: "/app/my-readiness" },
    { label: "TRAINING", path: "/app/my-training" },
  ];

  return (
    <div className="min-h-screen bg-[#0a0d0f] text-white font-mono flex flex-col">
      {/* Top bar */}
      <nav className="bg-[#0d1117] border-b border-gray-800/50 px-4 sm:px-6 py-3 sticky top-0 z-40">
        <div className="max-w-5xl mx-auto flex items-center justify-between">
          <button onClick={() => navigate("/app/dashboard")} className="flex items-center gap-2">
            <div className="w-7 h-7 rounded bg-gradient-to-br from-[#84cc16] to-[#00d4ff] flex items-center justify-center font-bold text-black text-xs">V</div>
            <span className="font-bold text-sm uppercase tracking-widest hidden sm:inline">VITANOVA</span>
          </button>
          <div className="flex items-center gap-2 sm:gap-3">
            <span className="text-[9px] bg-[#84cc16]/20 text-[#84cc16] px-2 py-0.5 rounded uppercase tracking-wider font-bold hidden sm:inline">Soldier</span>
            {tabs.map((tab) => {
              const active = location.pathname === tab.path;
              return (
                <button
                  key={tab.label}
                  onClick={() => navigate(tab.path)}
                  className={`relative text-xs px-3 py-1.5 rounded uppercase tracking-widest transition-all ${
                    active ? "bg-[#84cc16]/15 text-[#84cc16]" : "text-gray-500 hover:text-white"
                  }`}
                >
                  {tab.label}
                  {active && (
                    <motion.div layoutId="soldier-tab" className="absolute bottom-0 left-1 right-1 h-0.5 bg-[#84cc16] rounded-full" />
                  )}
                </button>
              );
            })}
            <div className="text-xs text-gray-600 uppercase tracking-wider ml-2 hidden sm:block">{user?.displayName}</div>
            <button onClick={logout} className="text-xs text-gray-600 hover:text-red-400 uppercase tracking-wider ml-1 sm:ml-2">EXIT</button>
          </div>
        </div>
      </nav>

      {/* Content */}
      <main className="flex-1 overflow-auto">
        <motion.div key={location.pathname} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.25 }}>
          <Outlet />
        </motion.div>
      </main>
    </div>
  );
}
