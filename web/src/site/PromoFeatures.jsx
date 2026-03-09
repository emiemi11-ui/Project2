import React, { useRef } from 'react';
import { motion, useInView } from 'framer-motion';

/* ——————————————————————————————————————————————
   Animation helpers
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
      variants={{ hidden: {}, visible: { transition: { staggerChildren: 0.08 } } }}
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
   Data — 8 Modules in detail
   —————————————————————————————————————————————— */

const modules = [
  {
    id: 'sleep',
    icon: '🌙',
    name: 'Sleep',
    tagline: 'Understand every night, automatically.',
    color: 'from-indigo-500/20 to-purple-600/10',
    borderColor: 'border-indigo-500/30',
    accentColor: 'text-indigo-400',
    description:
      'Sleep module uses your phone\'s accelerometer to track movement patterns throughout the night. Our algorithm detects sleep phases (Light, Deep, REM) with clinical-grade accuracy validated against polysomnography studies.',
    features: [
      'Automatic sleep/wake detection — no button press needed',
      'Phase analysis: Light, Deep, REM, Awake segments',
      'Sleep score (0-100) based on duration, quality, and consistency',
      'Smart alarm that wakes you in the lightest phase',
      'Sleep debt tracking and recovery suggestions',
      'Night disturbance logging with timestamps',
    ],
    sensors: [
      { name: 'Accelerometer', usage: 'Detects body movement to infer sleep phases and disturbances' },
      { name: 'Light Sensor', usage: 'Monitors ambient light to detect late-night phone usage' },
    ],
    science: [
      'Algorithm validated against PSG (polysomnography) with 87% phase agreement',
      'Based on research from Sleep Medicine Reviews (2021): Actigraphy-based sleep staging',
      'Incorporates circadian rhythm models from Kronauer et al.',
    ],
    mockup: 'Sleep dashboard showing a hypnogram with colored phase bands, a circular sleep score of 82, and a 7-day trend chart with consistency metrics.',
  },
  {
    id: 'energy',
    icon: '⚡',
    name: 'Energy & HRV',
    tagline: 'See your energy before you feel it.',
    color: 'from-amber-500/20 to-orange-600/10',
    borderColor: 'border-amber-500/30',
    accentColor: 'text-amber-400',
    description:
      'Measure Heart Rate Variability using just your phone camera. Place your fingertip on the lens for 30 seconds and get a medical-grade HRV reading that reveals your autonomic nervous system balance.',
    features: [
      'Camera-based PPG (photoplethysmography) HRV measurement',
      '30-second scan — morning and evening recommended',
      'RMSSD, SDNN, and pNN50 metrics displayed',
      'Energy level score derived from HRV + sleep + activity data',
      'Stress vs Recovery balance indicator',
      'Trend analysis with personal baseline learning',
    ],
    sensors: [
      { name: 'Camera + Flash', usage: 'PPG signal extraction from fingertip blood flow color changes' },
    ],
    science: [
      'PPG-based HRV validated against ECG with r=0.94 correlation (European Heart Journal, 2020)',
      'Energy model based on allostatic load theory (McEwen, 2006)',
      'Autonomic balance assessment using Malik et al. HRV standards',
    ],
    mockup: 'HRV measurement screen with a pulsing heart animation, real-time BPM counter, RMSSD value of 42ms, and a color-coded energy meter from red (depleted) to green (optimal).',
  },
  {
    id: 'focus',
    icon: '🎯',
    name: 'Focus',
    tagline: 'Deep work, measured and protected.',
    color: 'from-sky-500/20 to-cyan-600/10',
    borderColor: 'border-sky-500/30',
    accentColor: 'text-sky-400',
    description:
      'Focus module tracks your digital attention patterns using UsageStats API. It identifies your productive hours, blocks distracting apps during deep work, and quantifies your actual focus time versus scattered attention.',
    features: [
      'Automatic screen-time categorization (productive vs distraction)',
      'Deep work session timer with notification blocking',
      'Pomodoro and custom interval support',
      'App usage breakdown with daily/weekly reports',
      'Focus score based on uninterrupted work blocks',
      'Optimal focus time suggestions based on your patterns',
    ],
    sensors: [
      { name: 'UsageStats API', usage: 'Tracks app usage, screen time, and notification frequency' },
      { name: 'DND Controller', usage: 'Manages Do Not Disturb mode during focus sessions' },
    ],
    science: [
      'Based on Cal Newport\'s Deep Work research and attention residue studies',
      'Gloria Mark (UCI): Average refocus time of 23 minutes after interruption',
      'Flow state triggers based on Csikszentmihalyi\'s challenge-skill balance model',
    ],
    mockup: 'Focus dashboard with a large timer in the center, surrounding app blocks showing which apps are silenced, a daily focus hours bar chart, and current streak counter showing "5 days".',
  },
  {
    id: 'fitness',
    icon: '🏋️',
    name: 'Fitness',
    tagline: 'Every step, every rep, every route.',
    color: 'from-red-500/20 to-rose-600/10',
    borderColor: 'border-red-500/30',
    accentColor: 'text-red-400',
    description:
      'Comprehensive fitness tracking using GPS, accelerometer, and step counter. Track runs, walks, cycling routes, and gym sessions. Automatic workout detection means you never miss logging an activity.',
    features: [
      'GPS route tracking with pace, elevation, and splits',
      'Automatic step counting with distance estimation',
      'Workout detection — knows when you\'re exercising',
      'Guided workout library with progress tracking',
      'Weekly volume and intensity load monitoring',
      'Recovery time estimation based on workout + HRV data',
    ],
    sensors: [
      { name: 'GPS', usage: 'Route tracking, distance calculation, pace and speed metrics' },
      { name: 'Step Counter', usage: 'Pedometer for daily step count and walking distance' },
      { name: 'Accelerometer', usage: 'Activity classification and workout type detection' },
    ],
    science: [
      'ACSM guidelines for exercise prescription integrated into load monitoring',
      'Foster\'s session RPE method for training load quantification',
      'Banister impulse-response model for fitness-fatigue balance',
    ],
    mockup: 'Map view showing a running route in green, overlay panel with distance (5.2km), pace (5:34/km), elapsed time, and heart rate zone. Bottom bar shows weekly activity rings.',
  },
  {
    id: 'nutrition',
    icon: '🥗',
    name: 'Nutrition',
    tagline: 'Snap, scan, done.',
    color: 'from-green-500/20 to-emerald-600/10',
    borderColor: 'border-green-500/30',
    accentColor: 'text-green-400',
    description:
      'Take a photo of your meal and our AI identifies the food, estimates portions, and calculates macronutrients. No manual entry, no barcode scanning, no searching databases. Just point and shoot.',
    features: [
      'AI food recognition from photos — identifies dishes and ingredients',
      'Automatic macro estimation (protein, carbs, fat, calories)',
      'Hydration tracking with smart reminders',
      'Meal timing analysis and intermittent fasting support',
      'Micronutrient gap detection and supplement suggestions',
      'Weekly meal planning with AI-generated suggestions',
    ],
    sensors: [
      { name: 'Camera', usage: 'Food photo capture for AI-powered nutritional analysis' },
    ],
    science: [
      'Computer vision model trained on 500K+ food images (ImageNet-Food subset)',
      'Portion estimation using depth-from-focus and reference object calibration',
      'Nutritional targets based on DRI (Dietary Reference Intakes) guidelines',
    ],
    mockup: 'Camera viewfinder showing a plate of food with AI overlay labels identifying "grilled chicken" (estimated 180g), "brown rice" (estimated 150g), and "mixed salad". Side panel shows running daily totals.',
  },
  {
    id: 'brain',
    icon: '🧠',
    name: 'Brain',
    tagline: 'Train your mind. Track the progress.',
    color: 'from-violet-500/20 to-purple-600/10',
    borderColor: 'border-violet-500/30',
    accentColor: 'text-violet-400',
    description:
      'Cognitive training games designed with neuroscientists. 5-minute daily sessions that challenge memory, attention, processing speed, and executive function. Track your cognitive age and see real improvement.',
    features: [
      '12 scientifically designed cognitive games',
      'Adaptive difficulty that matches your level',
      'Cognitive domains: Memory, Attention, Speed, Flexibility, Problem-solving',
      'Brain Age estimation updated weekly',
      'N-back training for working memory',
      'Stroop and flanker tasks for executive function',
    ],
    sensors: [
      { name: 'Touch Input', usage: 'Precise reaction time measurement down to millisecond accuracy' },
    ],
    science: [
      'N-back training efficacy: Jaeggi et al. (2008) — fluid intelligence transfer effects',
      'Cognitive reserve theory (Stern, 2009) — use-dependent neural plasticity',
      'Game design follows ACTIVE trial protocol (Ball et al., 2002)',
    ],
    mockup: 'Brain training hub showing 6 game icons arranged in a hexagonal pattern. Center displays "Brain Age: 28" with a downward arrow indicating improvement. Progress bars for each cognitive domain.',
  },
  {
    id: 'habits',
    icon: '🔄',
    name: 'Habits',
    tagline: 'Systems over goals.',
    color: 'from-teal-500/20 to-cyan-600/10',
    borderColor: 'border-teal-500/30',
    accentColor: 'text-teal-400',
    description:
      'Build unbreakable routines with habit stacking, micro-habits, and streak tracking. Morning and evening routines auto-detected. Integrates with all other modules to form a complete daily protocol.',
    features: [
      'Morning and evening routine builder',
      'Habit stacking — chain habits to existing behaviors',
      'Micro-habit system — start with 2-minute versions',
      'Streak tracking with freeze days for flexibility',
      'Cue-routine-reward loop visualization',
      'Cross-module habits (e.g., "Meditate after HRV scan")',
    ],
    sensors: [
      { name: 'UsageStats', usage: 'Detects routine patterns like wake time, first app opened' },
      { name: 'Step Counter', usage: 'Auto-confirms physical habits like morning walk' },
    ],
    science: [
      'Habit loop model: Charles Duhigg — The Power of Habit',
      'Atomic Habits framework: James Clear — 1% improvement compound effect',
      'Implementation intentions (Gollwitzer, 1999) — if-then planning efficacy',
    ],
    mockup: 'Daily routine timeline showing morning habits (wake, hydrate, stretch, journal) with green checkmarks. A streaks grid calendar below shows 23 consecutive days. Side panel lists evening routine items.',
  },
  {
    id: 'mood',
    icon: '💚',
    name: 'Mood',
    tagline: 'Feel it. Understand it. Navigate it.',
    color: 'from-pink-500/20 to-rose-600/10',
    borderColor: 'border-pink-500/30',
    accentColor: 'text-pink-400',
    description:
      'Quick emotional check-ins with voice journaling option. AI analyzes sentiment patterns over time, identifies mood triggers, and correlates your emotional state with sleep, exercise, and nutrition data.',
    features: [
      'One-tap mood check-in (5-point scale + emotion tags)',
      'Voice journaling with AI transcription and sentiment analysis',
      'Mood-trigger correlation (sleep, exercise, screen time)',
      'Pattern recognition — weekly and monthly mood maps',
      'Gratitude prompts and cognitive reframing exercises',
      'Crisis resource links and breathing exercises for acute stress',
    ],
    sensors: [
      { name: 'Microphone', usage: 'Voice journal recording with on-device sentiment analysis' },
      { name: 'UsageStats', usage: 'Correlates app usage patterns with mood fluctuations' },
    ],
    science: [
      'PANAS (Positive and Negative Affect Schedule) validated mood assessment',
      'Ecological Momentary Assessment (EMA) methodology for real-time mood tracking',
      'Pennebaker\'s expressive writing paradigm adapted for voice journaling',
    ],
    mockup: 'Mood dashboard with a large emotion wheel, recent check-in timeline showing mood flow, a correlation chart linking mood to sleep quality, and a voice journal entry with highlighted positive keywords.',
  },
];

