import React from 'react';
import { motion, useInView } from 'framer-motion';
import { useRef } from 'react';

/* ——————————————————————————————————————————————
   Shared animation helpers
   —————————————————————————————————————————————— */

function Section({ children, className = '' }) {
  const ref = useRef(null);
  const inView = useInView(ref, { once: true, margin: '-80px' });
  return (
    <motion.section
      ref={ref}
      initial={{ opacity: 0, y: 40 }}
      animate={inView ? { opacity: 1, y: 0 } : {}}
      transition={{ duration: 0.7, ease: [0.22, 1, 0.36, 1] }}
      className={className}
    >
      {children}
    </motion.section>
  );
}

function StaggerContainer({ children, className = '' }) {
  const ref = useRef(null);
  const inView = useInView(ref, { once: true, margin: '-60px' });
  return (
    <motion.div
      ref={ref}
      initial="hidden"
      animate={inView ? 'visible' : 'hidden'}
      variants={{
        hidden: {},
        visible: { transition: { staggerChildren: 0.08 } },
      }}
      className={className}
    >
      {children}
    </motion.div>
  );
}

const cardVariant = {
  hidden: { opacity: 0, y: 30 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.5, ease: [0.22, 1, 0.36, 1] } },
};

/* ——————————————————————————————————————————————
   Data
   —————————————————————————————————————————————— */

const modules = [
  { key: 'sleep', icon: '🌙', name: 'Sleep', desc: 'Analiza somnului prin accelerometru, faze REM/Deep/Light, scor zilnic.' },
  { key: 'energy', icon: '⚡', name: 'Energy & HRV', desc: 'Heart Rate Variability din cameră, nivel de energie, recomandări în timp real.' },
  { key: 'focus', icon: '🎯', name: 'Focus', desc: 'Sesiuni deep-work, blocare notificări, tracking screen-time și productivitate.' },
  { key: 'fitness', icon: '🏋️', name: 'Fitness', desc: 'GPS tracking, pași, distanță, workout-uri ghidate, volum și intensitate.' },
  { key: 'nutrition', icon: '🥗', name: 'Nutrition', desc: 'Photo-scan mâncare, macro/micro tracking, hidratare, meal planning AI.' },
  { key: 'brain', icon: '🧠', name: 'Brain', desc: 'Jocuri cognitive, memorie, reacție, neuro-scores, brain age estimation.' },
  { key: 'habits', icon: '🔄', name: 'Habits', desc: 'Habit stacking, streak-uri, micro-habits, rutine matinale și serale.' },
  { key: 'mood', icon: '💚', name: 'Mood', desc: 'Check-in emoțional, jurnal vocal, analiza sentiment, pattern recognition.' },
];

const timelinePoints = [
  { time: '06:30', module: '🌙', action: 'Wake-up analysis — sleep score calculat automat' },
  { time: '07:00', module: '⚡', action: 'Morning HRV scan — 30s camera measurement' },
  { time: '07:30', module: '🔄', action: 'Morning routine check — habits auto-tracked' },
  { time: '08:00', module: '🥗', action: 'Breakfast log — photo scan, macros calculated' },
  { time: '09:00', module: '🎯', action: 'Deep work session — Focus mode activated' },
  { time: '12:30', module: '🥗', action: 'Lunch log — AI nutrition suggestions' },
  { time: '14:00', module: '🧠', action: 'Brain training — 5 min cognitive games' },
  { time: '16:00', module: '🏋️', action: 'Workout — GPS tracking, real-time metrics' },
  { time: '17:30', module: '💚', action: 'Mood check-in — voice journaling available' },
  { time: '19:00', module: '🥗', action: 'Dinner log — daily macro summary' },
  { time: '21:00', module: '🔄', action: 'Evening routine — wind-down habits triggered' },
  { time: '22:30', module: '🌙', action: 'Sleep mode — phone sensors begin tracking' },
];

