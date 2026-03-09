import { createContext, useContext, useState, useCallback, useMemo, useEffect } from "react";
import { accounts, getPersonById } from "../data/mockData";

/* ——————————————————————————————————————————————
   Role Permissions Map
   —————————————————————————————————————————————— */

const rolePermissions = {
  admin: [
    "dashboard", "ops", "personnel", "units", "alerts",
    "patients", "patient-detail", "appointments",
    "mood", "stress", "sessions",
    "team", "athlete-detail", "programs", "acwr",
    "users", "system",
  ],
  commander: [
    "dashboard", "ops", "personnel", "units", "alerts",
    "team", "athlete-detail", "acwr",
  ],
  physician: [
    "dashboard", "alerts",
    "patients", "patient-detail", "appointments",
  ],
  psychologist: [
    "dashboard", "alerts",
    "patients", "patient-detail", "appointments",
    "mood", "stress", "sessions",
  ],
  trainer: [
    "dashboard", "alerts",
    "team", "athlete-detail", "programs", "acwr",
  ],
  patient: [
    "dashboard", "alerts",
    "appointments", "mood", "stress",
  ],
  soldier: [
    "dashboard", "alerts",
    "mood", "stress",
  ],
};

/* ——————————————————————————————————————————————
   Storage Keys
   —————————————————————————————————————————————— */

const TOKEN_KEY = "vitanova_token";
const USER_KEY = "vitanova_user";

/* ——————————————————————————————————————————————
   Token Utilities
   —————————————————————————————————————————————— */

function generateToken(accountId) {
  const payload = {
    sub: accountId,
    iat: Date.now(),
    exp: Date.now() + 1000 * 60 * 60 * 8, // 8 hours
  };
  return btoa(JSON.stringify(payload));
}

function isTokenValid(token) {
  if (!token) return false;
  try {
    const payload = JSON.parse(atob(token));
    return payload.exp > Date.now();
  } catch {
    return false;
  }
}

/* ——————————————————————————————————————————————
   Restore Session from localStorage
   —————————————————————————————————————————————— */

function restoreSession() {
  try {
    const token = localStorage.getItem(TOKEN_KEY);
    const userJson = localStorage.getItem(USER_KEY);
    if (token && isTokenValid(token) && userJson) {
      return { token, user: JSON.parse(userJson) };
    }
  } catch {
    // corrupted storage — clear it
  }
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  return { token: null, user: null };
}

/* ——————————————————————————————————————————————
   Auth Context
   —————————————————————————————————————————————— */

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [state, setState] = useState(() => {
    const restored = restoreSession();
    return {
      user: restored.user,
      token: restored.token,
      isAuthenticated: restored.user !== null,
      loginError: null,
    };
  });

  // Re-check token validity on focus (handles expired sessions)
  useEffect(() => {
    function handleFocus() {
      const token = localStorage.getItem(TOKEN_KEY);
      if (state.isAuthenticated && !isTokenValid(token)) {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        setState({ user: null, token: null, isAuthenticated: false, loginError: null });
      }
    }
    window.addEventListener("focus", handleFocus);
    return () => window.removeEventListener("focus", handleFocus);
  }, [state.isAuthenticated]);

  const login = useCallback((email, password) => {
    const account = accounts.find(
      (a) => a.email.toLowerCase() === email.toLowerCase() && a.password === password
    );

    if (!account) {
      setState((prev) => ({ ...prev, loginError: "Invalid email or password." }));
      return false;
    }

    const person = getPersonById(account.personId);
    const token = generateToken(account.id);

    const user = {
      accountId: account.id,
      personId: account.personId,
      email: account.email,
      displayName: account.displayName,
      role: account.role,
      permissions: rolePermissions[account.role] || [],
      avatar: person?.avatar || null,
      unit: person?.unit || null,
    };

    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));

    setState({ user, token, isAuthenticated: true, loginError: null });
    return true;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setState({ user: null, token: null, isAuthenticated: false, loginError: null });
  }, []);

  const hasPermission = useCallback(
    (permission) => {
      if (!state.user) return false;
      return state.user.permissions.includes(permission);
    },
    [state.user]
  );

  const hasRole = useCallback(
    (role) => {
      if (!state.user) return false;
      if (Array.isArray(role)) return role.includes(state.user.role);
      return state.user.role === role;
    },
    [state.user]
  );

  const clearError = useCallback(() => {
    setState((prev) => ({ ...prev, loginError: null }));
  }, []);

  const value = useMemo(
    () => ({
      user: state.user,
      token: state.token,
      isAuthenticated: state.isAuthenticated,
      loginError: state.loginError,
      login,
      logout,
      hasPermission,
      hasRole,
      clearError,
    }),
    [state, login, logout, hasPermission, hasRole, clearError]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

/* ——————————————————————————————————————————————
   useAuth Hook
   —————————————————————————————————————————————— */

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider.");
  }
  return context;
}

export default useAuth;