/* ——————————————————————————————————————————————
   Module Detail Card
   —————————————————————————————————————————————— */

function ModuleDetail({ mod, index }) {
  const isReversed = index % 2 === 1;

  return (
    <Section className="py-20 sm:py-28 px-6" id={mod.id}>
      <div className="max-w-6xl mx-auto">
        <div className={`grid md:grid-cols-2 gap-12 md:gap-16 items-start ${isReversed ? 'md:direction-rtl' : ''}`}>
          {/* Info side */}
          <div className={isReversed ? 'md:order-2' : ''}>
            <div className="flex items-center gap-3 mb-4">
              <span className="text-4xl">{mod.icon}</span>
              <div>
                <h2 className="font-heading text-3xl sm:text-4xl font-bold text-text">{mod.name}</h2>
                <p className={`text-sm font-mono ${mod.accentColor}`}>{mod.tagline}</p>
              </div>
            </div>

            <p className="text-text-muted font-body leading-relaxed mb-8">{mod.description}</p>

            {/* Features */}
            <div className="mb-8">
              <h3 className="font-heading font-semibold text-sm text-text-dim uppercase tracking-wider mb-4">Key Features</h3>
              <ul className="space-y-2.5">
                {mod.features.map((f, i) => (
                  <li key={i} className="flex items-start gap-2.5 text-sm">
                    <svg className="w-4 h-4 mt-0.5 text-primary flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    <span className="text-text-muted font-body">{f}</span>
                  </li>
                ))}
              </ul>
            </div>

            {/* Sensors */}
            <div className="mb-8">
              <h3 className="font-heading font-semibold text-sm text-text-dim uppercase tracking-wider mb-4">Sensors Used</h3>
              <div className="space-y-3">
                {mod.sensors.map((s, i) => (
                  <div key={i} className="flex items-start gap-3 p-3 rounded-xl bg-bg-card border border-border">
                    <div className="px-2 py-0.5 rounded bg-primary/10 border border-primary/20 flex-shrink-0">
                      <span className="font-mono text-xs text-primary">{s.name}</span>
                    </div>
                    <p className="text-xs text-text-muted font-body leading-relaxed">{s.usage}</p>
                  </div>
                ))}
              </div>
            </div>

            {/* Science */}
            <div>
              <h3 className="font-heading font-semibold text-sm text-text-dim uppercase tracking-wider mb-4">Scientific Basis</h3>
              <ul className="space-y-2">
                {mod.science.map((s, i) => (
                  <li key={i} className="flex items-start gap-2.5 text-xs">
                    <span className="text-secondary mt-0.5 flex-shrink-0">📄</span>
                    <span className="text-text-dim font-mono leading-relaxed">{s}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>

          {/* Mockup side */}
          <div className={isReversed ? 'md:order-1' : ''}>
            <motion.div
              whileHover={{ y: -4, transition: { duration: 0.3 } }}
              className={`relative rounded-2xl bg-gradient-to-br ${mod.color} border ${mod.borderColor} p-8 overflow-hidden`}
            >
              {/* Phone frame mockup */}
              <div className="relative mx-auto w-full max-w-[280px]">
                <div className="rounded-[2rem] border-2 border-white/10 bg-bg/80 backdrop-blur-sm p-4 aspect-[9/16] flex flex-col">
                  {/* Status bar */}
                  <div className="flex items-center justify-between mb-4 px-1">
                    <span className="text-[10px] font-mono text-text-dim">9:41</span>
                    <div className="flex items-center gap-1">
                      <div className="w-3 h-2 rounded-sm border border-text-dim" />
                      <div className="w-2 h-2 rounded-full border border-text-dim" />
                    </div>
                  </div>

                  {/* Module header */}
                  <div className="flex items-center gap-2 mb-4">
                    <span className="text-xl">{mod.icon}</span>
                    <span className="font-heading font-bold text-sm text-text">{mod.name}</span>
                  </div>

                  {/* Placeholder UI elements */}
                  <div className="flex-1 space-y-3">
                    <div className="w-full h-24 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center">
                      <span className="text-4xl opacity-50">{mod.icon}</span>
                    </div>
                    <div className="grid grid-cols-2 gap-2">
                      <div className="h-14 rounded-lg bg-white/5 border border-white/10" />
                      <div className="h-14 rounded-lg bg-white/5 border border-white/10" />
                    </div>
                    <div className="w-full h-8 rounded-lg bg-white/5 border border-white/10" />
                    <div className="w-3/4 h-8 rounded-lg bg-white/5 border border-white/10" />
                    <div className="w-full h-16 rounded-lg bg-white/5 border border-white/10" />
                  </div>

                  {/* Bottom nav */}
                  <div className="flex items-center justify-around pt-3 mt-3 border-t border-white/10">
                    {['🏠', '📊', '⚙️'].map((ic, i) => (
                      <span key={i} className="text-sm opacity-40">{ic}</span>
                    ))}
                  </div>
                </div>

                {/* Notch */}
                <div className="absolute top-0 left-1/2 -translate-x-1/2 w-24 h-5 bg-bg rounded-b-2xl" />
              </div>

              {/* Mockup description */}
              <div className="mt-6 p-4 rounded-xl bg-bg/60 border border-white/5">
                <p className="text-xs text-text-dim font-mono leading-relaxed italic">
                  {mod.mockup}
                </p>
              </div>
            </motion.div>
          </div>
        </div>
      </div>
    </Section>
  );
}

/* ——————————————————————————————————————————————
   PAGE EXPORT
   —————————————————————————————————————————————— */

export default function PromoFeatures() {
  return (
    <>
      {/* Hero */}
      <section className="relative pt-32 pb-20 px-6 overflow-hidden">
        <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
          <div className="w-[600px] h-[400px] rounded-full bg-gradient-to-br from-primary/15 via-secondary/10 to-transparent blur-[100px] opacity-50" />
        </div>

        <div className="relative z-10 max-w-4xl mx-auto text-center">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
          >
            <span className="text-sm font-mono text-primary mb-4 block tracking-wider uppercase">Features</span>
          </motion.div>
          <motion.h1
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.7, delay: 0.1 }}
            className="font-heading text-4xl sm:text-5xl md:text-6xl font-bold mb-6"
          >
            8 Modules.{' '}
            <span className="bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              Infinite Insight.
            </span>
          </motion.h1>
          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.7, delay: 0.2 }}
            className="text-lg text-text-muted max-w-2xl mx-auto font-body"
          >
            Each module is independently powerful. Together, they create a health intelligence system that no single app can match.
          </motion.p>
        </div>

        {/* Quick nav */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7, delay: 0.35 }}
          className="relative z-10 max-w-3xl mx-auto mt-12"
        >
          <StaggerContainer className="flex flex-wrap items-center justify-center gap-3">
            {modules.map((mod) => (
              <motion.a
                key={mod.id}
                variants={cardVariant}
                href={`#${mod.id}`}
                className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-bg-card border border-border hover:border-primary/30 text-sm text-text-muted hover:text-text transition-all duration-200 font-body"
              >
                <span>{mod.icon}</span>
                <span>{mod.name}</span>
              </motion.a>
            ))}
          </StaggerContainer>
        </motion.div>
      </section>

      {/* Divider */}
      <div className="max-w-6xl mx-auto px-6">
        <div className="h-px bg-gradient-to-r from-transparent via-border to-transparent" />
      </div>

      {/* Module details */}
      {modules.map((mod, i) => (
        <React.Fragment key={mod.id}>
          <ModuleDetail mod={mod} index={i} />
          {i < modules.length - 1 && (
            <div className="max-w-6xl mx-auto px-6">
              <div className="h-px bg-gradient-to-r from-transparent via-border to-transparent" />
            </div>
          )}
        </React.Fragment>
      ))}

      {/* Bottom CTA */}
      <Section className="py-24 px-6">
        <div className="max-w-3xl mx-auto text-center">
          <h2 className="font-heading text-3xl sm:text-4xl font-bold mb-4">
            Ready to experience all 8 modules?
          </h2>
          <p className="text-text-muted font-body mb-8">
            Download VitaNova free and unlock your complete health ecosystem.
          </p>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <a
              href="/download"
              className="px-8 py-3.5 rounded-xl bg-primary text-[#03050A] font-heading font-semibold hover:bg-primary-hover transition-all duration-200 hover:shadow-[0_0_30px_rgba(0,229,160,0.3)]"
            >
              Download Free
            </a>
            <a
              href="/plans"
              className="px-8 py-3.5 rounded-xl border border-border text-text font-heading font-semibold hover:border-text-muted hover:bg-bg-card transition-all duration-200"
            >
              Compare Plans
            </a>
          </div>
        </div>
      </Section>
    </>
  );
}
