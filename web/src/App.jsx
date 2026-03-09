import { lazy, Suspense } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "./hooks/useAuth";

/* ——————————————————————————————————————————————
   Lazy-loaded Route Components
   —————————————————————————————————————————————— */

// Public site
const Landing = lazy(() => import("./site/PromoHome"));
const Features = lazy(() => import("./site/PromoFeatures"));
const Plans = lazy(() => import("./site/PromoPlans"));
const About = lazy(() => import("./site/PromoAbout"));
const Download = lazy(() => import("./site/PromoDownload"));

// Auth
const Login = lazy(() => import("./app/Login"));
const Register = lazy(() => import("./app/Register"));

// Dashboard redirect
const Dashboard = lazy(() => import("./app/Dashboard"));

// Layouts
const CommanderLayout = lazy(() => import("./app/layouts/CommanderLayout"));
const PhysicianLayout = lazy(() => import("./app/layouts/PhysicianLayout"));
const PsychologistLayout = lazy(() => import("./app/layouts/PsychologistLayout"));
const TrainerLayout = lazy(() => import("./app/layouts/TrainerLayout"));
const PatientLayout = lazy(() => import("./app/layouts/PatientLayout"));
const SoldierLayout = lazy(() => import("./app/layouts/SoldierLayout"));

// Commander
const CommanderOps = lazy(() => import("./app/commander/CommanderOps"));
const Personnel = lazy(() => import("./app/commander/Personnel"));
const Units = lazy(() => import("./app/commander/Units"));
const Alerts = lazy(() => import("./app/commander/Alerts"));

// Physician
const PhysicianPatients = lazy(() => import("./app/physician/PhysicianPatients"));
const PatientDetail = lazy(() => import("./app/physician/PatientDetail"));
const PhysicianAppointments = lazy(() => import("./app/physician/PhysicianAppointments"));

// Psychologist
const PsychMood = lazy(() => import("./app/psychologist/PsychMood"));
const PsychStress = lazy(() => import("./app/psychologist/PsychStress"));
const PsychSessions = lazy(() => import("./app/psychologist/PsychSessions"));

// Trainer
const TrainerTeam = lazy(() => import("./app/trainer/TrainerTeam"));
const TrainerAthleteDetail = lazy(() => import("./app/trainer/TrainerAthleteDetail"));
const TrainerPrograms = lazy(() => import("./app/trainer/TrainerPrograms"));
const TrainerAcwr = lazy(() => import("./app/trainer/TrainerAcwr"));

// Patient
const PatientHome = lazy(() => import("./app/patient/PatientHome"));
const PatientMedications = lazy(() => import("./app/patient/PatientMedications"));
const PatientMessages = lazy(() => import("./app/patient/PatientMessages"));

// Soldier
const SoldierStatus = lazy(() => import("./app/soldier/SoldierStatus"));
const SoldierTraining = lazy(() => import("./app/soldier/SoldierTraining"));

// Admin
const AdminUsers = lazy(() => import("./app/admin/AdminUsers"));
const AdminSystem = lazy(() => import("./app/admin/AdminSystem"));

/* ——————————————————————————————————————————————
   Loading Fallback
   —————————————————————————————————————————————— */

function LoadingScreen() {
  return (
    <div className="flex items-center justify-center min-h-screen bg-bg">
      <div className="flex flex-col items-center gap-4">
        <div className="w-10 h-10 border-2 border-primary border-t-transparent rounded-full animate-spin" />
        <p className="text-text-muted text-sm font-mono tracking-wide">Loading...</p>
      </div>
    </div>
  );
}

/* ——————————————————————————————————————————————
   Protected Route Wrapper
   —————————————————————————————————————————————— */

function ProtectedRoute({ children, allowedRoles }) {
  const { user, isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/app/dashboard" replace />;
  }

  return children;
}

/* ——————————————————————————————————————————————
   App Component
   —————————————————————————————————————————————— */

export default function App() {
  return (
    <Suspense fallback={<LoadingScreen />}>
      <Routes>
        {/* Public Site */}
        <Route path="/" element={<Landing />} />
        <Route path="/features" element={<Features />} />
        <Route path="/plans" element={<Plans />} />
        <Route path="/about" element={<About />} />
        <Route path="/download" element={<Download />} />

        {/* Auth */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Dashboard (all authenticated users) */}
        <Route
          path="/app/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />

        {/* Commander */}
        <Route
          element={
            <ProtectedRoute allowedRoles={["commander", "admin"]}>
              <CommanderLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/app/ops" element={<CommanderOps />} />
          <Route path="/app/personnel" element={<Personnel />} />
          <Route path="/app/units" element={<Units />} />
        </Route>

        {/* Alerts — accessible to all authenticated */}
        <Route
          path="/app/alerts"
          element={
            <ProtectedRoute>
              <Alerts />
            </ProtectedRoute>
          }
        />

        {/* Physician */}
        <Route
          element={
            <ProtectedRoute allowedRoles={["physician", "psychologist", "admin"]}>
              <PhysicianLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/app/patients" element={<PhysicianPatients />} />
          <Route path="/app/patient/:id" element={<PatientDetail />} />
          <Route path="/app/appointments" element={<PhysicianAppointments />} />
        </Route>

        {/* Psychologist */}
        <Route
          element={
            <ProtectedRoute allowedRoles={["psychologist", "admin"]}>
              <PsychologistLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/app/mood" element={<PsychMood />} />
          <Route path="/app/stress" element={<PsychStress />} />
          <Route path="/app/sessions" element={<PsychSessions />} />
        </Route>

        {/* Trainer */}
        <Route
          element={
            <ProtectedRoute allowedRoles={["trainer", "commander", "admin"]}>
              <TrainerLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/app/team" element={<TrainerTeam />} />
          <Route path="/app/athlete/:id" element={<TrainerAthleteDetail />} />
          <Route path="/app/programs" element={<TrainerPrograms />} />
          <Route path="/app/acwr" element={<TrainerAcwr />} />
        </Route>

        {/* Patient */}
        <Route
          element={
            <ProtectedRoute allowedRoles={["patient"]}>
              <PatientLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/app/my-health" element={<PatientHome />} />
          <Route path="/app/medications" element={<PatientMedications />} />
          <Route path="/app/messages" element={<PatientMessages />} />
        </Route>

        {/* Soldier */}
        <Route
          element={
            <ProtectedRoute allowedRoles={["soldier"]}>
              <SoldierLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/app/my-readiness" element={<SoldierStatus />} />
          <Route path="/app/my-training" element={<SoldierTraining />} />
        </Route>

        {/* Admin */}
        <Route
          path="/app/users"
          element={
            <ProtectedRoute allowedRoles={["admin"]}>
              <AdminUsers />
            </ProtectedRoute>
          }
        />
        <Route
          path="/app/system"
          element={
            <ProtectedRoute allowedRoles={["admin"]}>
              <AdminSystem />
            </ProtectedRoute>
          }
        />

        {/* Catch-all */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
}
