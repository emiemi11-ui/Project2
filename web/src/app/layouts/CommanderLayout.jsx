import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { persons } from "../../data/mockData";
import { useMemo } from "react";

export default function CommanderLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();

  const kpi = useMemo(() => {
    const all = persons.filter((p) => p.scores);
    const ready = all.filter((p) => p.scores.readiness >= 70).length;
    const alerts = all.filter((p) => p.scores.readiness < 45).length;
    return { total: all.length, readyPct: Math.round((ready / all.length) * 100), alerts };
  }, []);

  const navItems = [
    { label: "OPS CENTER", path: "/app/ops" },
    { label: "PERSONNEL", path: "/app/personnel" },
    { label: "UNITS", path: "/app/units" },
    { label: "ALERTS", path: "/app/alerts" },
    { label: "TEAM", path: "/app/team" },
  ];

  return (
    <div className="min-h-screen bg-[#03050a] text-white flex" style={{ fontFamily: "'JetBrains Mono', monospace" }}>
      {/* Sidebar */}
      <aside className="w-56 bg-[#06080f] border-r border-[#00e5a0]/10 flex flex-col p-4 hidden lg:flex">
        <button onClick={() => navigate("/app/dashboard")} className="flex items-center gap-2 mb-6">
          <div className="w-7 h-7 rounded bg-[#00e5a0] flex items-center justify-center font-bold text-black text-xs">V</div>
          <span className="font-bold text-sm tracking-widest uppercase">VITANOVA</span>
        </button>

        {/* KPI Summary */}
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
                onClick={() => navigate(item.path)}
                className={`w-full text-left px-3 py-2 rounded text-xs uppercase tracking-widest transition-colors ${
                  active ? "bg-[#00e5a0]/10 text-[#00e5a0]" : "text-gray-500 hover:text-[#00e5a0] hover:bg-[#0a0e18]"
                }`}
              >
                {item.label}
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
      </aside>

      {/* Content */}
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
