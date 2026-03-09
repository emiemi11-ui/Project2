import SiteLayout from "./SiteLayout";
import { motion, useInView } from "framer-motion";
import { useRef } from "react";

const sensors = [
  { name: "Accelerometer", desc: "Sleep detection, step counting, activity recognition", icon: "📱" },
  { name: "Camera PPG", desc: "Heart rate, HRV (RMSSD, SDNN, pNN50) via fingertip", icon: "❤️" },
  { name: "GPS", desc: "Route tracking, pace, distance for outdoor activities", icon: "📍" },
  { name: "Gyroscope", desc: "Movement quality analysis, posture detection", icon: "🔄" },
  { name: "Light Sensor", desc: "Ambient light for circadian rhythm optimization", icon: "💡" },
  { name: "Health Connect", desc: "Integration with Samsung Health, Google Fit, and more", icon: "🔗" },
];

const screenshots = [
  { title: "Living Dashboard", desc: "Dynamic gradient changes with time of day. Readiness gauge, module cards, circadian timeline.", color: "from-emerald-500/20 to-teal-500/10" },
  { title: "HRV Measurement", desc: "60-second camera PPG with real-time waveform. Butterworth-filtered signal processing.", color: "from-red-500/20 to-orange-500/10" },
  { title: "Sleep Analysis", desc: "Hypnogram with REM/Deep/Light phases. Sleep score, cycle count, efficiency metrics.", color: "from-blue-500/20 to-indigo-500/10" },
  { title: "Cognitive Tests", desc: "Reaction time, N-Back memory, Stroop attention, pattern recognition. Real millisecond precision.", color: "from-purple-500/20 to-pink-500/10" },
];

const requirements = [
  "Android 8.0 (API 26) or higher",
  "Camera with flash (for PPG measurements)",
  "GPS (for fitness tracking)",
  "50 MB free storage",
  "No internet required — works fully offline",
];