const plans = [
  {
    key: 'solo',
    name: 'SOLO',
    price: 'Free',
    period: '',
    desc: 'Individual health tracking with all 8 modules.',
    features: ['All 8 modules', 'Phone sensors', '7-day history', 'Basic insights', 'Local data only'],
    highlighted: false,
  },
  {
    key: 'team',
    name: 'TEAM',
    price: '$9.99',
    period: '/mo',
    desc: 'Team tracking with shared goals and trainer access.',
    features: ['Everything in SOLO', 'Team dashboards', 'Shared goals', 'Trainer access', '30-day history', 'Cloud sync'],
    highlighted: true,
  },
  {
    key: 'medical',
    name: 'MEDICAL',
    price: '$14.99',
    period: '/mo',
    desc: 'Clinical-grade tracking with physician integration.',
    features: ['Everything in TEAM', 'Physician portal', 'Psychologist access', 'Clinical reports', '1-year history', 'FHIR export'],
    highlighted: false,
  },
  {
    key: 'tactical',
    name: 'TACTICAL',
    price: '$19.99',
    period: '/mo',
    desc: 'Military & emergency services command overview.',
    features: ['Everything in MEDICAL', 'Command dashboard', 'Unit overview', 'Stress alerts', 'Unlimited history', 'Priority support'],
    highlighted: false,
  },
];

const sensors = [
  { sensor: 'Accelerometer', target: 'Sleep', explanation: 'Detectează mișcările din somn pentru analiza fazelor și calității somnului.' },
  { sensor: 'Camera', target: 'HRV', explanation: 'Măsoară variația ritmului cardiac prin analiza culorii pielii de pe deget.' },
  { sensor: 'GPS', target: 'Fitness', explanation: 'Tracking-ul traseului, distanței și vitezei pentru alergare și ciclism.' },
  { sensor: 'Step Counter', target: 'Steps', explanation: 'Numărarea pașilor zilnici și estimarea caloriilor arse din mers.' },
  { sensor: 'UsageStats', target: 'Screen Time', explanation: 'Monitorizarea timpului petrecut pe ecran și pattern-urile de utilizare.' },
  { sensor: 'Light Sensor', target: 'Night Use', explanation: 'Detectarea folosirii telefonului noaptea și impactul asupra somnului.' },
];

const careRoles = [
  { role: 'Doctor', icon: '🩺', position: 'top', desc: 'Acces la date clinice, rapoarte automate, alerte medicale.' },
  { role: 'Psychologist', icon: '🧠', position: 'right', desc: 'Mood patterns, jurnal emoțional, sesiuni programate.' },
  { role: 'Trainer', icon: '💪', position: 'bottom', desc: 'Fitness data, workout plans, obiective comune.' },
  { role: 'Commander', icon: '🎖️', position: 'left', desc: 'Overview echipă, readiness scores, alerte de stres.' },
];

const disconnectedApps = [
  { name: 'Sleep App', icon: '😴' },
  { name: 'Heart Rate', icon: '❤️' },
  { name: 'Focus Timer', icon: '⏱️' },
  { name: 'Step Counter', icon: '👟' },
  { name: 'Food Log', icon: '🍎' },
  { name: 'Brain Games', icon: '🧩' },
  { name: 'Habit Track', icon: '✅' },
  { name: 'Mood Diary', icon: '📔' },
];

/* ——————————————————————————————————————————————
   HERO Section
   —————————————————————————————————————————————— */

