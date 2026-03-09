import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

export default function PsychologistLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();

  const navItems = [
    { label: "Mood", path: "/app/mood" },
    { label: "Stress", path: "/app/stress" },
    { label: "Sessions", path: "/app/sessions" },
    { label: "Patients", path: "/app/patients" },
  ];

  return (
    <div className="min-h-screen bg-[#12100e] text-white flex">
      {/* Sidebar */}
      <aside className="w-56 bg-[#16140f] border-r border-[#2a2520] flex flex-col p-5 hidden lg:flex">
        <button onClick={() => navigate("/app/dashboard")} className="flex items-center gap-2 mb-8">
          <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-[#a78bfa] to-[#d4c5a9] flex items-center justify-center font-bold text-black text-xs">V</div>
          <span className="font-bold text-sm tracking-tight">Vita<span className="text-[#a78bfa]">Nova</span></span>
        </button>

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
                onClick={() => navigate(item.path)}
                className={`w-full text-left px-3 py-2.5 rounded-lg text-sm transition-colors ${
                  active ? "bg-[#a78bfa]/15 text-[#a78bfa]" : "text-[#8a7a6a] hover:text-[#d4c5a9] hover:bg-[#1e1c18]"
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
      </aside>

      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
