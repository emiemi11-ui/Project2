import SiteLayout from "./SiteLayout";
import { motion, useInView } from "framer-motion";
import { useRef, useEffect, useState } from "react";

const principles = [
  {
    icon: "🔬",
    title: "Sensor-First, Not Self-Report",
    desc: "Real biometric data from real sensors. No guessing, no surveys, no manual input. Your phone's accelerometer, camera PPG, and GPS tell the truth.",
  },
  {
    icon: "🧠",
    title: "8 Modules, One Ecosystem",
    desc: "Sleep, Energy, Focus, Fitness, Nutrition, Brain, Habits, and Mood — all interconnected. Your sleep quality affects your readiness score, which affects your training recommendations.",
  },
  {
    icon: "🔒",
    title: "Privacy by Design",
    desc: "Your data stays on your device. You choose what to share, with whom, through the Privacy Matrix. No cloud, no ads, no selling data.",
  },
  {
    icon: "🎯",
    title: "Elastic Streaks, Not Guilt",
    desc: "Miss a day? Your streak bends, not breaks. Recovery Mode activates. We optimize for long-term consistency, not perfectionism.",
  },
  {
    icon: "⏰",
    title: "Circadian Intelligence",
    desc: "Every recommendation is timed to your biological clock. Optimal HRV measurement windows, ideal workout times, sleep cycle alignment — all personalized.",
  },
  {
    icon: "👥",
    title: "Multi-Role Architecture",
    desc: "7 roles, 7 different experiences. Commander sees readiness heatmaps. Physician sees vitals. Psychologist sees stress patterns. Each role gets exactly what they need.",
  },
];

const team = [
  {
    name: "Dr. Adrian Popescu",
    role: "Founder & Lead Architect",
    bio: "Former military physician turned health-tech innovator. 15 years of experience in operational health monitoring.",
    avatar: "AP",
    color: "from-emerald-400 to-teal-500",
  },
  {
    name: "Maria Ionescu",
    role: "Head of Product",
    bio: "Sports science PhD with a passion for bridging the gap between research and consumer products.",
    avatar: "MI",
    color: "from-orange-400 to-amber-500",
  },
  {
    name: "Andrei Dragomir",
    role: "Lead Engineer",
    bio: "Full-stack architect with deep expertise in sensor integration, real-time data processing, and mobile development.",
    avatar: "AD",
    color: "from-blue-400 to-indigo-500",
  },
  {
    name: "Elena Constantinescu",
    role: "UX & Design Lead",
    bio: "Designed health interfaces for 3 national health services. Believes data visualization should be beautiful and actionable.",
    avatar: "EC",
    color: "from-purple-400 to-pink-500",
  },
];

const howItWorks = [
  { step: "1", title: "Sensors Collect", desc: "Your phone's built-in sensors (accelerometer, gyroscope, camera, GPS) continuously gather biometric data." },
  { step: "2", title: "Algorithms Analyze", desc: "On-device algorithms (HRV analysis, sleep staging, ACWR calculation) transform raw data into actionable metrics." },
  { step: "3", title: "Insights Emerge", desc: "Your personal dashboard shows readiness scores, sleep quality, stress levels, and cognitive performance — all in real time." },
  { step: "4", title: "Specialists Act", desc: "With your consent, trainers, physicians, and psychologists see relevant metrics to provide personalized guidance." },
];

