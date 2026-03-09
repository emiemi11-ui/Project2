/* ——————————————————————————————————————————————
   VitaNova Mock Data
   Realistic, coherent biometric data for 32 persons
   —————————————————————————————————————————————— */

// ——— Helpers ———

function seededRandom(seed) {
  let s = seed;
  return function next() {
    s = (s * 16807 + 0) % 2147483647;
    return (s - 1) / 2147483646;
  };
}

function clamp(value, min, max) {
  return Math.round(Math.min(max, Math.max(min, value)));
}

function generateHistory(baseValue, variance, days, rng) {
  const history = [];
  const today = new Date();
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(today);
    date.setDate(date.getDate() - i);
    const drift = (rng() - 0.5) * 2 * variance;
    history.push({
      date: date.toISOString().split("T")[0],
      value: clamp(baseValue + drift, 0, 100),
    });
  }
  return history;
}

function generateStepsHistory(baseSteps, variance, days, rng) {
  const history = [];
  const today = new Date();
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(today);
    date.setDate(date.getDate() - i);
    const drift = (rng() - 0.5) * 2 * variance;
    history.push({
      date: date.toISOString().split("T")[0],
      value: clamp(baseSteps + drift, 1000, 25000),
    });
  }
  return history;
}

function generateScreenTimeHistory(baseMinutes, variance, days, rng) {
  const history = [];
  const today = new Date();
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(today);
    date.setDate(date.getDate() - i);
    const drift = (rng() - 0.5) * 2 * variance;
    history.push({
      date: date.toISOString().split("T")[0],
      value: clamp(baseMinutes + drift, 30, 720),
    });
  }
  return history;
}

// ——— Archetypes for coherent score generation ———

const archetypes = {
  highPerformer: { sleep: [82, 95], hrv: [78, 92], readiness: [80, 95], stress: [10, 25], steps: [10000, 18000], screen: [60, 180], cognitive: [82, 96] },
  healthy: { sleep: [70, 85], hrv: [65, 80], readiness: [68, 82], stress: [20, 35], steps: [7000, 13000], screen: [120, 280], cognitive: [70, 85] },
  moderate: { sleep: [55, 72], hrv: [50, 68], readiness: [52, 70], stress: [35, 55], steps: [5000, 9000], screen: [200, 360], cognitive: [55, 72] },
  struggling: { sleep: [35, 55], hrv: [30, 50], readiness: [30, 52], stress: [55, 75], steps: [2500, 6000], screen: [300, 480], cognitive: [35, 55] },
  atRisk: { sleep: [20, 40], hrv: [18, 35], readiness: [15, 38], stress: [70, 90], steps: [1500, 4000], screen: [360, 600], cognitive: [20, 42] },
};

function pickInRange(range, rng) {
  return Math.round(range[0] + rng() * (range[1] - range[0]));
}

// ——— Raw person definitions ———

