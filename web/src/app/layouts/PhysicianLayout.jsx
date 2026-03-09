import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";

export default function PhysicianLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);

  const navItems = [
    { label: "Patients", path: "/app/patients" },
    { label: "Appointments", path: "/app/appointments" },
    { label: "Alerts", path: "/app/alerts" },
  ];

  const handleNav = (path) => {
    navigate(path);
    setMobileOpen(false);
  };

  const sidebar = (
    <>
      <button onClick={() => handleNav("/app/dashboard")} className="flex items-center gap-2 mb-6">
        <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-[#1e3a5f] to-[#22c55e] flex items-center justify-center font-bold text-white text-xs">V</div>
        <span className="font-bold text-sm text-[#1e3a5f] tracking-tight">Vita<span className="text-[#22c55e]">Nova</span></span>
      </button>

      <div className="flex items-center gap-2 mb-4 px-2">
        <span className="text-[10px] bg-[#22c55e]/15 text-[#22c55e] px-2 py-0.5 rounded font-semibold">Physician</span>
      </div>

      <div className="mb-6 px-2">
        <div className="text-xs text-gray-400 mb-1">Logged in as</div>
        <div className="text-sm font-semibold text-[#1e3a5f]">{user?.displayName}</div>
        <div className="text-xs text-gray-400">{new Date().toLocaleDateString("ro-RO", { weekday: "long", day: "numeric", month: "long" })}</div>
      </div>

      <nav className="flex-1 space-y-0.5">
        {navItems.map((item) => {
          const active = location.pathname === item.path || (item.path === "/app/patients" && location.pathname.startsWith("/app/patient/"));
          return (
            <button
              key={item.label}
              onClick={() => handleNav(item.path)}
              className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-all ${
                active
                  ? "bg-[#1e3a5f]/10 text-[#1e3a5f] font-medium border-l-2 border-[#1e3a5f]"
                  : "text-gray-500 hover:text-[#1e3a5f] hover:bg-gray-50"
              }`}
            >
              {item.label}
            </button>
          );
        })}
      </nav>

      <button onClick={logout} className="text-sm text-gray-400 hover:text-red-500 transition-colors px-3 py-2 text-left">
        Logout
      </button>
    </>
  );

  return (
    <div className="min-h-screen bg-[#f8fafc] flex">
      {/* Mobile top bar */}
      <div className="fixed top-0 left-0 right-0 z-50 bg-white border-b border-gray-200 px-4 py-3 flex items-center justify-between lg:hidden">
        <button onClick={() => navigate("/app/dashboard")} className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-[#1e3a5f] to-[#22c55e] flex items-center justify-center font-bold text-white text-xs">V</div>
          <span className="font-bold text-sm text-[#1e3a5f]">VitaNova</span>
        </button>
        <button onClick={() => setMobileOpen(!mobileOpen)} className="w-8 h-8 flex flex-col items-center justify-center gap-1.5">
          <motion.span animate={mobileOpen ? { rotate: 45, y: 6 } : { rotate: 0, y: 0 }} className="block w-5 h-0.5 bg-[#1e3a5f]" />
          <motion.span animate={mobileOpen ? { opacity: 0 } : { opacity: 1 }} className="block w-5 h-0.5 bg-[#1e3a5f]" />
          <motion.span animate={mobileOpen ? { rotate: -45, y: -6 } : { rotate: 0, y: 0 }} className="block w-5 h-0.5 bg-[#1e3a5f]" />
        </button>
      </div>

      {/* Mobile sidebar overlay */}
      <AnimatePresence>
        {mobileOpen && (
          <>
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="fixed inset-0 bg-black/40 z-40 lg:hidden" onClick={() => setMobileOpen(false)} />
            <motion.aside
              initial={{ x: -280 }} animate={{ x: 0 }} exit={{ x: -280 }}
              transition={{ type: "spring", damping: 25, stiffness: 300 }}
              className="fixed top-0 left-0 bottom-0 w-64 bg-white border-r border-gray-200 flex flex-col p-4 z-50 lg:hidden overflow-auto"
            >
              {sidebar}
            </motion.aside>
          </>
        )}
      </AnimatePresence>

      {/* Desktop sidebar */}
      <aside className="w-56 bg-white border-r border-gray-200 flex-col p-4 hidden lg:flex shrink-0">
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
