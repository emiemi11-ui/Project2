import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { persons } from "../../data/mockData";
import { useMemo, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";

export default function TrainerLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);

  const avgReadiness = useMemo(() => {
    const soldiers = persons.filter((p) => p.role === "soldier");
    if (soldiers.length === 0) return 0;
    return Math.round(soldiers.reduce((s, p) => s + p.metrics.readinessScore, 0) / soldiers.length);
  }, []);

  const navItems = [
    { label: "Team", path: "/app/team" },
    { label: "Programs", path: "/app/programs" },
    { label: "ACWR Monitor", path: "/app/acwr" },
    { label: "Alerts", path: "/app/alerts" },
  ];

  const handleNav = (path) => {
    navigate(path);
    setMobileOpen(false);
  };

  const sidebar = (
    <>
      <button onClick={() => handleNav("/app/dashboard")} className="flex items-center gap-2 mb-6">
        <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-[#f97316] to-[#06b6d4] flex items-center justify-center font-bold text-black text-xs">V</div>
        <span className="font-bold text-sm tracking-tight">Vita<span className="text-[#f97316]">Nova</span></span>
      </button>

      <div className="flex items-center gap-2 mb-4 px-1">
        <span className="text-[10px] bg-[#f97316]/20 text-[#f97316] px-2 py-0.5 rounded font-semibold">Trainer</span>
      </div>

      <div className="bg-[#0a0f14] rounded-lg p-3 mb-6 border border-gray-800/50 text-center">
        <div className="text-2xl font-bold text-[#f97316]">{avgReadiness}%</div>
        <div className="text-[10px] text-gray-500 uppercase tracking-wider">Team Readiness</div>
      </div>

      <nav className="flex-1 space-y-0.5">
        {navItems.map((item) => {
          const active = location.pathname === item.path || (item.path === "/app/team" && location.pathname.startsWith("/app/athlete/"));
          return (
            <button
              key={item.label}
              onClick={() => handleNav(item.path)}
              className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-all ${
                active
                  ? "bg-[#f97316]/15 text-[#f97316] border-l-2 border-[#f97316]"
                  : "text-gray-400 hover:text-white hover:bg-gray-800/50"
              }`}
            >
              {item.label}
            </button>
          );
        })}
      </nav>

      <div className="border-t border-gray-800/50 pt-3 mt-3">
        <div className="text-xs text-gray-500 mb-2 px-3">{user?.displayName}</div>
        <button onClick={logout} className="text-sm text-gray-500 hover:text-red-400 transition-colors px-3 py-1.5">Logout</button>
      </div>
    </>
  );

  return (
    <div className="min-h-screen bg-[#0a0f14] text-white flex">
      {/* Mobile top bar */}
      <div className="fixed top-0 left-0 right-0 z-50 bg-[#0d1117] border-b border-gray-800/50 px-4 py-3 flex items-center justify-between lg:hidden">
        <button onClick={() => navigate("/app/dashboard")} className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-[#f97316] to-[#06b6d4] flex items-center justify-center font-bold text-black text-xs">V</div>
          <span className="font-bold text-sm">Vita<span className="text-[#f97316]">Nova</span></span>
        </button>
        <button onClick={() => setMobileOpen(!mobileOpen)} className="w-8 h-8 flex flex-col items-center justify-center gap-1.5">
          <motion.span animate={mobileOpen ? { rotate: 45, y: 6 } : { rotate: 0, y: 0 }} className="block w-5 h-0.5 bg-[#f97316]" />
          <motion.span animate={mobileOpen ? { opacity: 0 } : { opacity: 1 }} className="block w-5 h-0.5 bg-[#f97316]" />
          <motion.span animate={mobileOpen ? { rotate: -45, y: -6 } : { rotate: 0, y: 0 }} className="block w-5 h-0.5 bg-[#f97316]" />
        </button>
      </div>

      <AnimatePresence>
        {mobileOpen && (
          <>
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="fixed inset-0 bg-black/60 z-40 lg:hidden" onClick={() => setMobileOpen(false)} />
            <motion.aside
              initial={{ x: -280 }} animate={{ x: 0 }} exit={{ x: -280 }}
              transition={{ type: "spring", damping: 25, stiffness: 300 }}
              className="fixed top-0 left-0 bottom-0 w-64 bg-[#0d1117] border-r border-gray-800/50 flex flex-col p-4 z-50 lg:hidden overflow-auto"
            >
              {sidebar}
            </motion.aside>
          </>
        )}
      </AnimatePresence>

      <aside className="w-56 bg-[#0d1117] border-r border-gray-800/50 flex-col p-4 hidden lg:flex shrink-0">
        {sidebar}
      </aside>

      <main className="flex-1 overflow-auto pt-14 lg:pt-0">
        <motion.div key={location.pathname} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.25 }}>
          <Outlet />
        </motion.div>
      </main>
    </div>
  );
}