const personDefinitions = [
  { name: "Marcus Chen", age: 42, role: "commander", unit: "Alpha Company", archetype: "highPerformer" },
  { name: "Sarah Mitchell", age: 38, role: "physician", unit: "Medical Corps", archetype: "healthy" },
  { name: "David Okonkwo", age: 35, role: "psychologist", unit: "Behavioral Health", archetype: "highPerformer" },
  { name: "James Rodriguez", age: 30, role: "trainer", unit: "Performance Unit", archetype: "highPerformer" },
  { name: "Elena Vasquez", age: 34, role: "admin", unit: "HQ Support", archetype: "healthy" },
  { name: "Ryan Foster", age: 27, role: "soldier", unit: "Alpha Company", archetype: "healthy" },
  { name: "Aisha Patel", age: 29, role: "soldier", unit: "Alpha Company", archetype: "moderate" },
  { name: "Tyler Brooks", age: 24, role: "soldier", unit: "Bravo Company", archetype: "struggling" },
  { name: "Natasha Kim", age: 31, role: "physician", unit: "Medical Corps", archetype: "healthy" },
  { name: "Derek Washington", age: 26, role: "soldier", unit: "Bravo Company", archetype: "moderate" },
  { name: "Liam O'Brien", age: 23, role: "soldier", unit: "Charlie Company", archetype: "atRisk" },
  { name: "Sofia Ramirez", age: 28, role: "patient", unit: "Recovery Wing", archetype: "struggling" },
  { name: "Nathan Clarke", age: 33, role: "trainer", unit: "Performance Unit", archetype: "highPerformer" },
  { name: "Olivia Tran", age: 36, role: "psychologist", unit: "Behavioral Health", archetype: "healthy" },
  { name: "Ethan Morales", age: 25, role: "soldier", unit: "Alpha Company", archetype: "highPerformer" },
  { name: "Jasmine Lee", age: 22, role: "soldier", unit: "Charlie Company", archetype: "struggling" },
  { name: "Carlos Gutierrez", age: 40, role: "commander", unit: "Bravo Company", archetype: "healthy" },
  { name: "Hannah Novak", age: 32, role: "physician", unit: "Medical Corps", archetype: "moderate" },
  { name: "Brandon Yates", age: 21, role: "soldier", unit: "Charlie Company", archetype: "moderate" },
  { name: "Mia Thompson", age: 29, role: "patient", unit: "Recovery Wing", archetype: "atRisk" },
  { name: "Jackson Reed", age: 27, role: "soldier", unit: "Alpha Company", archetype: "healthy" },
  { name: "Zara Ahmed", age: 26, role: "soldier", unit: "Bravo Company", archetype: "highPerformer" },
  { name: "Cooper Hayes", age: 34, role: "trainer", unit: "Performance Unit", archetype: "healthy" },
  { name: "Priya Sharma", age: 30, role: "psychologist", unit: "Behavioral Health", archetype: "highPerformer" },
  { name: "Trevor Blake", age: 28, role: "soldier", unit: "Bravo Company", archetype: "struggling" },
  { name: "Nina Petrova", age: 25, role: "soldier", unit: "Charlie Company", archetype: "moderate" },
  { name: "Oscar Mendez", age: 37, role: "commander", unit: "Charlie Company", archetype: "moderate" },
  { name: "Rebecca Saunders", age: 33, role: "admin", unit: "HQ Support", archetype: "healthy" },
  { name: "Kai Nakamura", age: 24, role: "soldier", unit: "Alpha Company", archetype: "moderate" },
  { name: "Danielle Roy", age: 31, role: "patient", unit: "Recovery Wing", archetype: "moderate" },
  { name: "Victor Lindgren", age: 39, role: "physician", unit: "Medical Corps", archetype: "healthy" },
  { name: "Amara Osei", age: 23, role: "soldier", unit: "Bravo Company", archetype: "healthy" },
];

// ——— Generate persons with coherent metrics ———

export const persons = personDefinitions.map((def, index) => {
  const rng = seededRandom(index * 137 + 42);
  const arch = archetypes[def.archetype];

  const sleepScore = pickInRange(arch.sleep, rng);
  const hrvScore = pickInRange(arch.hrv, rng);
  const readinessScore = pickInRange(arch.readiness, rng);
  const stressScore = pickInRange(arch.stress, rng);
  const steps = pickInRange(arch.steps, rng);
  const screenTime = pickInRange(arch.screen, rng);
  const cognitiveScore = pickInRange(arch.cognitive, rng);

  return {
    id: `person-${String(index + 1).padStart(3, "0")}`,
    name: def.name,
    age: def.age,
    role: def.role,
    unit: def.unit,
    avatar: `https://api.dicebear.com/9.x/notionists/svg?seed=${encodeURIComponent(def.name)}`,
    status: stressScore > 70 ? "critical" : stressScore > 50 ? "warning" : "active",
    metrics: {
      sleepScore,
      hrvScore,
      readinessScore,
      stressScore,
      steps,
      screenTime,
      cognitiveScore,
    },
    history: {
      sleep: generateHistory(sleepScore, 8, 7, rng),
      hrv: generateHistory(hrvScore, 10, 7, rng),
      readiness: generateHistory(readinessScore, 9, 7, rng),
      stress: generateHistory(stressScore, 7, 7, rng),
      steps: generateStepsHistory(steps, 2500, 7, rng),
      screenTime: generateScreenTimeHistory(screenTime, 60, 7, rng),
      cognitive: generateHistory(cognitiveScore, 6, 7, rng),
    },
  };
});

// ——— Login accounts ———

