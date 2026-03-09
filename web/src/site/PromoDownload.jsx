import SiteLayout from "./SiteLayout";
import { motion } from "framer-motion";

const sensors = [
  { name: "Accelerometer", desc: "Sleep detection, step counting, activity recognition", icon: "📱" },
  { name: "Camera PPG", desc: "Heart rate, HRV (RMSSD, SDNN, pNN50) via fingertip", icon: "❤️" },
  { name: "GPS", desc: "Route tracking, pace, distance for outdoor activities", icon: "📍" },
  { name: "Gyroscope", desc: "Movement quality analysis, posture detection", icon: "🔄" },
  { name: "Light Sensor", desc: "Ambient light for circadian rhythm optimization", icon: "💡" },
  { name: "Health Connect", desc: "Integration with Samsung Health, Google Fit, and more", icon: "🔗" },
];

const screenshots = [
  { title: "Living Dashboard", desc: "Dynamic gradient changes with time of day. Readiness gauge, module cards, circadian timeline." },
  { title: "HRV Measurement", desc: "60-second camera PPG with real-time waveform. Butterworth-filtered signal processing." },
  { title: "Sleep Analysis", desc: "Hypnogram with REM/Deep/Light phases. Sleep score, cycle count, efficiency metrics." },
  { title: "Cognitive Tests", desc: "Reaction time, N-Back memory, Stroop attention, pattern recognition. Real millisecond precision." },
];

const requirements = [
  "Android 8.0 (API 26) or higher",
  "Camera with flash (for PPG measurements)",
  "GPS (for fitness tracking)",
  "50 MB free storage",
  "No internet required — works fully offline",
];

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
            className="text-center mb-16"
          >
            <h1 className="text-4xl md:text-5xl font-heading font-bold text-text mb-4">
              Download <span className="text-primary">VitaNova</span>
            </h1>
            <p className="text-lg text-text-muted max-w-xl mx-auto font-body mb-8">
              Your complete health ecosystem. Free, private, and powered by real sensors.
            </p>

            <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-8">
              <a
                href="#"
                className="inline-flex items-center gap-3 px-8 py-4 rounded-2xl bg-gradient-to-r from-primary to-secondary text-black font-heading font-bold text-lg hover:shadow-lg hover:shadow-primary/25 transition-all duration-300"
              >
                <svg className="w-8 h-8" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M17.523 2.237L12 7.71 6.477 2.24 5.064 3.65 12 10.536l6.936-6.886zM3.5 13h17v2h-17zM6.477 21.76L12 16.29l5.523 5.474 1.413-1.41L12 13.464l-6.936 6.886z" />
                </svg>
                Download APK
              </a>
            </div>

            <p className="text-sm text-text-dim font-body">
              v1.0.0 — APK size: ~15 MB — Android 8.0+
            </p>
          </motion.div>

          {/* QR Code placeholder */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            className="flex justify-center mb-20"
          >
            <div className="w-48 h-48 rounded-2xl border-2 border-dashed border-border bg-bg-card flex items-center justify-center">
              <div className="text-center">
                <div className="text-4xl mb-2">📲</div>
                <p className="text-xs text-text-dim font-mono">Scan to download</p>
              </div>
            </div>
          </motion.div>

          {/* Screenshots */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.3 }}
            className="mb-20"
          >
            <h2 className="text-2xl font-heading font-bold text-text text-center mb-8">
              What You'll See
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {screenshots.map((shot) => (
                <div key={shot.title} className="rounded-xl border border-border bg-bg-card overflow-hidden">
                  <div className="h-48 bg-gradient-to-br from-primary/5 to-secondary/5 flex items-center justify-center">
                    <div className="w-24 h-40 rounded-xl bg-bg border border-border/50 flex items-center justify-center">
                      <span className="text-xs text-text-dim font-mono">Preview</span>
                    </div>
                  </div>
                  <div className="p-4">
                    <h3 className="text-base font-heading font-semibold text-text mb-1">{shot.title}</h3>
                    <p className="text-sm text-text-muted font-body">{shot.desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </motion.div>

          {/* Sensors Used */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.4 }}
            className="mb-20"
          >
            <h2 className="text-2xl font-heading font-bold text-text text-center mb-8">
              Sensors We Use
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {sensors.map((sensor) => (
                <div key={sensor.name} className="rounded-xl border border-border bg-bg-card p-5 flex items-start gap-3">
                  <span className="text-2xl">{sensor.icon}</span>
                  <div>
                    <h3 className="text-sm font-heading font-semibold text-text mb-1">{sensor.name}</h3>
                    <p className="text-xs text-text-muted font-body">{sensor.desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </motion.div>

          {/* Requirements */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.5 }}
            className="rounded-xl border border-border bg-bg-card p-8"
          >
            <h2 className="text-xl font-heading font-bold text-text mb-4">System Requirements</h2>
            <ul className="space-y-2">
              {requirements.map((req) => (
                <li key={req} className="flex items-center gap-2 text-sm text-text-muted font-body">
                  <svg className="w-4 h-4 text-primary flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                  {req}
                </li>
              ))}
            </ul>
          </motion.div>
        </div>
      </section>
    </SiteLayout>
  );
}