function Hero() {
  return (
    <section className="relative min-h-screen flex items-center justify-center overflow-hidden">
      {/* Gradient glow */}
      <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
        <div className="w-[800px] h-[600px] rounded-full bg-gradient-to-br from-primary/20 via-secondary/10 to-transparent blur-[120px] opacity-60" />
      </div>
      <div className="absolute top-1/4 -left-32 w-96 h-96 rounded-full bg-primary/5 blur-[100px]" />
      <div className="absolute bottom-1/4 -right-32 w-96 h-96 rounded-full bg-secondary/5 blur-[100px]" />

      {/* Grid overlay */}
      <div
        className="absolute inset-0 opacity-[0.03]"
        style={{
          backgroundImage:
            'linear-gradient(rgba(232,238,255,0.5) 1px, transparent 1px), linear-gradient(90deg, rgba(232,238,255,0.5) 1px, transparent 1px)',
          backgroundSize: '60px 60px',
        }}
      />

      <div className="relative z-10 max-w-5xl mx-auto px-6 text-center">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, ease: [0.22, 1, 0.36, 1] }}
        >
          <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full border border-primary/20 bg-primary/5 mb-8">
            <span className="w-2 h-2 rounded-full bg-primary animate-pulse" />
            <span className="text-sm font-mono text-primary">v2.0 — Now Available</span>
          </div>
        </motion.div>

        <motion.h1
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.1, ease: [0.22, 1, 0.36, 1] }}
          className="font-heading text-5xl sm:text-6xl md:text-7xl lg:text-8xl font-bold tracking-tight leading-[0.95] mb-6"
        >
          Your Complete{' '}
          <span className="bg-gradient-to-r from-primary via-secondary to-primary bg-[length:200%_auto] animate-[gradient_3s_linear_infinite] bg-clip-text text-transparent">
            Health Ecosystem
          </span>
        </motion.h1>

        <motion.p
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.2, ease: [0.22, 1, 0.36, 1] }}
          className="text-lg sm:text-xl text-text-muted max-w-2xl mx-auto mb-10 font-body leading-relaxed"
        >
          8 module. Senzori reali. Zero input manual.
          <br />
          <span className="text-text-dim">Totul într-o singură aplicație care chiar înțelege cum te simți.</span>
        </motion.p>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.35, ease: [0.22, 1, 0.36, 1] }}
          className="flex flex-col sm:flex-row items-center justify-center gap-4"
        >
          <a
            href="/download"
            className="px-8 py-3.5 rounded-xl bg-primary text-[#03050A] font-heading font-semibold text-base hover:bg-primary-hover transition-all duration-200 hover:shadow-[0_0_30px_rgba(0,229,160,0.3)]"
          >
            Download Free
          </a>
          <a
            href="/features"
            className="px-8 py-3.5 rounded-xl border border-border text-text font-heading font-semibold text-base hover:border-text-muted hover:bg-bg-card transition-all duration-200"
          >
            Explore Features
          </a>
        </motion.div>

        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 1, delay: 0.6 }}
          className="mt-16 flex items-center justify-center gap-8 text-text-dim text-sm font-mono"
        >
          <span>8 Modules</span>
          <span className="w-1 h-1 rounded-full bg-text-dim" />
          <span>6 Sensors</span>
          <span className="w-1 h-1 rounded-full bg-text-dim" />
          <span>0 Manual Input</span>
        </motion.div>
      </div>

      {/* Scroll indicator */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 1.2 }}
        className="absolute bottom-8 left-1/2 -translate-x-1/2"
      >
        <motion.div
          animate={{ y: [0, 8, 0] }}
          transition={{ duration: 2, repeat: Infinity }}
          className="w-5 h-8 rounded-full border border-text-dim flex items-start justify-center p-1"
        >
          <div className="w-1 h-2 rounded-full bg-text-muted" />
        </motion.div>
      </motion.div>
    </section>
  );
}

/* ——————————————————————————————————————————————
   PROBLEM Section
   —————————————————————————————————————————————— */

