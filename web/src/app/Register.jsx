import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";

/* ——————————————————————————————————————————————
   Plan & Role Configuration
   —————————————————————————————————————————————— */

const plans = [
  {
    id: "SOLO",
    label: "Solo",
    description: "For individuals tracking personal health",
    roles: [{ value: "user", label: "User" }],
  },
  {
    id: "TEAM",
    label: "Team",
    description: "For sports teams and fitness groups",
    roles: [
      { value: "athlete", label: "Athlete" },
      { value: "trainer", label: "Trainer" },
    ],
  },
  {
    id: "MEDICAL",
    label: "Medical",
    description: "For healthcare providers and patients",
    roles: [
      { value: "patient", label: "Patient" },
      { value: "physician", label: "Physician" },
      { value: "psychologist", label: "Psychologist" },
    ],
  },
  {
    id: "TACTICAL",
    label: "Tactical",
    description: "For military and tactical units",
    roles: [
      { value: "soldier", label: "Soldier" },
      { value: "commander", label: "Commander" },
    ],
  },
];

const defaultRoleForPlan = {
  SOLO: "user",
  TEAM: "athlete",
  MEDICAL: "patient",
  TACTICAL: "soldier",
};

/* ——————————————————————————————————————————————
   Register Page
   —————————————————————————————————————————————— */

export default function Register() {
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [selectedPlan, setSelectedPlan] = useState("SOLO");
  const [selectedRole, setSelectedRole] = useState("user");
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const currentPlan = plans.find((p) => p.id === selectedPlan);

  function handlePlanChange(planId) {
    setSelectedPlan(planId);
    setSelectedRole(defaultRoleForPlan[planId]);
  }

  function handleSubmit(e) {
    e.preventDefault();
    setError(null);

    if (!name.trim()) {
      setError("Numele este obligatoriu.");
      return;
    }
    if (!email.trim()) {
      setError("Email-ul este obligatoriu.");
      return;
    }
    if (password.length < 6) {
      setError("Parola trebuie sa aiba cel putin 6 caractere.");
      return;
    }
    if (password !== confirmPassword) {
      setError("Parolele nu coincid.");
      return;
    }

    setSubmitting(true);

    // Simulate registration delay
    setTimeout(() => {
      setSubmitting(false);
      navigate("/login", {
        state: { registered: true },
      });
    }, 800);
  }

  return (
    <div
      className="min-h-screen flex items-center justify-center px-4 py-12"
      style={{ backgroundColor: "#03050A" }}
    >
      {/* Background glow */}
      <div className="fixed inset-0 pointer-events-none overflow-hidden">
        <div className="absolute top-1/3 left-1/2 -translate-x-1/2 w-[600px] h-[600px] rounded-full bg-secondary/5 blur-[120px]" />
      </div>

      <div className="relative w-full max-w-lg">
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
            Creeaz&#259; cont
          </h2>

          {/* Error message */}
          {error && (
            <div className="mb-4 px-4 py-3 rounded-lg bg-danger-dim border border-danger/20 text-danger text-sm font-body">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Name */}
            <div>
              <label
                htmlFor="name"
                className="block text-sm font-medium text-text-muted mb-1.5 font-body"
              >
                Nume complet
              </label>
              <input
                id="name"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                autoComplete="name"
                placeholder="Ion Popescu"
                className="w-full px-4 py-2.5 rounded-lg bg-bg-elevated border border-border text-text placeholder-text-dim text-sm font-body focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-colors"
              />
            </div>

            {/* Email */}
            <div>
              <label
                htmlFor="reg-email"
                className="block text-sm font-medium text-text-muted mb-1.5 font-body"
              >
                Email
              </label>
              <input
                id="reg-email"
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
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label
                  htmlFor="reg-password"
                  className="block text-sm font-medium text-text-muted mb-1.5 font-body"
                >
                  Parola
                </label>
                <input
                  id="reg-password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  autoComplete="new-password"
                  placeholder="Min. 6 caractere"
                  className="w-full px-4 py-2.5 rounded-lg bg-bg-elevated border border-border text-text placeholder-text-dim text-sm font-body focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-colors"
                />
              </div>
              <div>
                <label
                  htmlFor="reg-confirm"
                  className="block text-sm font-medium text-text-muted mb-1.5 font-body"
                >
                  Confirma parola
                </label>
                <input
                  id="reg-confirm"
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                  autoComplete="new-password"
                  placeholder="Repeta parola"
                  className="w-full px-4 py-2.5 rounded-lg bg-bg-elevated border border-border text-text placeholder-text-dim text-sm font-body focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-colors"
                />
              </div>
            </div>

            {/* Plan Selector */}
            <div>
              <label className="block text-sm font-medium text-text-muted mb-2 font-body">
                Plan
              </label>
              <div className="grid grid-cols-2 gap-2">
                {plans.map((plan) => (
                  <button
                    key={plan.id}
                    type="button"
                    onClick={() => handlePlanChange(plan.id)}
                    className={`text-left px-4 py-3 rounded-lg border text-sm font-body transition-all ${
                      selectedPlan === plan.id
                        ? "bg-primary/10 border-primary/40 text-text"
                        : "bg-bg-elevated border-border text-text-muted hover:border-border-hover hover:bg-bg-hover"
                    }`}
                  >
                    <span className="block font-semibold">{plan.label}</span>
                    <span className="block text-xs text-text-dim mt-0.5">
                      {plan.description}
                    </span>
                  </button>
                ))}
              </div>
            </div>

            {/* Role Selector */}
            {currentPlan && currentPlan.roles.length > 1 && (
              <div>
                <label className="block text-sm font-medium text-text-muted mb-2 font-body">
                  Rol
                </label>
                <div className="flex gap-2 flex-wrap">
                  {currentPlan.roles.map((role) => (
                    <button
                      key={role.value}
                      type="button"
                      onClick={() => setSelectedRole(role.value)}
                      className={`px-4 py-2 rounded-lg border text-sm font-body transition-all ${
                        selectedRole === role.value
                          ? "bg-primary/10 border-primary/40 text-text"
                          : "bg-bg-elevated border-border text-text-muted hover:border-border-hover hover:bg-bg-hover"
                      }`}
                    >
                      {role.label}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Submit */}
            <button
              type="submit"
              disabled={submitting}
              className="w-full py-2.5 rounded-lg font-heading font-semibold text-sm text-[#03050A] transition-all duration-200 hover:brightness-110 active:scale-[0.98] disabled:opacity-60 disabled:cursor-not-allowed"
              style={{ backgroundColor: "#00E5A0" }}
            >
              {submitting ? (
                <span className="flex items-center justify-center gap-2">
                  <svg
                    className="w-4 h-4 animate-spin"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                    />
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
                    />
                  </svg>
                  Se creeaz&#259;...
                </span>
              ) : (
                "Creeaz\u0103 cont"
              )}
            </button>
          </form>

          {/* Login link */}
          <p className="text-center text-sm text-text-muted mt-6 font-body">
            Ai deja cont?{" "}
            <Link
              to="/login"
              className="text-primary hover:text-primary-hover font-medium transition-colors"
            >
              Autentific&#259;-te
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
