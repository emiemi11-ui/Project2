import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { persons } from "../../data/mockData";
import { useMemo } from "react";

export default function TrainerLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();

  const avgReadiness = useMemo(() => {
    const all = persons.filter((p) => p.scores);
    return Math.round(all.reduce((s, p) => s + p.scores.readiness, 0) / all.length);
  }, []);

  const navItems = [
    { label: "Team", path: "/app/team" },
    { label: "Programs", path: "/app/programs" },
    { label: "ACWR Monitor", path: "/app/acwr" },
    { label: "Alerts", path: "/app/alerts" },
  ];

  return (
    <div className="min-h-screen bg-[#0a0f14] text-white flex">
      {/* Sidebar */}
      <aside className="w-56 bg-[#0d1117] border-r border-gray-800/50 flex flex-col p-4 hidden lg:flex">
        <button onClick={() => navigate("/app/dashboard")} className="flex items-center gap-2 mb-6">
          <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-[#f97316] to-[#06b6d4] flex items-center justify-center font-bold text-black text-xs">V</div>
          <span className="font-bold text-sm tracking-tight">Vita<span className="text-[#f97316]">Nova</span></span>
        </button>

        <div className="bg-[#0a0f14] rounded-lg p-3 mb-6 border border-gray-800/50 text-center">
          <div className="text-2xl font-bold text-[#f97316]">{avgReadiness}%</div>
          <div className="text-[10px] text-gray-500 uppercase tracking-wider">Team Readiness</div>
        </div>

        <nav className="flex-1 space-y-0.5">
          {navItems.map((item) => {
            const active = location.pathname === item.path;
            return (
              <button
                key={item.label}
                onClick={() => navigate(item.path)}
                className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors ${
                  active ? "bg-[#f97316]/15 text-[#f97316]" : "text-gray-400 hover:text-white hover:bg-gray-800/50"
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
      </aside>

      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