function ProblemSection() {
  return (
    <Section className="py-24 sm:py-32 px-6">
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-16">
          <span className="text-sm font-mono text-primary mb-4 block tracking-wider uppercase">The Problem</span>
          <h2 className="font-heading text-3xl sm:text-4xl md:text-5xl font-bold mb-4">
            Aplicațiile tale de sănătate
            <br />
            <span className="text-text-muted">nu vorbesc între ele</span>
          </h2>
        </div>

        <div className="grid md:grid-cols-2 gap-12 md:gap-16 items-center">
          {/* Disconnected apps */}
          <div className="relative">
            <div className="text-center mb-6">
              <span className="text-sm font-mono text-danger tracking-wider uppercase">Before — 8 Separate Apps</span>
            </div>
            <StaggerContainer className="grid grid-cols-4 gap-4">
              {disconnectedApps.map((app) => (
                <motion.div
                  key={app.name}
                  variants={cardVariant}
                  className="aspect-square rounded-xl bg-bg-card border border-border flex flex-col items-center justify-center gap-2 p-3 opacity-60"
                >
                  <span className="text-2xl">{app.icon}</span>
                  <span className="text-[10px] text-text-dim font-mono text-center leading-tight">{app.name}</span>
                </motion.div>
              ))}
            </StaggerContainer>
            {/* Disconnection lines */}
            <div className="absolute inset-0 pointer-events-none flex items-center justify-center">
              <svg className="w-full h-full absolute" viewBox="0 0 300 300">
                <line x1="75" y1="75" x2="150" y2="150" stroke="#FF4D6A" strokeWidth="1" strokeDasharray="4 4" opacity="0.3" />
                <line x1="225" y1="75" x2="150" y2="150" stroke="#FF4D6A" strokeWidth="1" strokeDasharray="4 4" opacity="0.3" />
                <line x1="75" y1="225" x2="150" y2="150" stroke="#FF4D6A" strokeWidth="1" strokeDasharray="4 4" opacity="0.3" />
                <line x1="225" y1="225" x2="150" y2="150" stroke="#FF4D6A" strokeWidth="1" strokeDasharray="4 4" opacity="0.3" />
              </svg>
            </div>
          </div>

          {/* VitaNova unified */}
          <div className="relative">
            <div className="text-center mb-6">
              <span className="text-sm font-mono text-primary tracking-wider uppercase">After — VitaNova</span>
            </div>
            <div className="flex items-center justify-center">
              <div className="relative w-64 h-64 sm:w-72 sm:h-72">
                {/* Outer ring */}
                <div className="absolute inset-0 rounded-full border border-primary/20" />
                {/* Middle ring */}
                <div className="absolute inset-6 rounded-full border border-primary/30" />
                {/* Inner glow */}
                <div className="absolute inset-12 rounded-full bg-gradient-to-br from-primary/10 to-secondary/10 backdrop-blur-sm border border-primary/40 flex items-center justify-center">
                  <div className="text-center">
                    <span className="font-heading font-bold text-xl text-primary">VitaNova</span>
                    <span className="block text-xs text-text-muted mt-1">Unified</span>
                  </div>
                </div>
                {/* Module icons around the circle */}
                {modules.map((mod, i) => {
                  const angle = (i * 360) / modules.length - 90;
                  const rad = (angle * Math.PI) / 180;
                  const r = 120;
                  const x = 50 + (r / 144) * 50 * Math.cos(rad);
                  const y = 50 + (r / 144) * 50 * Math.sin(rad);
                  return (
                    <div
                      key={mod.key}
                      className="absolute w-10 h-10 rounded-full bg-bg-card border border-primary/30 flex items-center justify-center text-lg -translate-x-1/2 -translate-y-1/2"
                      style={{ left: `${x}%`, top: `${y}%` }}
                    >
                      {mod.icon}
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
      </div>
    </Section>
  );
}

/* ——————————————————————————————————————————————
   MODULES Section
   —————————————————————————————————————————————— */

function ModulesSection() {
  return (
    <Section className="py-24 sm:py-32 px-6">
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-16">
          <span className="text-sm font-mono text-primary mb-4 block tracking-wider uppercase">8 Modules</span>
          <h2 className="font-heading text-3xl sm:text-4xl md:text-5xl font-bold mb-4">
            Everything You Need.
            <br />
            <span className="text-text-muted">Nothing You Don't.</span>
          </h2>
        </div>

        <StaggerContainer className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {modules.map((mod) => (
            <motion.div
              key={mod.key}
              variants={cardVariant}
              whileHover={{ y: -6, transition: { duration: 0.2 } }}
              className="group relative rounded-2xl bg-bg-card border border-border p-6 hover:border-primary/30 transition-all duration-300 cursor-default overflow-hidden"
            >
              {/* Hover glow */}
              <div className="absolute inset-0 bg-gradient-to-br from-primary/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
              <div className="relative z-10">
                <span className="text-3xl mb-4 block">{mod.icon}</span>
                <h3 className="font-heading font-semibold text-lg text-text mb-2">{mod.name}</h3>
                <p className="text-sm text-text-muted leading-relaxed font-body">{mod.desc}</p>
              </div>
              {/* Expand indicator */}
              <div className="relative z-10 mt-4 flex items-center gap-1.5 text-primary text-xs font-mono opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                <span>Learn more</span>
                <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </div>
            </motion.div>
          ))}
        </StaggerContainer>
      </div>
    </Section>
  );
}

/* ——————————————————————————————————————————————
   PROTOCOL 24H Section
   —————————————————————————————————————————————— */

function ProtocolSection() {
  return (
    <Section className="py-24 sm:py-32 px-6">
      <div className="max-w-4xl mx-auto">
        <div className="text-center mb-16">
          <span className="text-sm font-mono text-primary mb-4 block tracking-wider uppercase">Protocol 24H</span>
          <h2 className="font-heading text-3xl sm:text-4xl md:text-5xl font-bold mb-4">
            O zi completă.
            <br />
            <span className="text-text-muted">Zero efort.</span>
          </h2>
          <p className="text-text-muted max-w-lg mx-auto font-body">
            VitaNova lucrează în background de la wake-up la sleep, colectând date automat și oferind insights personalizate.
          </p>
        </div>

        <div className="relative">
          {/* Vertical line */}
          <div className="absolute left-6 sm:left-8 top-0 bottom-0 w-px bg-gradient-to-b from-primary/50 via-secondary/30 to-primary/50" />

          <StaggerContainer className="space-y-0">
            {timelinePoints.map((point, i) => (
              <motion.div
                key={i}
                variants={cardVariant}
                className="relative flex items-start gap-6 sm:gap-8 group py-4"
              >
                {/* Dot */}
                <div className="relative z-10 flex-shrink-0 w-12 sm:w-16 flex items-center justify-center">
                  <div className="w-3 h-3 rounded-full bg-bg border-2 border-primary group-hover:bg-primary group-hover:shadow-[0_0_12px_rgba(0,229,160,0.5)] transition-all duration-300" />
                </div>

                {/* Content */}
                <div className="flex-1 -mt-1 pb-6 border-b border-border/50 group-last:border-0">
                  <div className="flex items-center gap-3 mb-1">
                    <span className="font-mono text-sm text-primary font-semibold">{point.time}</span>
                    <span className="text-lg">{point.module}</span>
                  </div>
                  <p className="text-sm text-text-muted font-body leading-relaxed">{point.action}</p>
                </div>
              </motion.div>
            ))}
          </StaggerContainer>
        </div>
      </div>
    </Section>
  );
}

/* ——————————————————————————————————————————————
   PLANS Section
   —————————————————————————————————————————————— */

function PlansSection() {
  return (
    <Section className="py-24 sm:py-32 px-6">
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-16">
          <span className="text-sm font-mono text-primary mb-4 block tracking-wider uppercase">Plans</span>
          <h2 className="font-heading text-3xl sm:text-4xl md:text-5xl font-bold mb-4">
            Simple, Transparent Pricing
          </h2>
          <p className="text-text-muted max-w-lg mx-auto font-body">
            Start free. Upgrade when you need team or clinical features.
          </p>
        </div>

        <StaggerContainer className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          {plans.map((plan) => (
            <motion.div
              key={plan.key}
              variants={cardVariant}
              whileHover={{ y: -4, transition: { duration: 0.2 } }}
              className={`relative rounded-2xl p-6 flex flex-col ${
                plan.highlighted
                  ? 'bg-gradient-to-b from-primary/10 to-bg-card border-2 border-primary/40 shadow-[0_0_40px_rgba(0,229,160,0.1)]'
                  : 'bg-bg-card border border-border hover:border-border-hover'
              } transition-all duration-300`}
            >
              {plan.highlighted && (
                <div className="absolute -top-3 left-1/2 -translate-x-1/2 px-3 py-0.5 rounded-full bg-primary text-[#03050A] text-xs font-heading font-bold tracking-wider uppercase">
                  Recommended
                </div>
              )}

              <div className="mb-6">
                <h3 className="font-heading font-bold text-lg text-text tracking-wider mb-1">{plan.name}</h3>
                <p className="text-sm text-text-dim font-body">{plan.desc}</p>
              </div>

              <div className="mb-6">
                <span className="font-heading text-4xl font-bold text-text">{plan.price}</span>
                {plan.period && <span className="text-text-muted font-body text-sm">{plan.period}</span>}
              </div>

              <ul className="space-y-2.5 mb-8 flex-1">
                {plan.features.map((f, j) => (
                  <li key={j} className="flex items-start gap-2.5 text-sm">
                    <svg className="w-4 h-4 mt-0.5 text-primary flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    <span className="text-text-muted font-body">{f}</span>
                  </li>
                ))}
              </ul>

              <a
                href="/download"
                className={`block text-center py-2.5 rounded-xl font-heading font-semibold text-sm transition-all duration-200 ${
                  plan.highlighted
                    ? 'bg-primary text-[#03050A] hover:bg-primary-hover hover:shadow-[0_0_20px_rgba(0,229,160,0.3)]'
                    : 'border border-border text-text hover:border-text-muted hover:bg-bg-hover'
                }`}
              >
                {plan.price === 'Free' ? 'Get Started' : 'Subscribe'}
              </a>
            </motion.div>
          ))}
        </StaggerContainer>
      </div>
    </Section>
  );
}

/* ——————————————————————————————————————————————
   REAL SENSORS Section
   —————————————————————————————————————————————— */

function SensorsSection() {
  return (
    <Section className="py-24 sm:py-32 px-6">
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-16">
          <span className="text-sm font-mono text-primary mb-4 block tracking-wider uppercase">Real Sensors</span>
          <h2 className="font-heading text-3xl sm:text-4xl md:text-5xl font-bold mb-4">
            Nu promitem.{' '}
            <span className="bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">Măsurăm.</span>
          </h2>
          <p className="text-text-muted max-w-lg mx-auto font-body">
            Fiecare modul folosește senzori reali din telefonul tău. Zero self-report, zero guesswork.
          </p>
        </div>

        <StaggerContainer className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {sensors.map((s, i) => (
            <motion.div
              key={i}
              variants={cardVariant}
              whileHover={{ y: -4, transition: { duration: 0.2 } }}
              className="group rounded-2xl bg-bg-card border border-border p-6 hover:border-primary/30 transition-all duration-300"
            >
              <div className="flex items-center gap-3 mb-4">
                <div className="px-3 py-1 rounded-lg bg-primary/10 border border-primary/20">
                  <span className="font-mono text-xs text-primary font-semibold">{s.sensor}</span>
                </div>
                <svg className="w-4 h-4 text-text-dim" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
                </svg>
                <div className="px-3 py-1 rounded-lg bg-secondary/10 border border-secondary/20">
                  <span className="font-mono text-xs text-secondary font-semibold">{s.target}</span>
                </div>
              </div>
              <p className="text-sm text-text-muted font-body leading-relaxed">{s.explanation}</p>
            </motion.div>
          ))}
        </StaggerContainer>
      </div>
    </Section>
  );
}

/* ——————————————————————————————————————————————
   CARE CIRCLE Section
   —————————————————————————————————————————————— */

function CareCircleSection() {
  const positions = {
    top: { x: '50%', y: '5%', translate: '-translate-x-1/2' },
    right: { x: '85%', y: '50%', translate: '-translate-y-1/2' },
    bottom: { x: '50%', y: '85%', translate: '-translate-x-1/2' },
    left: { x: '5%', y: '50%', translate: '-translate-y-1/2' },
  };

  return (
    <Section className="py-24 sm:py-32 px-6">
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-16">
          <span className="text-sm font-mono text-primary mb-4 block tracking-wider uppercase">Care Circle</span>
          <h2 className="font-heading text-3xl sm:text-4xl md:text-5xl font-bold mb-4">
            Your Health Team.
            <br />
            <span className="text-text-muted">Connected.</span>
          </h2>
          <p className="text-text-muted max-w-lg mx-auto font-body">
            Share specific data with your care providers. You control exactly what each person sees.
          </p>
        </div>

        <div className="grid md:grid-cols-2 gap-12 items-center">
          {/* Diagram */}
          <div className="relative aspect-square max-w-md mx-auto w-full">
            {/* Connecting lines */}
            <svg className="absolute inset-0 w-full h-full" viewBox="0 0 400 400">
              <line x1="200" y1="200" x2="200" y2="40" stroke="url(#lineGrad)" strokeWidth="1" opacity="0.4" />
              <line x1="200" y1="200" x2="360" y2="200" stroke="url(#lineGrad)" strokeWidth="1" opacity="0.4" />
              <line x1="200" y1="200" x2="200" y2="360" stroke="url(#lineGrad)" strokeWidth="1" opacity="0.4" />
              <line x1="200" y1="200" x2="40" y2="200" stroke="url(#lineGrad)" strokeWidth="1" opacity="0.4" />
              <defs>
                <linearGradient id="lineGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                  <stop offset="0%" stopColor="#00E5A0" />
                  <stop offset="100%" stopColor="#00D4FF" />
                </linearGradient>
              </defs>
              {/* Data flow dots */}
              <circle r="3" fill="#00E5A0" opacity="0.8">
                <animateMotion dur="3s" repeatCount="indefinite" path="M200,200 L200,40" />
              </circle>
              <circle r="3" fill="#00D4FF" opacity="0.8">
                <animateMotion dur="3.5s" repeatCount="indefinite" path="M200,200 L360,200" />
              </circle>
              <circle r="3" fill="#00E5A0" opacity="0.8">
                <animateMotion dur="4s" repeatCount="indefinite" path="M200,200 L200,360" />
              </circle>
              <circle r="3" fill="#00D4FF" opacity="0.8">
                <animateMotion dur="3.2s" repeatCount="indefinite" path="M200,200 L40,200" />
              </circle>
            </svg>

            {/* Center user */}
            <div className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-20 h-20 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 border-2 border-primary/40 flex items-center justify-center shadow-[0_0_30px_rgba(0,229,160,0.2)]">
              <div className="text-center">
                <span className="text-2xl">👤</span>
                <span className="block text-[10px] font-mono text-primary mt-0.5">YOU</span>
              </div>
            </div>

            {/* Role nodes */}
            {careRoles.map((role) => {
              const pos = positions[role.position];
              return (
                <div
                  key={role.role}
                  className={`absolute ${pos.translate}`}
                  style={{ left: pos.x, top: pos.y }}
                >
                  <div className="w-16 h-16 rounded-full bg-bg-card border border-border hover:border-primary/40 flex flex-col items-center justify-center transition-all duration-300 cursor-default">
                    <span className="text-xl">{role.icon}</span>
                    <span className="text-[9px] font-mono text-text-muted mt-0.5">{role.role}</span>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Role descriptions */}
          <StaggerContainer className="space-y-4">
            {careRoles.map((role) => (
              <motion.div
                key={role.role}
                variants={cardVariant}
                className="flex items-start gap-4 p-4 rounded-xl bg-bg-card border border-border hover:border-border-hover transition-colors duration-200"
              >
                <span className="text-2xl flex-shrink-0 mt-0.5">{role.icon}</span>
                <div>
                  <h4 className="font-heading font-semibold text-text mb-1">{role.role}</h4>
                  <p className="text-sm text-text-muted font-body">{role.desc}</p>
                </div>
              </motion.div>
            ))}
          </StaggerContainer>
        </div>
      </div>
    </Section>
  );
}

/* ——————————————————————————————————————————————
   CTA FINAL Section
   —————————————————————————————————————————————— */

function CtaSection() {
  return (
    <Section className="py-24 sm:py-32 px-6">
      <div className="max-w-4xl mx-auto text-center relative">
        {/* Background glow */}
        <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
          <div className="w-[500px] h-[300px] rounded-full bg-gradient-to-r from-primary/15 to-secondary/15 blur-[80px]" />
        </div>

        <div className="relative z-10">
          <h2 className="font-heading text-3xl sm:text-4xl md:text-5xl font-bold mb-4">
            Start Your{' '}
            <span className="bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">Journey</span>
          </h2>
          <p className="text-text-muted max-w-lg mx-auto font-body mb-10 text-lg">
            Download VitaNova free. No credit card. No account required. Just your phone and the will to understand yourself better.
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <a
              href="/download"
              className="inline-flex items-center gap-3 px-8 py-4 rounded-xl bg-primary text-[#03050A] font-heading font-semibold text-base hover:bg-primary-hover transition-all duration-200 hover:shadow-[0_0_30px_rgba(0,229,160,0.3)]"
            >
              <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                <path d="M3 20.5v-17c0-.83.67-1.5 1.5-1.5S6 2.67 6 3.5v17c0 .83-.67 1.5-1.5 1.5S3 21.33 3 20.5zM16.03 3.97L7 12l9.03 8.03c.49.44 1.24.08 1.24-.58v-15.9c0-.66-.75-1.02-1.24-.58z" />
              </svg>
              Download for Android
            </a>
            <a
              href="/plans"
              className="px-8 py-4 rounded-xl border border-border text-text font-heading font-semibold text-base hover:border-text-muted hover:bg-bg-card transition-all duration-200"
            >
              View Plans
            </a>
          </div>

          <p className="text-text-dim text-sm font-mono mt-8">
            Android 8.0+ required &middot; ~25MB &middot; No ads, ever.
          </p>
        </div>
      </div>
    </Section>
  );
}

/* ——————————————————————————————————————————————
   PAGE EXPORT
   —————————————————————————————————————————————— */

export default function PromoHome() {
  return (
    <>
      <Hero />
      <ProblemSection />
      <ModulesSection />
      <ProtocolSection />
      <PlansSection />
      <SensorsSection />
      <CareCircleSection />
      <CtaSection />
    </>
  );
}