export const accounts = [
  {
    id: "account-001",
    email: "m.chen@vitanova.mil",
    password: "commander123",
    personId: "person-001",
    role: "commander",
    displayName: "Col. Marcus Chen",
  },
  {
    id: "account-002",
    email: "s.mitchell@vitanova.mil",
    password: "physician123",
    personId: "person-002",
    role: "physician",
    displayName: "Dr. Sarah Mitchell",
  },
  {
    id: "account-003",
    email: "d.okonkwo@vitanova.mil",
    password: "psychologist123",
    personId: "person-003",
    role: "psychologist",
    displayName: "Dr. David Okonkwo",
  },
  {
    id: "account-004",
    email: "j.rodriguez@vitanova.mil",
    password: "trainer123",
    personId: "person-004",
    role: "trainer",
    displayName: "Sgt. James Rodriguez",
  },
  {
    id: "account-005",
    email: "e.vasquez@vitanova.mil",
    password: "admin123",
    personId: "person-005",
    role: "admin",
    displayName: "Elena Vasquez",
  },
  {
    id: "account-006",
    email: "s.ramirez@vitanova.mil",
    password: "patient123",
    personId: "person-012",
    role: "patient",
    displayName: "Sofia Ramirez",
  },
  {
    id: "account-007",
    email: "r.foster@vitanova.mil",
    password: "soldier123",
    personId: "person-006",
    role: "soldier",
    displayName: "Cpl. Ryan Foster",
  },
  {
    id: "account-008",
    email: "n.kim@vitanova.mil",
    password: "physician456",
    personId: "person-009",
    role: "physician",
    displayName: "Dr. Natasha Kim",
  },
  {
    id: "account-009",
    email: "c.gutierrez@vitanova.mil",
    password: "commander456",
    personId: "person-017",
    role: "commander",
    displayName: "Maj. Carlos Gutierrez",
  },
  // Dev-mode shortcut accounts (all password: demo123)
  {
    id: "account-100",
    email: "commander@vitanova.com",
    password: "demo123",
    personId: "person-001",
    role: "commander",
    displayName: "Col. Marcus Chen",
  },
  {
    id: "account-101",
    email: "physician@vitanova.com",
    password: "demo123",
    personId: "person-002",
    role: "physician",
    displayName: "Dr. Sarah Mitchell",
  },
  {
    id: "account-102",
    email: "psychologist@vitanova.com",
    password: "demo123",
    personId: "person-003",
    role: "psychologist",
    displayName: "Dr. David Okonkwo",
  },
  {
    id: "account-103",
    email: "trainer@vitanova.com",
    password: "demo123",
    personId: "person-004",
    role: "trainer",
    displayName: "Sgt. James Rodriguez",
  },
  {
    id: "account-104",
    email: "admin@vitanova.com",
    password: "demo123",
    personId: "person-005",
    role: "admin",
    displayName: "Elena Vasquez",
  },
  {
    id: "account-105",
    email: "patient1@vitanova.com",
    password: "demo123",
    personId: "person-012",
    role: "patient",
    displayName: "Sofia Ramirez",
  },
  {
    id: "account-106",
    email: "soldier1@vitanova.com",
    password: "demo123",
    personId: "person-006",
    role: "soldier",
    displayName: "Cpl. Ryan Foster",
  },
];

// ——— Unit summaries ———

export const units = [
  { id: "unit-alpha", name: "Alpha Company", commander: "Col. Marcus Chen", personnelCount: 6, readiness: 78 },
  { id: "unit-bravo", name: "Bravo Company", commander: "Maj. Carlos Gutierrez", personnelCount: 6, readiness: 64 },
  { id: "unit-charlie", name: "Charlie Company", commander: "Cpt. Oscar Mendez", personnelCount: 5, readiness: 52 },
  { id: "unit-medical", name: "Medical Corps", commander: "Dr. Sarah Mitchell", personnelCount: 4, readiness: 82 },
  { id: "unit-behavioral", name: "Behavioral Health", commander: "Dr. David Okonkwo", personnelCount: 3, readiness: 88 },
  { id: "unit-performance", name: "Performance Unit", commander: "Sgt. James Rodriguez", personnelCount: 3, readiness: 85 },
  { id: "unit-hq", name: "HQ Support", commander: "Elena Vasquez", personnelCount: 2, readiness: 76 },
  { id: "unit-recovery", name: "Recovery Wing", commander: "Dr. Sarah Mitchell", personnelCount: 3, readiness: 45 },
];

