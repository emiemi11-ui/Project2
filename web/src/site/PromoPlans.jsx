import SiteLayout from "./SiteLayout";
import { motion } from "framer-motion";

const plans = [
  {
    name: "SOLO",
    subtitle: "Personal Health Tracking",
    price: "Free",
    period: "",
    color: "from-emerald-400 to-teal-500",
    border: "border-emerald-500/30",
    roles: ["Individual User"],
    features: [
      "8-module health ecosystem",
      "Real sensor integration (HRV, sleep, steps)",
      "Cognitive performance tests",
      "Habit tracking with Elastic Streaks",
      "Circadian rhythm optimization",
      "Local data storage — 100% private",
      "Smart alarm with sleep cycle detection",
      "Basic nutrition logging",
    ],
    cta: "Download Free",
    highlighted: false,
  },
  {
    name: "TEAM",
    subtitle: "Athletic Performance",
    price: "€9.99",
    period: "/month per athlete",
    color: "from-orange-400 to-amber-500",
    border: "border-orange-500/30",
    roles: ["Trainer", "Athlete / Soldier"],
    features: [
      "Everything in SOLO",
      "ACWR monitoring & injury prevention",
      "Team readiness dashboard",
      "Training program management",
      "Athlete radar charts & comparisons",
      "Recovery load optimization",
      "Unit-level analytics",
      "Real-time alert system",
    ],
    cta: "Start Trial",
    highlighted: true,
  },
  {
    name: "MEDICAL",
    subtitle: "Clinical Monitoring",
    price: "€19.99",
    period: "/month per patient",
    color: "from-blue-400 to-indigo-500",
    border: "border-blue-500/30",
    roles: ["Physician", "Patient"],
    features: [
      "Everything in SOLO",
      "Patient vital signs monitoring",
      "Appointment scheduling system",
      "Medication adherence tracking",
      "Secure physician-patient messaging",
      "Clinical dashboards with trends",
      "Emergency button & protocols",
      "GDPR-compliant data sharing",
    ],
    cta: "Contact Sales",
    highlighted: false,
  },
  {
    name: "TACTICAL",
    subtitle: "Military Operations",
    price: "Custom",
    period: "per unit",
    color: "from-red-400 to-rose-500",
    border: "border-red-500/30",
    roles: ["Commander", "Psychologist", "Admin"],
    features: [
      "Everything in TEAM + MEDICAL",
      "Operational readiness heatmaps",
      "Stress pattern detection",
      "Unit-level risk assessment",
      "Privacy matrix controls",
      "Role-based access (7 roles)",
      "Multi-unit command view",
      "Deployment-ready offline mode",
    ],
    cta: "Request Demo",
    highlighted: false,
  },
];

const comparisonFeatures = [
  { feature: "Health Modules", solo: "8", team: "8", medical: "8", tactical: "8" },
  { feature: "Real Sensor Data", solo: "✓", team: "✓", medical: "✓", tactical: "✓" },
  { feature: "Team Dashboard", solo: "—", team: "✓", medical: "—", tactical: "✓" },
  { feature: "ACWR Monitoring", solo: "—", team: "✓", medical: "—", tactical: "✓" },
  { feature: "Patient Monitoring", solo: "—", team: "—", medical: "✓", tactical: "✓" },
  { feature: "Appointments", solo: "—", team: "—", medical: "✓", tactical: "✓" },
  { feature: "Medication Tracking", solo: "—", team: "—", medical: "✓", tactical: "✓" },
  { feature: "Command View", solo: "—", team: "—", medical: "—", tactical: "✓" },
  { feature: "Stress Heatmaps", solo: "—", team: "—", medical: "—", tactical: "✓" },
  { feature: "Privacy Matrix", solo: "Basic", team: "Standard", medical: "Enhanced", tactical: "Full" },
  { feature: "Roles Supported", solo: "1", team: "2", medical: "2", tactical: "7" },
  { feature: "Offline Mode", solo: "✓", team: "✓", medical: "✓", tactical: "✓" },
];

