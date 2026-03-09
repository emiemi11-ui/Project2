import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

/* ——————————————————————————————————————————————
   Role-based redirect map
   —————————————————————————————————————————————— */

const roleRedirects = {
  commander: "/app/ops",
  physician: "/app/patients",
  psychologist: "/app/mood",
  trainer: "/app/team",
  admin: "/app/users",
  patient: "/app/my-health",
  soldier: "/app/my-readiness",
};

/* ——————————————————————————————————————————————
   Dashboard — Role-based Redirect
   —————————————————————————————————————————————— */

export default function Dashboard() {
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated || !user) {
      navigate("/login", { replace: true });
      return;
    }

    const target = roleRedirects[user.role] || "/app/ops";
    navigate(target, { replace: true });
  }, [user, isAuthenticated, navigate]);

  // Show brief loading state while redirecting
  return (
    <div className="flex items-center justify-center min-h-screen bg-bg">
      <div className="flex flex-col items-center gap-4">
        <div className="w-10 h-10 border-2 border-primary border-t-transparent rounded-full animate-spin" />
        <p className="text-text-muted text-sm font-mono tracking-wide">
          Se redirectioneaz&#259;...
        </p>
      </div>
    </div>
  );
}
