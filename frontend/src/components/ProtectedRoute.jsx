import { useAuth } from "../context/AuthContext";

export default function ProtectedRoute({ children, allowedRoles }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div style={{
        display: "flex", alignItems: "center", justifyContent: "center",
        height: "100vh", color: "var(--text-secondary, #94a3b8)",
        background: "var(--bg-primary, #0a0f1e)", fontSize: 14
      }}>
        Loading…
      </div>
    );
  }

  if (!user) return null; // App.jsx will redirect to login

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return (
      <div style={{
        display: "flex", alignItems: "center", justifyContent: "center",
        height: "100vh", color: "#fb7185", background: "var(--bg-primary, #0a0f1e)"
      }}>
        Access denied.
      </div>
    );
  }

  return children;
}