// ——— Alerts ———

export const alerts = [
  {
    id: "alert-001",
    type: "critical",
    title: "High Stress Detected",
    message: "Pvt. Liam O'Brien shows sustained stress levels above 80 for 3+ days. Immediate assessment recommended.",
    personId: "person-011",
    timestamp: new Date(Date.now() - 1000 * 60 * 12).toISOString(),
    acknowledged: false,
  },
  {
    id: "alert-002",
    type: "critical",
    title: "Low Readiness Score",
    message: "Mia Thompson's readiness dropped below 25. Clinical review required.",
    personId: "person-020",
    timestamp: new Date(Date.now() - 1000 * 60 * 45).toISOString(),
    acknowledged: false,
  },
  {
    id: "alert-003",
    type: "warning",
    title: "Sleep Pattern Disruption",
    message: "Tyler Brooks has logged below-average sleep scores for 5 consecutive nights.",
    personId: "person-008",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
    acknowledged: false,
  },
  {
    id: "alert-004",
    type: "warning",
    title: "HRV Decline",
    message: "Jasmine Lee's HRV has trended downward by 20% over the past week.",
    personId: "person-016",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 4).toISOString(),
    acknowledged: true,
  },
  {
    id: "alert-005",
    type: "info",
    title: "Recovery Milestone",
    message: "Danielle Roy completed phase 2 of the rehabilitation program.",
    personId: "person-030",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 8).toISOString(),
    acknowledged: true,
  },
  {
    id: "alert-006",
    type: "warning",
    title: "Excessive Screen Time",
    message: "Trevor Blake averaged 7+ hours of screen time daily this week.",
    personId: "person-025",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 6).toISOString(),
    acknowledged: false,
  },
  {
    id: "alert-007",
    type: "info",
    title: "Unit Readiness Update",
    message: "Charlie Company overall readiness has dropped below 55%. Review scheduled.",
    personId: null,
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 10).toISOString(),
    acknowledged: false,
  },
];

// ——— Appointments ———

const appointmentStatuses = ["scheduled", "completed", "cancelled"];

export const appointments = [
  {
    id: "appt-001",
    patientId: "person-012",
    patientName: "Sofia Ramirez",
    providerId: "person-002",
    providerName: "Dr. Sarah Mitchell",
    type: "Follow-up",
    date: new Date(Date.now() + 1000 * 60 * 60 * 24).toISOString(),
    duration: 30,
    status: "scheduled",
    notes: "Review medication adjustment and sleep patterns.",
  },
  {
    id: "appt-002",
    patientId: "person-020",
    patientName: "Mia Thompson",
    providerId: "person-003",
    providerName: "Dr. David Okonkwo",
    type: "Psych Evaluation",
    date: new Date(Date.now() + 1000 * 60 * 60 * 48).toISOString(),
    duration: 60,
    status: "scheduled",
    notes: "Biweekly cognitive behavioral therapy session.",
  },
  {
    id: "appt-003",
    patientId: "person-011",
    patientName: "Liam O'Brien",
    providerId: "person-014",
    providerName: "Dr. Olivia Tran",
    type: "Crisis Intervention",
    date: new Date(Date.now() + 1000 * 60 * 60 * 4).toISOString(),
    duration: 45,
    status: "scheduled",
    notes: "Urgent: sustained high-stress alert triggered.",
  },
  {
    id: "appt-004",
    patientId: "person-030",
    patientName: "Danielle Roy",
    providerId: "person-002",
    providerName: "Dr. Sarah Mitchell",
    type: "Rehab Check-in",
    date: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
    duration: 30,
    status: "completed",
    notes: "Phase 2 completion assessment. Patient progressing well.",
  },
  {
    id: "appt-005",
    patientId: "person-008",
    patientName: "Tyler Brooks",
    providerId: "person-003",
    providerName: "Dr. David Okonkwo",
    type: "Sleep Consultation",
    date: new Date(Date.now() - 1000 * 60 * 60 * 72).toISOString(),
    duration: 30,
    status: "completed",
    notes: "Discussed sleep hygiene strategies. Will reassess in 2 weeks.",
  },
  {
    id: "appt-006",
    patientId: "person-016",
    patientName: "Jasmine Lee",
    providerId: "person-024",
    providerName: "Dr. Priya Sharma",
    type: "Wellness Check",
    date: new Date(Date.now() + 1000 * 60 * 60 * 72).toISOString(),
    duration: 30,
    status: "scheduled",
    notes: "Monitor HRV decline trend and stress coping mechanisms.",
  },
];

