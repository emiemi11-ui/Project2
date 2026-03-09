import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

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
      <nav className="bg-white border-b border-gray-200 px-6 py-3">
        <div className="max-w-4xl mx-auto flex items-center justify-between">
          <button onClick={() => navigate("/app/dashboard")} className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-[#3b82f6] to-[#10b981] flex items-center justify-center font-bold text-white text-sm">V</div>
            <span className="font-bold text-gray-800">VitaNova</span>
          </button>
          <div className="flex items-center gap-1">
            {tabs.map((tab) => {
              const active = location.pathname === tab.path;
              return (
                <button
                  key={tab.label}
                  onClick={() => navigate(tab.path)}
                  className={`text-sm px-3 py-1.5 rounded-lg transition-colors flex items-center gap-1.5 ${
                    active ? "bg-blue-50 text-blue-600 font-medium" : "text-gray-500 hover:text-gray-700"
                  }`}
                >
                  <span className="text-base">{tab.icon}</span>
                  <span className="hidden sm:inline">{tab.label}</span>
                </button>
              );
            })}
            <button onClick={logout} className="text-sm text-gray-400 hover:text-red-500 ml-3">Logout</button>
          </div>
        </div>
      </nav>

      {/* Content */}
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>

      {/* Emergency Button */}
      <button className="fixed bottom-6 right-6 w-16 h-16 rounded-full bg-red-500 hover:bg-red-600 text-white shadow-lg shadow-red-500/30 flex items-center justify-center transition-all hover:scale-110 z-50">
        <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192l-3.536 3.536M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-5 0a4 4 0 11-8 0 4 4 0 018 0z" />
        </svg>
      </button>
    </div>
  );
}
