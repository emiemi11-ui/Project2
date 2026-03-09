import SiteLayout from "./SiteLayout";
import { motion } from "framer-motion";

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
  },
  {
    name: "Maria Ionescu",
    role: "Head of Product",
    bio: "Sports science PhD with a passion for bridging the gap between research and consumer products.",
    avatar: "MI",
  },
  {
    name: "Andrei Dragomir",
    role: "Lead Engineer",
    bio: "Full-stack architect with deep expertise in sensor integration, real-time data processing, and mobile development.",
    avatar: "AD",
  },
  {
    name: "Elena Constantinescu",
    role: "UX & Design Lead",
    bio: "Designed health interfaces for 3 national health services. Believes data visualization should be beautiful and actionable.",
    avatar: "EC",
  },
];

const howItWorks = [
  { step: "1", title: "Sensors Collect", desc: "Your phone's built-in sensors (accelerometer, gyroscope, camera, GPS) continuously gather biometric data." },
  { step: "2", title: "Algorithms Analyze", desc: "On-device algorithms (HRV analysis, sleep staging, ACWR calculation) transform raw data into actionable metrics." },
  { step: "3", title: "Insights Emerge", desc: "Your personal dashboard shows readiness scores, sleep quality, stress levels, and cognitive performance — all in real time." },
  { step: "4", title: "Specialists Act", desc: "With your consent, trainers, physicians, and psychologists see relevant metrics to provide personalized guidance." },
];

export default function PromoAbout() {
  return (
    <SiteLayout>
      <section className="pt-32 pb-20 px-6">
        <div className="max-w-5xl mx-auto">
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

          {/* Principles */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-24">
            {principles.map((p, i) => (
              <motion.div
                key={p.title}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4, delay: i * 0.08 }}
                className="rounded-xl border border-border bg-bg-card p-6"
              >
                <div className="text-2xl mb-3">{p.icon}</div>
                <h3 className="text-lg font-heading font-bold text-text mb-2">{p.title}</h3>
                <p className="text-sm text-text-muted font-body leading-relaxed">{p.desc}</p>
              </motion.div>
            ))}
          </div>

          {/* How It Works */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.3 }}
            className="mb-24"
          >
            <h2 className="text-2xl font-heading font-bold text-text text-center mb-12">
              How It Works
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              {howItWorks.map((step, i) => (
                <div key={step.step} className="text-center">
                  <div className="w-12 h-12 rounded-full bg-gradient-to-br from-primary to-secondary flex items-center justify-center mx-auto mb-4">
                    <span className="text-lg font-heading font-bold text-black">{step.step}</span>
                  </div>
                  <h3 className="text-base font-heading font-semibold text-text mb-2">{step.title}</h3>
                  <p className="text-sm text-text-muted font-body">{step.desc}</p>
                  {i < howItWorks.length - 1 && (
                    <div className="hidden md:block absolute right-0 top-1/2 -translate-y-1/2 text-text-dim">→</div>
                  )}
                </div>
              ))}
            </div>
          </motion.div>

          {/* Team */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.4 }}
          >
            <h2 className="text-2xl font-heading font-bold text-text text-center mb-12">
              The Team
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {team.map((person) => (
                <div key={person.name} className="rounded-xl border border-border bg-bg-card p-6 text-center">
                  <div className="w-16 h-16 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 border border-primary/30 flex items-center justify-center mx-auto mb-4">
                    <span className="text-lg font-heading font-bold text-primary">{person.avatar}</span>
                  </div>
                  <h3 className="text-base font-heading font-semibold text-text mb-1">{person.name}</h3>
                  <p className="text-xs text-primary font-mono mb-3">{person.role}</p>
                  <p className="text-sm text-text-muted font-body">{person.bio}</p>
                </div>
              ))}
            </div>
          </motion.div>

          {/* Tech Stack */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.5 }}
            className="mt-24 rounded-xl border border-border bg-bg-card p-8 text-center"
          >
            <h2 className="text-xl font-heading font-bold text-text mb-6">Tech Stack</h2>
            <div className="flex flex-wrap justify-center gap-3">
              {["Kotlin", "Jetpack Compose", "Room DB", "React 19", "Vite", "Tailwind CSS", "Recharts", "Framer Motion", "Health Connect API", "Camera PPG"].map((tech) => (
                <span key={tech} className="px-4 py-2 rounded-lg bg-bg border border-border text-sm text-text-muted font-mono">
                  {tech}
                </span>
              ))}
            </div>
          </motion.div>
        </div>
      </section>
    </SiteLayout>
  );
}
