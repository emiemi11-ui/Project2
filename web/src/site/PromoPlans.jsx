import SiteLayout from "./SiteLayout";
import { motion, useInView } from "framer-motion";
import { useRef, useState } from "react";

const plans = [
  {
    name: "SOLO",
    subtitle: "Personal Health Tracking",
    price: "Free",
    period: "",
    color: "from-emerald-400 to-teal-500",
    border: "border-emerald-500/30",
    glow: "hover:shadow-emerald-500/20",
    accent: "text-emerald-400",
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
    glow: "hover:shadow-orange-500/25",
    accent: "text-orange-400",
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
    glow: "hover:shadow-blue-500/20",
    accent: "text-blue-400",
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
    glow: "hover:shadow-red-500/20",
    accent: "text-red-400",
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

const faqs = [
  { q: "Is there a free trial for paid plans?", a: "Yes — TEAM and MEDICAL plans include a 14-day free trial with full access to all features. No credit card required." },
  { q: "Can I switch plans later?", a: "Absolutely. Upgrade or downgrade at any time. Your data stays intact across plan changes." },
  { q: "Is my data safe?", a: "All data is stored locally on your device. Shared data uses end-to-end encryption. We never sell or monetize your health data." },
  { q: "Do I need internet access?", a: "VitaNova works fully offline. Internet is only needed for syncing data with team members or specialists." },
];

function ScrollReveal({ children, className = "", delay = 0 }) {
  const ref = useRef(null);
  const inView = useInView(ref, { once: true, margin: "-80px" });
  return (
    <motion.div
      ref={ref}
      initial={{ opacity: 0, y: 30 }}
      animate={inView ? { opacity: 1, y: 0 } : {}}
      transition={{ duration: 0.6, delay, ease: [0.22, 1, 0.36, 1] }}
      className={className}
    >
      {children}
    </motion.div>
  );
}

export default function PromoPlans() {
  const [openFaq, setOpenFaq] = useState(null);
  const [billingAnnual, setBillingAnnual] = useState(false);

  return (
    <SiteLayout>
      <section className="pt-32 pb-20 px-6">
        <div className="max-w-7xl mx-auto">
          {/* Header */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="text-center mb-10"
          >
            <h1 className="text-4xl md:text-5xl font-heading font-bold text-text mb-4">
              Choose Your <span className="text-primary">Plan</span>
            </h1>
            <p className="text-lg text-text-muted max-w-2xl mx-auto font-body">
              From personal health tracking to tactical military operations.
              Every plan includes our complete 8-module ecosystem.
            </p>
          </motion.div>

          {/* Billing toggle */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.3 }}
            className="flex items-center justify-center gap-3 mb-12"
          >
            <span className={`text-sm font-body ${!billingAnnual ? "text-text" : "text-text-dim"}`}>Monthly</span>
            <button
              onClick={() => setBillingAnnual(!billingAnnual)}
              className={`relative w-14 h-7 rounded-full transition-colors ${billingAnnual ? "bg-primary" : "bg-border"}`}
            >
              <motion.div
                animate={{ x: billingAnnual ? 28 : 2 }}
                transition={{ type: "spring", stiffness: 500, damping: 30 }}
                className="absolute top-1 w-5 h-5 rounded-full bg-white shadow-md"
              />
            </button>
            <span className={`text-sm font-body ${billingAnnual ? "text-text" : "text-text-dim"}`}>
              Annual <span className="text-primary text-xs font-semibold ml-1">Save 20%</span>
            </span>
          </motion.div>

          {/* Plan Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6 mb-24">
            {plans.map((plan, i) => (
              <motion.div
                key={plan.name}
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5, delay: i * 0.1 }}
                whileHover={{ y: -8, transition: { duration: 0.25 } }}
                className={`relative rounded-2xl border ${plan.border} bg-bg-card p-6 flex flex-col transition-shadow duration-300 hover:shadow-xl ${plan.glow} ${
                  plan.highlighted ? "ring-2 ring-orange-500/50" : ""
                }`}
              >
                {plan.highlighted && (
                  <motion.div
                    initial={{ scale: 0.8, opacity: 0 }}
                    animate={{ scale: 1, opacity: 1 }}
                    transition={{ delay: 0.5, type: "spring" }}
                    className="absolute -top-3 left-1/2 -translate-x-1/2 px-4 py-1 rounded-full bg-gradient-to-r from-orange-400 to-amber-500 text-xs font-bold text-black uppercase tracking-wider"
                  >
                    Most Popular
                  </motion.div>
                )}
                <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${plan.color} flex items-center justify-center mb-4`}>
                  <span className="text-lg font-heading font-bold text-white">{plan.name[0]}</span>
                </div>
                <h3 className="text-xl font-heading font-bold text-text mb-1">{plan.name}</h3>
                <p className="text-sm text-text-muted mb-4 font-body">{plan.subtitle}</p>
                <div className="mb-6">
                  <span className="text-3xl font-heading font-bold text-text">
                    {plan.price === "Free" || plan.price === "Custom"
                      ? plan.price
                      : billingAnnual
                        ? `€${(parseFloat(plan.price.replace("€", "")) * 0.8).toFixed(2)}`
                        : plan.price}
                  </span>
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
                  {plan.features.map((feat, fi) => (
                    <motion.li
                      key={feat}
                      initial={{ opacity: 0, x: -10 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: 0.3 + i * 0.1 + fi * 0.03 }}
                      className="flex items-start gap-2 text-sm text-text-muted font-body"
                    >
                      <svg className={`w-4 h-4 ${plan.accent} mt-0.5 flex-shrink-0`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                      </svg>
                      {feat}
                    </motion.li>
                  ))}
                </ul>
                <motion.button
                  whileHover={{ scale: 1.03 }}
                  whileTap={{ scale: 0.97 }}
                  className={`w-full py-3 rounded-xl font-heading font-semibold text-sm transition-all duration-200 ${
                    plan.highlighted
                      ? "bg-gradient-to-r from-orange-400 to-amber-500 text-black hover:shadow-lg hover:shadow-orange-500/25"
                      : "bg-bg border border-border text-text hover:border-primary/50 hover:text-primary"
                  }`}
                >
                  {plan.cta}
                </motion.button>
              </motion.div>
            ))}
          </div>

          {/* Comparison Table */}
          <ScrollReveal className="mb-24">
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
                    <motion.tr
                      key={row.feature}
                      initial={{ opacity: 0, x: -10 }}
                      whileInView={{ opacity: 1, x: 0 }}
                      viewport={{ once: true }}
                      transition={{ delay: i * 0.03 }}
                      className={`border-b border-border/50 ${i % 2 === 0 ? "bg-bg" : "bg-bg-card/50"} hover:bg-primary/5 transition-colors`}
                    >
                      <td className="px-6 py-3 text-sm text-text font-body">{row.feature}</td>
                      <td className="text-center px-4 py-3 text-sm text-text-muted font-mono">
                        <span className={row.solo === "✓" ? "text-emerald-400" : row.solo === "—" ? "text-text-dim" : ""}>{row.solo}</span>
                      </td>
                      <td className="text-center px-4 py-3 text-sm text-text-muted font-mono">
                        <span className={row.team === "✓" ? "text-orange-400" : row.team === "—" ? "text-text-dim" : ""}>{row.team}</span>
                      </td>
                      <td className="text-center px-4 py-3 text-sm text-text-muted font-mono">
                        <span className={row.medical === "✓" ? "text-blue-400" : row.medical === "—" ? "text-text-dim" : ""}>{row.medical}</span>
                      </td>
                      <td className="text-center px-4 py-3 text-sm text-text-muted font-mono">
                        <span className={row.tactical === "✓" ? "text-red-400" : row.tactical === "—" ? "text-text-dim" : ""}>{row.tactical}</span>
                      </td>
                    </motion.tr>
                  ))}
                </tbody>
              </table>
            </div>
          </ScrollReveal>

          {/* FAQ Section */}
          <ScrollReveal delay={0.1}>
            <h2 className="text-2xl font-heading font-bold text-text text-center mb-8">
              Frequently Asked Questions
            </h2>
            <div className="max-w-2xl mx-auto space-y-3">
              {faqs.map((faq, i) => (
                <motion.div
                  key={i}
                  initial={{ opacity: 0, y: 10 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ delay: i * 0.05 }}
                  className="rounded-xl border border-border bg-bg-card overflow-hidden"
                >
                  <button
                    onClick={() => setOpenFaq(openFaq === i ? null : i)}
                    className="w-full flex items-center justify-between p-4 text-left hover:bg-primary/5 transition-colors"
                  >
                    <span className="text-sm font-heading font-semibold text-text">{faq.q}</span>
                    <motion.svg
                      animate={{ rotate: openFaq === i ? 180 : 0 }}
                      transition={{ duration: 0.2 }}
                      className="w-4 h-4 text-text-dim flex-shrink-0 ml-3"
                      fill="none" stroke="currentColor" viewBox="0 0 24 24"
                    >
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </motion.svg>
                  </button>
                  {openFaq === i && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: "auto", opacity: 1 }}
                      transition={{ duration: 0.25 }}
                      className="overflow-hidden"
                    >
                      <div className="px-4 pb-4 text-sm text-text-muted font-body leading-relaxed border-t border-border/50 pt-3">
                        {faq.a}
                      </div>
                    </motion.div>
                  )}
                </motion.div>
              ))}
            </div>
          </ScrollReveal>

          {/* CTA Banner */}
          <ScrollReveal delay={0.15} className="mt-20">
            <motion.div
              whileHover={{ scale: 1.01 }}
              className="rounded-2xl bg-gradient-to-r from-primary/10 to-secondary/10 border border-primary/20 p-8 sm:p-12 text-center"
            >
              <h2 className="text-2xl sm:text-3xl font-heading font-bold text-text mb-3">
                Ready to get started?
              </h2>
              <p className="text-text-muted font-body mb-6 max-w-lg mx-auto">
                Download VitaNova for free and experience the complete health ecosystem. Upgrade anytime.
              </p>
              <motion.button
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                className="px-8 py-3 rounded-xl bg-gradient-to-r from-primary to-secondary text-black font-heading font-bold text-sm hover:shadow-lg hover:shadow-primary/25 transition-shadow"
              >
                Download Free
              </motion.button>
            </motion.div>
          </ScrollReveal>
        </div>
      </section>
    </SiteLayout>
  );
}