const features = [
  { title: "8 Health Modules", desc: "Sleep, Energy, Focus, Fitness, Nutrition, Brain, Habits, Mood" },
  { title: "Real Sensors", desc: "Camera PPG, accelerometer, gyroscope, GPS, light sensor" },
  { title: "100% Offline", desc: "No cloud, no internet required, no data leaves your device" },
  { title: "Privacy Matrix", desc: "Granular control over what data you share and with whom" },
  { title: "Elastic Streaks", desc: "Flexible habit tracking that bends instead of breaking" },
  { title: "Circadian Timing", desc: "Recommendations aligned to your biological clock" },
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

function PhoneMockup() {
  return (
    <motion.div
      initial={{ opacity: 0, y: 30 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.8, delay: 0.2 }}
      className="flex justify-center mb-16"
    >
      <motion.div
        whileHover={{ scale: 1.03, rotateY: 5 }}
        transition={{ type: "spring", stiffness: 200 }}
        className="relative w-64 h-[500px] rounded-[2.5rem] border-4 border-border bg-bg-card shadow-2xl shadow-primary/10 overflow-hidden"
        style={{ perspective: 1000 }}
      >
        {/* Notch */}
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-28 h-6 bg-bg rounded-b-2xl z-10" />
        {/* Screen content */}
        <div className="absolute inset-3 top-8 rounded-2xl bg-gradient-to-b from-[#0a1628] to-[#071018] overflow-hidden">
          {/* Status bar */}
          <div className="flex items-center justify-between px-4 pt-2 pb-1">
            <span className="text-[8px] text-gray-400 font-mono">9:41</span>
            <div className="flex items-center gap-1">
              <div className="w-3 h-1.5 rounded-sm border border-gray-500" />
            </div>
          </div>
          {/* Mock dashboard */}
          <div className="px-3 pt-2">
            <div className="text-[10px] text-gray-400 mb-1">Good morning</div>
            <div className="text-sm font-bold text-white mb-3">Your Readiness</div>
            {/* Readiness gauge mock */}
            <div className="flex justify-center mb-3">
              <div className="relative w-20 h-20">
                <svg viewBox="0 0 100 100" className="w-full h-full -rotate-90">
                  <circle cx="50" cy="50" r="40" fill="none" stroke="#1f2937" strokeWidth="8" />
                  <motion.circle
                    cx="50" cy="50" r="40" fill="none" stroke="#00e5a0" strokeWidth="8"
                    strokeDasharray="251" strokeDashoffset="63"
                    strokeLinecap="round"
                    initial={{ strokeDashoffset: 251 }}
                    animate={{ strokeDashoffset: 63 }}
                    transition={{ duration: 1.5, delay: 0.8 }}
                  />
                </svg>
                <div className="absolute inset-0 flex items-center justify-center">
                  <span className="text-lg font-bold text-white">75</span>
                </div>
              </div>
            </div>
            {/* Module cards mock */}
            <div className="grid grid-cols-2 gap-1.5">
              {[
                { label: "Sleep", val: "7.2h", color: "bg-blue-500/20 text-blue-400" },
                { label: "HRV", val: "52ms", color: "bg-emerald-500/20 text-emerald-400" },
                { label: "Stress", val: "32%", color: "bg-orange-500/20 text-orange-400" },
                { label: "Steps", val: "8,421", color: "bg-purple-500/20 text-purple-400" },
              ].map((m) => (
                <div key={m.label} className={`rounded-lg p-2 ${m.color} bg-opacity-20`}>
                  <div className="text-[8px] opacity-60">{m.label}</div>
                  <div className="text-[11px] font-bold">{m.val}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
        {/* Home indicator */}
        <div className="absolute bottom-2 left-1/2 -translate-x-1/2 w-24 h-1 rounded-full bg-border" />
      </motion.div>
    </motion.div>
  );
}

export default function PromoDownload() {
  return (
    <SiteLayout>
      <section className="pt-32 pb-20 px-6">
        <div className="max-w-5xl mx-auto">
          {/* Hero */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="text-center mb-8"
          >
            <h1 className="text-4xl md:text-5xl font-heading font-bold text-text mb-4">
              Download <span className="text-primary">VitaNova</span>
            </h1>
            <p className="text-lg text-text-muted max-w-xl mx-auto font-body mb-8">
              Your complete health ecosystem. Free, private, and powered by real sensors.
            </p>

            <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-4">
              <motion.a
                href="#"
                whileHover={{ scale: 1.05, y: -2 }}
                whileTap={{ scale: 0.95 }}
                className="inline-flex items-center gap-3 px-8 py-4 rounded-2xl bg-gradient-to-r from-primary to-secondary text-black font-heading font-bold text-lg hover:shadow-lg hover:shadow-primary/25 transition-all duration-300"
              >
                <svg className="w-8 h-8" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M17.523 2.237L12 7.71 6.477 2.24 5.064 3.65 12 10.536l6.936-6.886zM3.5 13h17v2h-17zM6.477 21.76L12 16.29l5.523 5.474 1.413-1.41L12 13.464l-6.936 6.886z" />
                </svg>
                Download APK
              </motion.a>
            </div>

            <p className="text-sm text-text-dim font-body">
              v1.0.0 — APK size: ~15 MB — Android 8.0+
            </p>
          </motion.div>

          {/* Phone Mockup */}
          <PhoneMockup />

          {/* Feature Grid */}
          <ScrollReveal className="mb-20">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {features.map((feat, i) => (
                <motion.div
                  key={feat.title}
                  initial={{ opacity: 0, y: 15 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ delay: i * 0.06 }}
                  whileHover={{ y: -4, transition: { duration: 0.2 } }}
                  className="rounded-xl border border-border bg-bg-card p-5 hover:border-primary/30 hover:shadow-lg hover:shadow-primary/5 transition-all duration-300"
                >
                  <h3 className="text-sm font-heading font-semibold text-text mb-1">{feat.title}</h3>
                  <p className="text-xs text-text-muted font-body">{feat.desc}</p>
                </motion.div>
              ))}
            </div>
          </ScrollReveal>

          {/* Screenshots */}
          <ScrollReveal className="mb-20">
            <h2 className="text-2xl font-heading font-bold text-text text-center mb-8">
              What You'll See
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {screenshots.map((shot, i) => (
                <motion.div
                  key={shot.title}
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ delay: i * 0.1 }}
                  whileHover={{ y: -4, transition: { duration: 0.2 } }}
                  className="rounded-xl border border-border bg-bg-card overflow-hidden hover:border-primary/30 hover:shadow-lg hover:shadow-primary/5 transition-all duration-300"
                >
                  <div className={`h-48 bg-gradient-to-br ${shot.color} flex items-center justify-center`}>
                    <motion.div
                      whileHover={{ scale: 1.05 }}
                      className="w-24 h-40 rounded-xl bg-bg border border-border/50 flex items-center justify-center shadow-lg"
                    >
                      <span className="text-xs text-text-dim font-mono">Preview</span>
                    </motion.div>
                  </div>
                  <div className="p-4">
                    <h3 className="text-base font-heading font-semibold text-text mb-1">{shot.title}</h3>
                    <p className="text-sm text-text-muted font-body">{shot.desc}</p>
                  </div>
                </motion.div>
              ))}
            </div>
          </ScrollReveal>

          {/* Sensors Used */}
          <ScrollReveal className="mb-20">
            <h2 className="text-2xl font-heading font-bold text-text text-center mb-8">
              Sensors We Use
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {sensors.map((sensor, i) => (
                <motion.div
                  key={sensor.name}
                  initial={{ opacity: 0, scale: 0.9 }}
                  whileInView={{ opacity: 1, scale: 1 }}
                  viewport={{ once: true }}
                  transition={{ delay: i * 0.06 }}
                  whileHover={{ scale: 1.03, y: -2 }}
                  className="rounded-xl border border-border bg-bg-card p-5 flex items-start gap-3 hover:border-primary/30 transition-colors"
                >
                  <span className="text-2xl">{sensor.icon}</span>
                  <div>
                    <h3 className="text-sm font-heading font-semibold text-text mb-1">{sensor.name}</h3>
                    <p className="text-xs text-text-muted font-body">{sensor.desc}</p>
                  </div>
                </motion.div>
              ))}
            </div>
          </ScrollReveal>

          {/* Requirements */}
          <ScrollReveal className="mb-20">
            <div className="rounded-xl border border-border bg-bg-card p-8">
              <h2 className="text-xl font-heading font-bold text-text mb-4">System Requirements</h2>
              <ul className="space-y-2">
                {requirements.map((req, i) => (
                  <motion.li
                    key={req}
                    initial={{ opacity: 0, x: -10 }}
                    whileInView={{ opacity: 1, x: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: i * 0.05 }}
                    className="flex items-center gap-2 text-sm text-text-muted font-body"
                  >
                    <svg className="w-4 h-4 text-primary flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    {req}
                  </motion.li>
                ))}
              </ul>
            </div>
          </ScrollReveal>

          {/* Privacy Note */}
          <ScrollReveal>
            <motion.div
              whileHover={{ scale: 1.01 }}
              className="rounded-2xl bg-gradient-to-r from-primary/5 to-secondary/5 border border-primary/20 p-8 text-center"
            >
              <div className="text-3xl mb-3">🔒</div>
              <h2 className="text-xl font-heading font-bold text-text mb-2">Your Data, Your Device</h2>
              <p className="text-sm text-text-muted font-body max-w-lg mx-auto leading-relaxed">
                VitaNova stores all health data locally on your device. No cloud servers, no tracking,
                no third-party analytics. You control what gets shared through the Privacy Matrix.
              </p>
            </motion.div>
          </ScrollReveal>
        </div>
      </section>
    </SiteLayout>
  );
}
