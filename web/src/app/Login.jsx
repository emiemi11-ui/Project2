import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
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
   Dev-mode test accounts
   —————————————————————————————————————————————— */

const testAccounts = [
  { email: "commander@vitanova.com", label: "Commander" },
  { email: "physician@vitanova.com", label: "Physician" },
  { email: "psychologist@vitanova.com", label: "Psychologist" },
  { email: "trainer@vitanova.com", label: "Trainer" },
  { email: "admin@vitanova.com", label: "Admin" },
  { email: "patient1@vitanova.com", label: "Patient" },
  { email: "soldier1@vitanova.com", label: "Soldier" },
];

const isDev = import.meta.env.DEV;

/* ——————————————————————————————————————————————
   Login Page
   —————————————————————————————————————————————— */

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const { login, loginError, clearError, isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  // If already authenticated, redirect
  useEffect(() => {
    if (isAuthenticated && user) {
      const target = roleRedirects[user.role] || "/app/dashboard";
      navigate(target, { replace: true });
    }
  }, [isAuthenticated, user, navigate]);

  function handleSubmit(e) {
    e.preventDefault();
    clearError();

    const success = login(email.trim(), password);
    if (success) {
      // Redirect will happen via the useEffect above on next render
    }
  }

  function fillTestAccount(acct) {
    setEmail(acct.email);
    setPassword("demo123");
    clearError();
  }

  if (isAuthenticated && user) {
    return null;
  }

  return (
    <div
      className="min-h-screen flex items-center justify-center px-4 py-12"
      style={{ backgroundColor: "#03050A" }}
    >
      {/* Background glow */}
      <div className="fixed inset-0 pointer-events-none overflow-hidden">
        <div className="absolute top-1/4 left-1/2 -translate-x-1/2 w-[600px] h-[600px] rounded-full bg-primary/5 blur-[120px]" />
      </div>

      <div className="relative w-full max-w-md">
        {/* Logo */}
        <div className="flex flex-col items-center mb-8">
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-primary to-secondary flex items-center justify-center font-heading font-bold text-[#03050A] text-2xl mb-4">
            V
          </div>
          <h1 className="font-heading font-bold text-2xl text-text tracking-tight">
            Vita<span className="text-primary">Nova</span>
          </h1>
        </div>

        {/* Card */}
        <div className="bg-bg-card border border-border rounded-2xl p-8">
          <h2 className="font-heading font-bold text-xl text-text text-center mb-6">
            Autentificare
          </h2>

          {/* Error message */}
          {loginError && (
            <div className="mb-4 px-4 py-3 rounded-lg bg-danger-dim border border-danger/20 text-danger text-sm font-body">
              {loginError}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Email */}
            <div>
              <label
                htmlFor="email"
                className="block text-sm font-medium text-text-muted mb-1.5 font-body"
              >
                Email
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
                placeholder="exemplu@vitanova.com"
                className="w-full px-4 py-2.5 rounded-lg bg-bg-elevated border border-border text-text placeholder-text-dim text-sm font-body focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-colors"
              />
            </div>

            {/* Password */}
            <div>
              <label
                htmlFor="password"
                className="block text-sm font-medium text-text-muted mb-1.5 font-body"
              >
                Parola
              </label>
              <div className="relative">
                <input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  autoComplete="current-password"
                  placeholder="Introdu parola"
                  className="w-full px-4 py-2.5 rounded-lg bg-bg-elevated border border-border text-text placeholder-text-dim text-sm font-body focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-colors pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-text-dim hover:text-text-muted transition-colors"
                  tabIndex={-1}
                >
                  {showPassword ? (
                    <svg
                      className="w-4 h-4"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={1.5}
                        d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88"
                      />
                    </svg>
                  ) : (
                    <svg
                      className="w-4 h-4"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={1.5}
                        d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"
                      />
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={1.5}
                        d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                      />
                    </svg>
                  )}
                </button>
              </div>
            </div>

            {/* Submit */}
            <button
              type="submit"
              className="w-full py-2.5 rounded-lg font-heading font-semibold text-sm text-[#03050A] transition-all duration-200 hover:brightness-110 active:scale-[0.98]"
              style={{ backgroundColor: "#00E5A0" }}
            >
              Intr&#259;
            </button>
          </form>

          {/* Register link */}
          <p className="text-center text-sm text-text-muted mt-6 font-body">
            Nu ai cont?{" "}
            <Link
              to="/register"
              className="text-primary hover:text-primary-hover font-medium transition-colors"
            >
              &#206;nregistreaz&#259;-te
            </Link>
          </p>
        </div>

        {/* Dev mode test accounts */}
        {isDev && (
          <div className="mt-6 bg-bg-card/50 border border-border/50 rounded-xl p-5">
            <p className="text-xs font-mono text-text-dim mb-3 uppercase tracking-wider">
              Dev Mode &mdash; Test Accounts
            </p>
            <div className="grid grid-cols-2 gap-2">
              {testAccounts.map((acct) => (
                <button
                  key={acct.email}
                  type="button"
                  onClick={() => fillTestAccount(acct)}
                  className="text-left px-3 py-2 rounded-lg bg-bg-elevated/50 border border-border/30 hover:border-primary/30 hover:bg-bg-elevated transition-all text-xs font-body group"
                >
                  <span className="text-text-muted group-hover:text-text block">
                    {acct.label}
                  </span>
                  <span className="text-text-dim font-mono text-[10px]">
                    {acct.email}
                  </span>
                </button>
              ))}
            </div>
            <p className="text-[10px] font-mono text-text-dim mt-3">
              Password for all:{" "}
              <span className="text-text-muted">demo123</span>
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
