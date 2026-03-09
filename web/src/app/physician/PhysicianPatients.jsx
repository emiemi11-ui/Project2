import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { persons, alerts, appointments } from "../../data/mockData";
import { motion } from "framer-motion";
import { AnimatedSection } from "../../components/AnimatedSection";

/* ——————————————————————————————————————————————
   Status dot color map
   —————————————————————————————————————————————— */
const statusColors = {
  active: "bg-emerald-500",
  warning: "bg-amber-500",
  critical: "bg-red-500",
};

const statusLabels = {
  active: "Stabil",
  warning: "Atenție",
  critical: "Critic",
};

/* ——————————————————————————————————————————————
   Mock last-visit dates (derived from appointments)
   —————————————————————————————————————————————— */
function getLastVisit(personId) {
  const past = appointments
    .filter((a) => a.patientId === personId && a.status === "completed")
    .sort((a, b) => new Date(b.date) - new Date(a.date));
  if (past.length > 0) {
    return new Date(past[0].date).toLocaleDateString("ro-RO", {
      day: "numeric",
      month: "short",
      year: "numeric",
    });
  }
  // fallback: random recent date
  const d = new Date();
  d.setDate(d.getDate() - Math.floor(Math.random() * 30 + 1));
  return d.toLocaleDateString("ro-RO", {
    day: "numeric",
    month: "short",
    year: "numeric",
  });
}

/* ——————————————————————————————————————————————
   Vital Card Component
   —————————————————————————————————————————————— */
function VitalCard({ label, value, unit, color, icon }) {
  return (
    <motion.div whileHover={{ y: -2, scale: 1.02 }} transition={{ duration: 0.2 }} className="bg-white rounded-xl border border-gray-200 p-4 flex flex-col gap-1 shadow-sm hover:shadow-md transition-shadow">
      <div className="flex items-center gap-2 text-gray-500 text-xs font-medium uppercase tracking-wide">
        <span>{icon}</span>
        <span>{label}</span>
      </div>
      <div className="flex items-baseline gap-1 mt-1">
        <span className={`text-2xl font-bold ${color}`}>{value}</span>
        <span className="text-gray-400 text-sm">{unit}</span>
      </div>
    </motion.div>
  );
}

/* ——————————————————————————————————————————————
   PhysicianPatients Component
   —————————————————————————————————————————————— */
