import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { persons, alerts } from "../../data/mockData";
import { useMemo, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";

export default function CommanderLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);

  const kpi = useMemo(() => {
    const soldiers = persons.filter((p) => ["soldier", "patient"].includes(p.role));
    const total = soldiers.length;
    const ready = soldiers.filter((p) => p.metrics.readinessScore >= 70).length;
    const alertCount = alerts.filter((a) => !a.acknowledged).length;
    return { total, readyPct: total > 0 ? Math.round((ready / total) * 100) : 0, alerts: alertCount };
  }, []);

  const navItems = [
    { label: "OPS CENTER", path: "/app/ops" },
    { label: "PERSONNEL", path: "/app/personnel" },
    { label: "UNITS", path: "/app/units" },
    { label: "ALERTS", path: "/app/alerts" },
    { label: "TEAM", path: "/app/team" },
  ];

  const handleNav = (path) => {
    navigate(path);
    setMobileOpen(false);
  };

  const sidebar = (
    <>
      <button onClick={() => handleNav("/app/dashboard")} className="flex items-center gap-2 mb-6">
        <div className="w-7 h-7 rounded bg-[#00e5a0] flex items-center justify-center font-bold text-black text-xs">V</div>
        <span className="font-bold text-sm tracking-widest uppercase">VITANOVA</span>
      </button>

      <div className="flex items-center gap-2 mb-4 px-1">
        <span className="text-[9px] bg-[#00e5a0]/20 text-[#00e5a0] px-2 py-0.5 rounded uppercase tracking-widest font-bold">Commander</span>
      </div>

      <div className="bg-[#0a0e18] rounded-lg p-3 mb-6 border border-[#00e5a0]/10">
        <div className="grid grid-cols-3 gap-2 text-center">
          <div>
            <div className="text-lg font-bold text-[#00e5a0]">{kpi.total}</div>
            <div className="text-[9px] text-gray-600 uppercase tracking-wider">PERS</div>
          </div>
          <div>
            <div className="text-lg font-bold text-[#00e5a0]">{kpi.readyPct}%</div>
            <div className="text-[9px] text-gray-600 uppercase tracking-wider">READY</div>
          </div>
          <div>
            <div className="text-lg font-bold text-[#e83050]">{kpi.alerts}</div>
            <div className="text-[9px] text-gray-600 uppercase tracking-wider">ALERT</div>
          </div>
        </div>
      </div>

      <nav className="flex-1 space-y-0.5">
        {navItems.map((item) => {
          const active = location.pathname === item.path;
          return (
            <button
              key={item.label}
              onClick={() => handleNav(item.path)}
              className={`w-full text-left px-3 py-2 rounded text-xs uppercase tracking-widest transition-all ${
                active
                  ? "bg-[#00e5a0]/10 text-[#00e5a0] border-l-2 border-[#00e5a0]"
                  : "text-gray-500 hover:text-[#00e5a0] hover:bg-[#0a0e18]"
              }`}
            >
              {item.label}
              {item.label === "ALERTS" && kpi.alerts > 0 && (
                <span className="ml-2 text-[9px] bg-[#e83050] text-white px-1.5 py-0.5 rounded-full">{kpi.alerts}</span>
              )}
            </button>
          );
        })}
      </nav>

      <div className="border-t border-[#00e5a0]/10 pt-3 mt-3">
        <div className="text-[10px] text-gray-600 uppercase tracking-wider mb-2 px-3">{user?.displayName}</div>
        <button onClick={logout} className="text-xs text-gray-600 hover:text-[#e83050] transition-colors px-3 py-1.5 uppercase tracking-wider">
          LOGOUT
        </button>
      </div>
    </>
  );

  return (
    <div className="min-h-screen bg-[#03050a] text-white flex" style={{ fontFamily: "'JetBrains Mono', monospace" }}>
      {/* Mobile top bar */}
      <div className="fixed top-0 left-0 right-0 z-50 bg-[#06080f] border-b border-[#00e5a0]/10 px-4 py-3 flex items-center justify-between lg:hidden">
        <button onClick={() => navigate("/app/dashboard")} className="flex items-center gap-2">
          <div className="w-7 h-7 rounded bg-[#00e5a0] flex items-center justify-center font-bold text-black text-xs">V</div>
          <span className="font-bold text-xs tracking-widest uppercase">VITANOVA</span>
        </button>
        <button onClick={() => setMobileOpen(!mobileOpen)} className="w-8 h-8 flex flex-col items-center justify-center gap-1.5">
          <motion.span animate={mobileOpen ? { rotate: 45, y: 6 } : { rotate: 0, y: 0 }} className="block w-5 h-0.5 bg-[#00e5a0]" />
          <motion.span animate={mobileOpen ? { opacity: 0 } : { opacity: 1 }} className="block w-5 h-0.5 bg-[#00e5a0]" />
          <motion.span animate={mobileOpen ? { rotate: -45, y: -6 } : { rotate: 0, y: 0 }} className="block w-5 h-0.5 bg-[#00e5a0]" />
        </button>
      </div>

      {/* Mobile sidebar overlay */}
      <AnimatePresence>
        {mobileOpen && (
          <>
            <motion.div
              initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/60 z-40 lg:hidden" onClick={() => setMobileOpen(false)}
            />
            <motion.aside
              initial={{ x: -280 }} animate={{ x: 0 }} exit={{ x: -280 }}
              transition={{ type: "spring", damping: 25, stiffness: 300 }}
              className="fixed top-0 left-0 bottom-0 w-64 bg-[#06080f] border-r border-[#00e5a0]/10 flex flex-col p-4 z-50 lg:hidden overflow-auto"
            >
              {sidebar}
            </motion.aside>
          </>
        )}
      </AnimatePresence>

      {/* Desktop sidebar */}
      <aside className="w-56 bg-[#06080f] border-r border-[#00e5a0]/10 flex-col p-4 hidden lg:flex shrink-0">
        {sidebar}
      </aside>

      {/* Content */}
      <main className="flex-1 overflow-auto pt-14 lg:pt-0">
        <motion.div
          key={location.pathname}
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.25 }}
        >
          <Outlet />
        </motion.div>
      </main>
    </div>
  );
}