// ——— Training programs ———

export const programs = [
  {
    id: "prog-001",
    name: "Operator Resilience Protocol",
    description: "12-week progressive overload program targeting tactical fitness and mental resilience.",
    duration: 12,
    unit: "weeks",
    targetRoles: ["soldier"],
    phases: [
      { name: "Foundation", weeks: "1-4", focus: "Aerobic base, mobility, stress inoculation" },
      { name: "Build", weeks: "5-8", focus: "Strength progression, interval training, cognitive drills" },
      { name: "Peak", weeks: "9-12", focus: "Max effort, operational simulation, recovery protocols" },
    ],
    enrolledCount: 14,
    completionRate: 72,
  },
  {
    id: "prog-002",
    name: "Return-to-Duty Rehabilitation",
    description: "Individualized recovery program for personnel transitioning back to active status.",
    duration: 8,
    unit: "weeks",
    targetRoles: ["patient"],
    phases: [
      { name: "Assessment", weeks: "1-2", focus: "Baseline testing, injury evaluation, goal setting" },
      { name: "Rebuild", weeks: "3-6", focus: "Progressive loading, movement re-education" },
      { name: "Integration", weeks: "7-8", focus: "Duty-specific tasks, clearance testing" },
    ],
    enrolledCount: 3,
    completionRate: 65,
  },
  {
    id: "prog-003",
    name: "Leadership Performance Edge",
    description: "Performance optimization for command-level personnel focusing on decision-making under stress.",
    duration: 6,
    unit: "weeks",
    targetRoles: ["commander"],
    phases: [
      { name: "Baseline", weeks: "1-2", focus: "Cognitive assessments, HRV profiling" },
      { name: "Optimize", weeks: "3-5", focus: "Neurofeedback, tactical breathing, sleep optimization" },
      { name: "Sustain", weeks: "6", focus: "Maintenance protocols, self-monitoring setup" },
    ],
    enrolledCount: 3,
    completionRate: 88,
  },
];

// ——— Mood entries ———

const moodLabels = ["great", "good", "okay", "low", "bad"];

export const moodEntries = persons
  .filter((p) => ["soldier", "patient"].includes(p.role))
  .flatMap((person) => {
    const rng = seededRandom(person.id.charCodeAt(8) * 31);
    const baseIdx = person.metrics.stressScore > 60 ? 3 : person.metrics.stressScore > 40 ? 2 : 1;
    const entries = [];
    for (let i = 6; i >= 0; i--) {
      const date = new Date();
      date.setDate(date.getDate() - i);
      const offset = Math.floor(rng() * 3) - 1;
      const idx = clamp(baseIdx + offset, 0, 4);
      entries.push({
        id: `mood-${person.id}-${i}`,
        personId: person.id,
        personName: person.name,
        mood: moodLabels[idx],
        note: idx >= 3 ? "Feeling worn out, not sleeping well." : idx <= 1 ? "Feeling sharp and motivated." : "Average day.",
        date: date.toISOString().split("T")[0],
      });
    }
    return entries;
  });

// ——— ACWR data (Acute:Chronic Workload Ratio) ———

export const acwrData = persons
  .filter((p) => ["soldier", "trainer"].includes(p.role))
  .map((person) => {
    const rng = seededRandom(person.id.charCodeAt(7) * 17);
    const acute = 400 + Math.round(rng() * 600);
    const chronic = 350 + Math.round(rng() * 500);
    const ratio = parseFloat((acute / chronic).toFixed(2));
    return {
      personId: person.id,
      personName: person.name,
      unit: person.unit,
      acuteLoad: acute,
      chronicLoad: chronic,
      ratio,
      zone: ratio > 1.5 ? "danger" : ratio > 1.3 ? "warning" : ratio < 0.8 ? "undertraining" : "optimal",
    };
  });

// ——— Session logs (psych sessions) ———

