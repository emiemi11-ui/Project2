import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

export default function PhysicianLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();

  const navItems = [
    { label: "Patients", path: "/app/patients" },
    { label: "Appointments", path: "/app/appointments" },
    { label: "Alerts", path: "/app/alerts" },
  ];

  return (
    <div className="min-h-screen bg-[#f8fafc] flex">
      {/* Sidebar */}
      <aside className="w-56 bg-white border-r border-gray-200 flex flex-col p-4 hidden lg:flex">
        <button onClick={() => navigate("/app/dashboard")} className="flex items-center gap-2 mb-6">
          <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-[#1e3a5f] to-[#22c55e] flex items-center justify-center font-bold text-white text-xs">V</div>
          <span className="font-bold text-sm text-[#1e3a5f] tracking-tight">Vita<span className="text-[#22c55e]">Nova</span></span>
        </button>

        <div className="mb-6 px-2">
          <div className="text-xs text-gray-400 mb-1">Logged in as</div>
          <div className="text-sm font-semibold text-[#1e3a5f]">{user?.displayName}</div>
          <div className="text-xs text-gray-400">{new Date().toLocaleDateString("ro-RO", { weekday: "long", day: "numeric", month: "long" })}</div>
        </div>

        <nav className="flex-1 space-y-0.5">
          {navItems.map((item) => {
            const active = location.pathname === item.path;
            return (
              <button
                key={item.label}
                onClick={() => navigate(item.path)}
                className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors ${
                  active ? "bg-[#1e3a5f]/10 text-[#1e3a5f] font-medium" : "text-gray-500 hover:text-[#1e3a5f] hover:bg-gray-50"
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
      </aside>

      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