const stats = [
  { value: 8, label: "Health Modules", suffix: "" },
  { value: 7, label: "Sensor Types", suffix: "" },
  { value: 7, label: "User Roles", suffix: "" },
  { value: 100, label: "Privacy Score", suffix: "%" },
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

function AnimatedCounter({ value, suffix = "" }) {
  const ref = useRef(null);
  const inView = useInView(ref, { once: true });
  const [display, setDisplay] = useState(0);

  useEffect(() => {
    if (!inView) return;
    let start = 0;
    const duration = 1500;
    const startTime = Date.now();
    const tick = () => {
      const elapsed = Date.now() - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      start = Math.round(eased * value);
      setDisplay(start);
      if (progress < 1) requestAnimationFrame(tick);
    };
    requestAnimationFrame(tick);
  }, [inView, value]);

  return <span ref={ref}>{display}{suffix}</span>;
}

export default function PromoAbout() {
  return (
    <SiteLayout>
      <section className="pt-32 pb-20 px-6">
        <div className="max-w-5xl mx-auto">
          {/* Header */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="text-center mb-20"
          >
            <h1 className="text-4xl md:text-5xl font-heading font-bold text-text mb-4">
              The NEXUS <span className="text-primary">Philosophy</span>
            </h1>
            <p className="text-lg text-text-muted max-w-2xl mx-auto font-body">
              VitaNova is built on six core principles that guide every feature,
              every algorithm, and every pixel.
            </p>
          </motion.div>

          {/* Animated Stats Counter */}
          <ScrollReveal className="mb-20">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              {stats.map((stat, i) => (
                <motion.div
                  key={stat.label}
                  initial={{ opacity: 0, scale: 0.8 }}
                  whileInView={{ opacity: 1, scale: 1 }}
                  viewport={{ once: true }}
                  transition={{ delay: i * 0.1, type: "spring" }}
                  whileHover={{ scale: 1.05, y: -4 }}
                  className="rounded-xl border border-border bg-bg-card p-6 text-center hover:border-primary/30 transition-colors"
                >
                  <div className="text-3xl sm:text-4xl font-heading font-bold text-primary mb-1">
                    <AnimatedCounter value={stat.value} suffix={stat.suffix} />
                  </div>
                  <div className="text-xs text-text-muted font-mono uppercase tracking-wider">{stat.label}</div>
                </motion.div>
              ))}
            </div>
          </ScrollReveal>

          {/* Principles */}
          <ScrollReveal className="mb-24">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {principles.map((p, i) => (
                <motion.div
                  key={p.title}
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ duration: 0.4, delay: i * 0.08 }}
                  whileHover={{ y: -4, transition: { duration: 0.2 } }}
                  className="rounded-xl border border-border bg-bg-card p-6 hover:border-primary/30 hover:shadow-lg hover:shadow-primary/5 transition-all duration-300"
                >
                  <div className="text-2xl mb-3">{p.icon}</div>
                  <h3 className="text-lg font-heading font-bold text-text mb-2">{p.title}</h3>
                  <p className="text-sm text-text-muted font-body leading-relaxed">{p.desc}</p>
                </motion.div>
              ))}
            </div>
          </ScrollReveal>

          {/* How It Works */}
          <ScrollReveal delay={0.1} className="mb-24">
            <h2 className="text-2xl font-heading font-bold text-text text-center mb-12">
              How It Works
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6 relative">
              {/* Connector line (desktop only) */}
              <div className="hidden md:block absolute top-6 left-[12.5%] right-[12.5%] h-0.5 bg-gradient-to-r from-primary/20 via-primary/40 to-primary/20" />
              {howItWorks.map((step, i) => (
                <motion.div
                  key={step.step}
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ delay: i * 0.15 }}
                  className="text-center relative"
                >
                  <motion.div
                    whileHover={{ scale: 1.1, rotate: 5 }}
                    className="w-12 h-12 rounded-full bg-gradient-to-br from-primary to-secondary flex items-center justify-center mx-auto mb-4 relative z-10"
                  >
                    <span className="text-lg font-heading font-bold text-black">{step.step}</span>
                  </motion.div>
                  <h3 className="text-base font-heading font-semibold text-text mb-2">{step.title}</h3>
                  <p className="text-sm text-text-muted font-body">{step.desc}</p>
                </motion.div>
              ))}
            </div>
          </ScrollReveal>

          {/* Team */}
          <ScrollReveal delay={0.1} className="mb-24">
            <h2 className="text-2xl font-heading font-bold text-text text-center mb-12">
              The Team
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {team.map((person, i) => (
                <motion.div
                  key={person.name}
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ delay: i * 0.1 }}
                  whileHover={{ y: -6, transition: { duration: 0.2 } }}
                  className="rounded-xl border border-border bg-bg-card p-6 text-center hover:border-primary/30 hover:shadow-lg hover:shadow-primary/5 transition-all duration-300"
                >
                  <motion.div
                    whileHover={{ scale: 1.1 }}
                    className={`w-16 h-16 rounded-full bg-gradient-to-br ${person.color} flex items-center justify-center mx-auto mb-4`}
                  >
                    <span className="text-lg font-heading font-bold text-white">{person.avatar}</span>
                  </motion.div>
                  <h3 className="text-base font-heading font-semibold text-text mb-1">{person.name}</h3>
                  <p className="text-xs text-primary font-mono mb-3">{person.role}</p>
                  <p className="text-sm text-text-muted font-body">{person.bio}</p>
                </motion.div>
              ))}
            </div>
          </ScrollReveal>

          {/* Tech Stack */}
          <ScrollReveal delay={0.1}>
            <div className="rounded-xl border border-border bg-bg-card p-8 text-center">
              <h2 className="text-xl font-heading font-bold text-text mb-6">Tech Stack</h2>
              <div className="flex flex-wrap justify-center gap-3">
                {["Kotlin", "Jetpack Compose", "Room DB", "React 19", "Vite", "Tailwind CSS", "Recharts", "Framer Motion", "Health Connect API", "Camera PPG"].map((tech, i) => (
                  <motion.span
                    key={tech}
                    initial={{ opacity: 0, scale: 0.8 }}
                    whileInView={{ opacity: 1, scale: 1 }}
                    viewport={{ once: true }}
                    transition={{ delay: i * 0.04 }}
                    whileHover={{ scale: 1.1, y: -2 }}
                    className="px-4 py-2 rounded-lg bg-bg border border-border text-sm text-text-muted font-mono hover:border-primary/40 hover:text-primary transition-colors cursor-default"
                  >
                    {tech}
                  </motion.span>
                ))}
              </div>
            </div>
          </ScrollReveal>
        </div>
      </section>
    </SiteLayout>
  );
}