export const sessionLogs = [
  {
    id: "sess-001",
    patientId: "person-011",
    patientName: "Liam O'Brien",
    providerId: "person-003",
    providerName: "Dr. David Okonkwo",
    date: new Date(Date.now() - 1000 * 60 * 60 * 24 * 3).toISOString(),
    duration: 50,
    type: "CBT",
    summary: "Patient reports persistent intrusive thoughts and sleep onset insomnia. Applied cognitive restructuring techniques. Homework: thought journal before bed.",
    riskLevel: "elevated",
  },
  {
    id: "sess-002",
    patientId: "person-020",
    patientName: "Mia Thompson",
    providerId: "person-003",
    providerName: "Dr. David Okonkwo",
    date: new Date(Date.now() - 1000 * 60 * 60 * 24 * 5).toISOString(),
    duration: 55,
    type: "Trauma Processing",
    summary: "Continued EMDR protocol. Patient showed reduced distress when revisiting index event. SUD decreased from 7 to 4.",
    riskLevel: "moderate",
  },
  {
    id: "sess-003",
    patientId: "person-012",
    patientName: "Sofia Ramirez",
    providerId: "person-014",
    providerName: "Dr. Olivia Tran",
    date: new Date(Date.now() - 1000 * 60 * 60 * 24 * 2).toISOString(),
    duration: 45,
    type: "Supportive",
    summary: "Patient processing adjustment to recovery timeline. Explored coping strategies and unit reintegration concerns. Mood improving relative to last session.",
    riskLevel: "low",
  },
  {
    id: "sess-004",
    patientId: "person-025",
    patientName: "Trevor Blake",
    providerId: "person-024",
    providerName: "Dr. Priya Sharma",
    date: new Date(Date.now() - 1000 * 60 * 60 * 24 * 1).toISOString(),
    duration: 50,
    type: "CBT",
    summary: "Addressed avoidance behaviors and social withdrawal. Patient receptive to behavioral activation plan. Set goals for structured daily routine.",
    riskLevel: "moderate",
  },
  {
    id: "sess-005",
    patientId: "person-016",
    patientName: "Jasmine Lee",
    providerId: "person-014",
    providerName: "Dr. Olivia Tran",
    date: new Date(Date.now() - 1000 * 60 * 60 * 24 * 4).toISOString(),
    duration: 40,
    type: "Psychoeducation",
    summary: "Introduced stress-performance curve model. Patient learned diaphragmatic breathing. Will practice twice daily and report at next session.",
    riskLevel: "low",
  },
];

// ——— Dashboard summary stats (pre-computed) ———

export function getDashboardStats() {
  const total = persons.length;
  const critical = persons.filter((p) => p.status === "critical").length;
  const warning = persons.filter((p) => p.status === "warning").length;
  const active = persons.filter((p) => p.status === "active").length;

  const avgReadiness = Math.round(persons.reduce((sum, p) => sum + p.metrics.readinessScore, 0) / total);
  const avgStress = Math.round(persons.reduce((sum, p) => sum + p.metrics.stressScore, 0) / total);
  const avgSleep = Math.round(persons.reduce((sum, p) => sum + p.metrics.sleepScore, 0) / total);
  const avgHrv = Math.round(persons.reduce((sum, p) => sum + p.metrics.hrvScore, 0) / total);

  return {
    totalPersonnel: total,
    statusBreakdown: { active, warning, critical },
    averages: {
      readiness: avgReadiness,
      stress: avgStress,
      sleep: avgSleep,
      hrv: avgHrv,
    },
    unacknowledgedAlerts: alerts.filter((a) => !a.acknowledged).length,
    upcomingAppointments: appointments.filter((a) => a.status === "scheduled").length,
  };
}

// ——— Utility: find person by id ———

export function getPersonById(id) {
  return persons.find((p) => p.id === id) || null;
}

// ——— Utility: filter persons by unit ———

export function getPersonsByUnit(unitName) {
  return persons.filter((p) => p.unit === unitName);
}

// ——— Utility: filter persons by role ———

export function getPersonsByRole(role) {
  return persons.filter((p) => p.role === role);
}

// ——— Utility: get account by email ———

export function getAccountByEmail(email) {
  return accounts.find((a) => a.email === email) || null;
}
