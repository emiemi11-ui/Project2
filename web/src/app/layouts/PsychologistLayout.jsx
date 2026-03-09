import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";

export default function PsychologistLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);

  const navItems = [
    { label: "Mood", path: "/app/mood" },
    { label: "Stress", path: "/app/stress" },
    { label: "Sessions", path: "/app/sessions" },
    { label: "Patients", path: "/app/patients" },
  ];

  const handleNav = (path) => {
    navigate(path);
    setMobileOpen(false);
  };

  const sidebar = (
    <>
      <button onClick={() => handleNav("/app/dashboard")} className="flex items-center gap-2 mb-8">
        <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-[#a78bfa] to-[#d4c5a9] flex items-center justify-center font-bold text-black text-xs">V</div>
        <span className="font-bold text-sm tracking-tight text-[#d4c5a9]">Vita<span className="text-[#a78bfa]">Nova</span></span>
      </button>

      <div className="flex items-center gap-2 mb-4 px-1">
        <span className="text-[10px] bg-[#a78bfa]/20 text-[#a78bfa] px-2 py-0.5 rounded font-semibold">Psychologist</span>
      </div>

      <div className="mb-8 px-1">
        <div className="text-xs text-[#5a5045] mb-1">Welcome back</div>
        <div className="text-sm font-medium text-[#d4c5a9]">{user?.displayName}</div>
      </div>

      <nav className="flex-1 space-y-1">
        {navItems.map((item) => {
          const active = location.pathname === item.path;
          return (
            <button
              key={item.label}
              onClick={() => handleNav(item.path)}
              className={`w-full text-left px-3 py-2.5 rounded-lg text-sm transition-all ${
                active
                  ? "bg-[#a78bfa]/15 text-[#a78bfa] border-l-2 border-[#a78bfa]"
                  : "text-[#8a7a6a] hover:text-[#d4c5a9] hover:bg-[#1e1c18]"
              }`}
            >
              {item.label}
            </button>
          );
        })}
      </nav>

      <button onClick={logout} className="text-sm text-[#5a5045] hover:text-red-400 transition-colors px-3 py-2 text-left">
        Logout
      </button>
    </>
  );

  return (
    <div className="min-h-screen bg-[#12100e] text-white flex">
      {/* Mobile top bar */}
      <div className="fixed top-0 left-0 right-0 z-50 bg-[#16140f] border-b border-[#2a2520] px-4 py-3 flex items-center justify-between lg:hidden">
        <button onClick={() => navigate("/app/dashboard")} className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-[#a78bfa] to-[#d4c5a9] flex items-center justify-center font-bold text-black text-xs">V</div>
          <span className="font-bold text-sm text-[#d4c5a9]">VitaNova</span>
        </button>
        <button onClick={() => setMobileOpen(!mobileOpen)} className="w-8 h-8 flex flex-col items-center justify-center gap-1.5">
          <motion.span animate={mobileOpen ? { rotate: 45, y: 6 } : { rotate: 0, y: 0 }} className="block w-5 h-0.5 bg-[#a78bfa]" />
          <motion.span animate={mobileOpen ? { opacity: 0 } : { opacity: 1 }} className="block w-5 h-0.5 bg-[#a78bfa]" />
          <motion.span animate={mobileOpen ? { rotate: -45, y: -6 } : { rotate: 0, y: 0 }} className="block w-5 h-0.5 bg-[#a78bfa]" />
        </button>
      </div>

      <AnimatePresence>
        {mobileOpen && (
          <>
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="fixed inset-0 bg-black/60 z-40 lg:hidden" onClick={() => setMobileOpen(false)} />
            <motion.aside
              initial={{ x: -280 }} animate={{ x: 0 }} exit={{ x: -280 }}
              transition={{ type: "spring", damping: 25, stiffness: 300 }}
              className="fixed top-0 left-0 bottom-0 w-64 bg-[#16140f] border-r border-[#2a2520] flex flex-col p-5 z-50 lg:hidden overflow-auto"
            >
              {sidebar}
            </motion.aside>
          </>
        )}
      </AnimatePresence>

      <aside className="w-56 bg-[#16140f] border-r border-[#2a2520] flex-col p-5 hidden lg:flex shrink-0">
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