export default function PhysicianPatients() {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedPatientId, setSelectedPatientId] = useState(null);

  // All persons are potential patients a physician can view
  const patientList = useMemo(() => {
    return persons.map((p) => ({
      ...p,
      lastVisit: getLastVisit(p.id),
    }));
  }, []);

  const filteredPatients = useMemo(() => {
    if (!searchQuery.trim()) return patientList;
    const q = searchQuery.toLowerCase();
    return patientList.filter(
      (p) =>
        p.name.toLowerCase().includes(q) ||
        p.unit.toLowerCase().includes(q) ||
        p.role.toLowerCase().includes(q)
    );
  }, [patientList, searchQuery]);

  const selectedPatient = useMemo(() => {
    if (!selectedPatientId) return null;
    return patientList.find((p) => p.id === selectedPatientId) || null;
  }, [patientList, selectedPatientId]);

  const patientAlerts = useMemo(() => {
    if (!selectedPatientId) return [];
    return alerts.filter((a) => a.personId === selectedPatientId);
  }, [selectedPatientId]);

  return (
    <div className="flex h-[calc(100vh-4rem)] bg-[#F8F9FA]">
      {/* ——— Left Panel: Patient List ——— */}
      <motion.aside
        initial={{ x: -20, opacity: 0 }}
        animate={{ x: 0, opacity: 1 }}
        transition={{ duration: 0.4 }}
        className="w-80 flex-shrink-0 bg-white border-r border-gray-200 flex flex-col"
      >
        {/* Search */}
        <div className="p-4 border-b border-gray-100">
          <h2 className="text-lg font-bold text-[#1A1A2E] mb-3">Pacienți</h2>
          <div className="relative">
            <svg
              className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
              />
            </svg>
            <input
              type="text"
              placeholder="Caută pacient..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 bg-[#F8F9FA] border border-gray-200 rounded-lg text-sm text-[#1A1A2E] placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
        </div>

        {/* Patient List */}
        <div className="flex-1 overflow-y-auto">
          {filteredPatients.length === 0 ? (
            <div className="p-4 text-center text-gray-400 text-sm">
              Niciun pacient găsit
            </div>
          ) : (
            filteredPatients.map((patient) => (
              <button
                key={patient.id}
                onClick={() => setSelectedPatientId(patient.id)}
                className={`w-full text-left px-4 py-3 border-b border-gray-50 hover:bg-blue-50 transition-colors ${
                  selectedPatientId === patient.id
                    ? "bg-blue-50 border-l-4 border-l-blue-500"
                    : "border-l-4 border-l-transparent"
                }`}
              >
                <div className="flex items-center gap-3">
                  <img
                    src={patient.avatar}
                    alt={patient.name}
                    className="w-10 h-10 rounded-full bg-gray-100 flex-shrink-0"
                  />
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-semibold text-[#1A1A2E] truncate">
                        {patient.name}
                      </span>
                      <span
                        className={`w-2.5 h-2.5 rounded-full flex-shrink-0 ${
                          statusColors[patient.status]
                        }`}
                        title={statusLabels[patient.status]}
                      />
                    </div>
                    <div className="flex items-center gap-2 mt-0.5">
                      <span className="text-xs text-gray-500">
                        {patient.age} ani
                      </span>
                      <span className="text-xs text-gray-300">|</span>
                      <span className="text-xs text-gray-500 truncate">
                        {patient.unit}
                      </span>
                    </div>
                    <div className="text-xs text-gray-400 mt-0.5">
                      Ultima vizită: {patient.lastVisit}
                    </div>
                  </div>
                </div>
              </button>
            ))
          )}
        </div>

        {/* Patient count footer */}
        <div className="px-4 py-2 border-t border-gray-100 bg-gray-50">
          <span className="text-xs text-gray-500">
            {filteredPatients.length} pacienți
          </span>
        </div>
      </motion.aside>

      {/* ——— Main Area: Selected Patient Overview ——— */}
      <main className="flex-1 overflow-y-auto p-6">
        {!selectedPatient ? (
          <div className="flex items-center justify-center h-full">
            <div className="text-center">
              <svg
                className="w-16 h-16 mx-auto text-gray-300 mb-4"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.5}
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"
                />
              </svg>
              <p className="text-gray-500 text-lg font-medium">
                Selectați un pacient
              </p>
              <p className="text-gray-400 text-sm mt-1">
                Alegeți un pacient din lista din stânga pentru a vedea detaliile
              </p>
            </div>
          </div>
        ) : (
          <motion.div
            key={selectedPatientId}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3 }}
            className="max-w-4xl mx-auto space-y-6"
          >
            {/* Patient Header */}
            <div className="bg-white rounded-xl border border-gray-200 p-6 shadow-sm">
              <div className="flex items-start gap-4">
                <img
                  src={selectedPatient.avatar}
                  alt={selectedPatient.name}
                  className="w-16 h-16 rounded-full bg-gray-100"
                />
                <div className="flex-1">
                  <div className="flex items-center gap-3">
                    <h1 className="text-2xl font-bold text-[#1A1A2E]">
                      {selectedPatient.name}
                    </h1>
                    <span
                      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${
                        selectedPatient.status === "critical"
                          ? "bg-red-100 text-red-700"
                          : selectedPatient.status === "warning"
                          ? "bg-amber-100 text-amber-700"
                          : "bg-emerald-100 text-emerald-700"
                      }`}
                    >
                      <span
                        className={`w-1.5 h-1.5 rounded-full ${
                          statusColors[selectedPatient.status]
                        }`}
                      />
                      {statusLabels[selectedPatient.status]}
                    </span>
                  </div>
                  <div className="flex items-center gap-4 mt-2 text-sm text-gray-500">
                    <span>{selectedPatient.age} ani</span>
                    <span className="text-gray-300">|</span>
                    <span>{selectedPatient.role}</span>
                    <span className="text-gray-300">|</span>
                    <span>{selectedPatient.unit}</span>
                    <span className="text-gray-300">|</span>
                    <span>
                      ID: {selectedPatient.id}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Latest Vitals Summary */}
            <div>
              <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-3">
                Semne vitale recente
              </h3>
              <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                <VitalCard
                  label="Heart Rate"
                  value={
                    60 +
                    Math.round(
                      (selectedPatient.metrics.stressScore / 100) * 40
                    )
                  }
                  unit="bpm"
                  color="text-rose-600"
                  icon="❤"
                />
                <VitalCard
                  label="HRV"
                  value={selectedPatient.metrics.hrvScore}
                  unit="ms"
                  color="text-blue-600"
                  icon="📊"
                />
                <VitalCard
                  label="Sleep Score"
                  value={selectedPatient.metrics.sleepScore}
                  unit="/100"
                  color="text-indigo-600"
                  icon="🌙"
                />
                <VitalCard
                  label="Stres"
                  value={selectedPatient.metrics.stressScore}
                  unit="/100"
                  color={
                    selectedPatient.metrics.stressScore > 60
                      ? "text-red-600"
                      : selectedPatient.metrics.stressScore > 40
                      ? "text-amber-600"
                      : "text-emerald-600"
                  }
                  icon="⚡"
                />
              </div>
            </div>

            {/* Additional Metrics Row */}
            <div className="grid grid-cols-3 gap-4">
              <div className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
                <div className="text-xs text-gray-500 font-medium uppercase tracking-wide">
                  Readiness
                </div>
                <div className="mt-2 flex items-baseline gap-1">
                  <span className="text-2xl font-bold text-teal-600">
                    {selectedPatient.metrics.readinessScore}
                  </span>
                  <span className="text-sm text-gray-400">/100</span>
                </div>
                <div className="mt-2 w-full bg-gray-100 rounded-full h-2">
                  <div
                    className="bg-teal-500 h-2 rounded-full transition-all"
                    style={{
                      width: `${selectedPatient.metrics.readinessScore}%`,
                    }}
                  />
                </div>
              </div>
              <div className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
                <div className="text-xs text-gray-500 font-medium uppercase tracking-wide">
                  Pași zilnici
                </div>
                <div className="mt-2 flex items-baseline gap-1">
                  <span className="text-2xl font-bold text-blue-600">
                    {selectedPatient.metrics.steps.toLocaleString()}
                  </span>
                  <span className="text-sm text-gray-400">pași</span>
                </div>
                <div className="mt-2 w-full bg-gray-100 rounded-full h-2">
                  <div
                    className="bg-blue-500 h-2 rounded-full transition-all"
                    style={{
                      width: `${Math.min(
                        (selectedPatient.metrics.steps / 12000) * 100,
                        100
                      )}%`,
                    }}
                  />
                </div>
              </div>
              <div className="bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
                <div className="text-xs text-gray-500 font-medium uppercase tracking-wide">
                  Cognitiv
                </div>
                <div className="mt-2 flex items-baseline gap-1">
                  <span className="text-2xl font-bold text-violet-600">
                    {selectedPatient.metrics.cognitiveScore}
                  </span>
                  <span className="text-sm text-gray-400">/100</span>
                </div>
                <div className="mt-2 w-full bg-gray-100 rounded-full h-2">
                  <div
                    className="bg-violet-500 h-2 rounded-full transition-all"
                    style={{
                      width: `${selectedPatient.metrics.cognitiveScore}%`,
                    }}
                  />
                </div>
              </div>
            </div>

            {/* Active Alerts for this patient */}
            {patientAlerts.length > 0 && (
              <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
                <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-3">
                  Alerte active
                </h3>
                <div className="space-y-2">
                  {patientAlerts.map((alert) => (
                    <div
                      key={alert.id}
                      className={`flex items-start gap-3 p-3 rounded-lg ${
                        alert.type === "critical"
                          ? "bg-red-50 border border-red-200"
                          : alert.type === "warning"
                          ? "bg-amber-50 border border-amber-200"
                          : "bg-blue-50 border border-blue-200"
                      }`}
                    >
                      <span
                        className={`mt-0.5 w-2 h-2 rounded-full flex-shrink-0 ${
                          alert.type === "critical"
                            ? "bg-red-500"
                            : alert.type === "warning"
                            ? "bg-amber-500"
                            : "bg-blue-500"
                        }`}
                      />
                      <div>
                        <div className="text-sm font-medium text-[#1A1A2E]">
                          {alert.title}
                        </div>
                        <div className="text-xs text-gray-500 mt-0.5">
                          {alert.message}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Quick Action Buttons */}
            <div className="flex items-center gap-3">
              <button
                onClick={() =>
                  navigate(`/app/patient/${selectedPatient.id}`)
                }
                className="px-5 py-2.5 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors shadow-sm"
              >
                Vezi dosarul complet
              </button>
              <button className="px-5 py-2.5 bg-white text-[#1A1A2E] text-sm font-medium rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors shadow-sm">
                Adaugă notă
              </button>
              <button className="px-5 py-2.5 bg-white text-[#1A1A2E] text-sm font-medium rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors shadow-sm">
                Programează
              </button>
              <button className="px-5 py-2.5 bg-red-50 text-red-700 text-sm font-medium rounded-lg border border-red-200 hover:bg-red-100 transition-colors shadow-sm">
                Alert
              </button>
            </div>
          </motion.div>
        )}
      </main>
    </div>
  );
}
