import { lazy, Suspense } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "./hooks/useAuth";

/* ——————————————————————————————————————————————
   Lazy-loaded Route Components
   —————————————————————————————————————————————— */

// Public site
const Landing = lazy(() => import("./pages/Landing"));
const Features = lazy(() => import("./pages/Features"));
const Plans = lazy(() => import("./pages/Plans"));
const About = lazy(() => import("./pages/About"));
const Download = lazy(() => import("./pages/Download"));

// Auth
const Login = lazy(() => import("./pages/Login"));
const Register = lazy(() => import("./pages/Register"));

// Dashboard
const Dashboard = lazy(() => import("./pages/app/Dashboard"));
const Ops = lazy(() => import("./pages/app/Ops"));
const Personnel = lazy(() => import("./pages/app/Personnel"));
const Units = lazy(() => import("./pages/app/Units"));
const Alerts = lazy(() => import("./pages/app/Alerts"));

// Clinical
const Patients = lazy(() => import("./pages/app/Patients"));
const PatientDetail = lazy(() => import("./pages/app/PatientDetail"));
const Appointments = lazy(() => import("./pages/app/Appointments"));

// Wellness
const Mood = lazy(() => import("./pages/app/Mood"));
const Stress = lazy(() => import("./pages/app/Stress"));
const Sessions = lazy(() => import("./pages/app/Sessions"));

// Performance
const Team = lazy(() => import("./pages/app/Team"));
const AthleteDetail = lazy(() => import("./pages/app/AthleteDetail"));
const Programs = lazy(() => import("./pages/app/Programs"));
const Acwr = lazy(() => import("./pages/app/Acwr"));

// Admin
const Users = lazy(() => import("./pages/app/Users"));
const System = lazy(() => import("./pages/app/System"));

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

        {/* Operations — commander, admin */}
        <Route
          path="/app/ops"
          element={
            <ProtectedRoute allowedRoles={["commander", "admin"]}>
              <Ops />
            </ProtectedRoute>
          }
        />
        <Route
          path="/app/personnel"
          element={
            <ProtectedRoute allowedRoles={["commander", "admin"]}>
              <Personnel />
            </ProtectedRoute>
          }
        />
        <Route
          path="/app/units"
          element={
            <ProtectedRoute allowedRoles={["commander", "admin"]}>
              <Units />
            </ProtectedRoute>
          }
        />
        <Route
          path="/app/alerts"
          element={
            <ProtectedRoute>
              <Alerts />
            </ProtectedRoute>
          }
        />

        {/* Clinical — physician, psychologist, admin */}
        <Route
          path="/app/patients"
          element={
            <ProtectedRoute allowedRoles={["physician", "psychologist", "admin"]}>
              <Patients />
            </ProtectedRoute>
          }
        />
        <Route
          path="/app/patient/:id"
          element={
            <ProtectedRoute allowedRoles={["physician", "psychologist", "admin"]}>
              <PatientDetail />
            </ProtectedRoute>
          }
        />
        <Route
          path="/app/appointments"
          element={
            <ProtectedRoute allowedRoles={["physician", "psychologist", "admin", "patient"]}>
              <Appointments />
            </ProtectedRoute>
          }
        />

        {/* Wellness — psychologist, patient, soldier, admin */}
        <Route
          path="/app/mood"
          element={
            <ProtectedRoute allowedRoles={["psychologist", "patient", "soldier", "admin"]}>
              <Mood />
            </ProtectedRoute>
          }
        />
        <Route
          path="/app/stress"
          element={
            <ProtectedRoute allowedRoles={["psychologist", "patient", "soldier", "admin"]}>
              <Stress />
            </ProtectedRoute>
          }
        />
        <Route
          path="/app/sessions"
          element={
            <ProtectedRoute allowedRoles={["psychologist", "admin"]}>
              <Sessions />
            </ProtectedRoute>
          }
        />

        {/* Performance — trainer, commander, admin */}
        <Route
          path="/app/team"
          element={
            <ProtectedRoute allowedRoles={["trainer", "commander", "admin"]}>
              <Team />
            </ProtectedRoute>
          }
        />
        <Route
          path="/app/athlete/:id"
          element={
            <ProtectedRoute allowedRoles={["trainer", "commander", "admin"]}>
              <AthleteDetail />
            </ProtectedRoute>
          }
        />
        <Route
          path="/app/programs"
          element={
            <ProtectedRoute allowedRoles={["trainer", "admin"]}>
              <Programs />
            </ProtectedRoute>
          }
        />
        <Route
          path="/app/acwr"
          element={
            <ProtectedRoute allowedRoles={["trainer", "commander", "admin"]}>
              <Acwr />
            </ProtectedRoute>
          }
        />

        {/* Admin */}
        <Route
          path="/app/users"
          element={
            <ProtectedRoute allowedRoles={["admin"]}>
              <Users />
            </ProtectedRoute>
          }
        />
        <Route
          path="/app/system"
          element={
            <ProtectedRoute allowedRoles={["admin"]}>
              <System />
            </ProtectedRoute>
          }
        />

        {/* Catch-all */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
}