export default function PromoPlans() {
  return (
    <SiteLayout>
      <section className="pt-32 pb-20 px-6">
        <div className="max-w-7xl mx-auto">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="text-center mb-16"
          >
            <h1 className="text-4xl md:text-5xl font-heading font-bold text-text mb-4">
              Choose Your <span className="text-primary">Plan</span>
            </h1>
            <p className="text-lg text-text-muted max-w-2xl mx-auto font-body">
              From personal health tracking to tactical military operations.
              Every plan includes our complete 8-module ecosystem.
            </p>
          </motion.div>

          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6 mb-24">
            {plans.map((plan, i) => (
              <motion.div
                key={plan.name}
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5, delay: i * 0.1 }}
                className={`relative rounded-2xl border ${plan.border} bg-bg-card p-6 flex flex-col ${
                  plan.highlighted ? "ring-2 ring-orange-500/50 scale-[1.02]" : ""
                }`}
              >
                {plan.highlighted && (
                  <div className="absolute -top-3 left-1/2 -translate-x-1/2 px-4 py-1 rounded-full bg-gradient-to-r from-orange-400 to-amber-500 text-xs font-bold text-black uppercase tracking-wider">
                    Most Popular
                  </div>
                )}
                <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${plan.color} flex items-center justify-center mb-4`}>
                  <span className="text-lg font-heading font-bold text-white">{plan.name[0]}</span>
                </div>
                <h3 className="text-xl font-heading font-bold text-text mb-1">{plan.name}</h3>
                <p className="text-sm text-text-muted mb-4 font-body">{plan.subtitle}</p>
                <div className="mb-6">
                  <span className="text-3xl font-heading font-bold text-text">{plan.price}</span>
                  <span className="text-sm text-text-muted font-body">{plan.period}</span>
                </div>
                <div className="mb-4">
                  <p className="text-xs text-text-dim uppercase tracking-wider mb-2 font-mono">Roles</p>
                  <div className="flex flex-wrap gap-1.5">
                    {plan.roles.map((role) => (
                      <span key={role} className="text-xs px-2 py-0.5 rounded-full bg-bg border border-border text-text-muted font-body">
                        {role}
                      </span>
                    ))}
                  </div>
                </div>
                <ul className="space-y-2.5 mb-8 flex-1">
                  {plan.features.map((feat) => (
                    <li key={feat} className="flex items-start gap-2 text-sm text-text-muted font-body">
                      <svg className="w-4 h-4 text-primary mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                      </svg>
                      {feat}
                    </li>
                  ))}
                </ul>
                <button
                  className={`w-full py-3 rounded-xl font-heading font-semibold text-sm transition-all duration-200 ${
                    plan.highlighted
                      ? "bg-gradient-to-r from-orange-400 to-amber-500 text-black hover:shadow-lg hover:shadow-orange-500/25"
                      : "bg-bg border border-border text-text hover:border-primary/50 hover:text-primary"
                  }`}
                >
                  {plan.cta}
                </button>
              </motion.div>
            ))}
          </div>

          {/* Comparison Table */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.4 }}
          >
            <h2 className="text-2xl font-heading font-bold text-text text-center mb-8">
              Feature Comparison
            </h2>
            <div className="overflow-x-auto rounded-xl border border-border">
              <table className="w-full">
                <thead>
                  <tr className="bg-bg-card border-b border-border">
                    <th className="text-left px-6 py-4 text-sm font-heading font-semibold text-text">Feature</th>
                    <th className="text-center px-4 py-4 text-sm font-heading font-semibold text-emerald-400">SOLO</th>
                    <th className="text-center px-4 py-4 text-sm font-heading font-semibold text-orange-400">TEAM</th>
                    <th className="text-center px-4 py-4 text-sm font-heading font-semibold text-blue-400">MEDICAL</th>
                    <th className="text-center px-4 py-4 text-sm font-heading font-semibold text-red-400">TACTICAL</th>
                  </tr>
                </thead>
                <tbody>
                  {comparisonFeatures.map((row, i) => (
                    <tr key={row.feature} className={`border-b border-border/50 ${i % 2 === 0 ? "bg-bg" : "bg-bg-card/50"}`}>
                      <td className="px-6 py-3 text-sm text-text font-body">{row.feature}</td>
                      <td className="text-center px-4 py-3 text-sm text-text-muted font-mono">{row.solo}</td>
                      <td className="text-center px-4 py-3 text-sm text-text-muted font-mono">{row.team}</td>
                      <td className="text-center px-4 py-3 text-sm text-text-muted font-mono">{row.medical}</td>
                      <td className="text-center px-4 py-3 text-sm text-text-muted font-mono">{row.tactical}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </motion.div>
        </div>
      </section>
    </SiteLayout>
  );
}
