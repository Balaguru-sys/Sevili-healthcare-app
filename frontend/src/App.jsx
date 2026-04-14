import { useState } from "react";
import { AuthProvider, useAuth } from "./context/AuthContext";
import LoginPage from "./pages/LoginPage";

// Patient pages
import Dashboard from "./pages/Dashboard";
import Chat from "./pages/Chat";
import Records from "./pages/Records";
import DoctorList from "./pages/DoctorList";
import DoctorProfile from "./pages/DoctorProfile";
import Emergency from "./pages/Emergency";
import BottomNav from "./components/BottomNav";

// Staff pages
import StaffDashboard from "./pages/staff/StaffDashboard";

import "./App.css";
import TopNav from "./components/TopNav";

function AppShell() {
  const { user, loading } = useAuth();
  const [activeTab, setActiveTab] = useState("dashboard");
  const [selectedDoctorId, setDoctorId] = useState(null);

  if (loading) {
    return (
      <div className="app-loading">
        <span>⊕</span>
      </div>
    );
  }

  if (!user) return <LoginPage />;

  // Staff users get the full staff dashboard
  if (user.role === "ROLE_STAFF") {
    return <StaffDashboard />;
  }

  // Patients get the mobile patient app
  const handleSelectDoctor = (id) => {
    setDoctorId(id);
    setActiveTab("doctorProfile");
  };

  const renderPage = () => {
    switch (activeTab) {
      case "dashboard":   return <Dashboard patientId={user.id} />;
      case "chat":        return <Chat patientId={user.id} />;
      case "records":     return <Records patientId={user.id} />;
      case "doctor":      return <DoctorList onSelectDoctor={handleSelectDoctor} />;
      case "doctorProfile":
        return (
          <DoctorProfile
            doctorId={selectedDoctorId}
            patientId={user.id}
            onBack={() => setActiveTab("doctor")}
          />
        );
      case "emergency":   return <Emergency patientId={user.id} />;
      default:            return <Dashboard patientId={user.id} />;
    }
  };

  return (
    <div className="app-root">
      <TopNav />
      <div className="page-content">{renderPage()}</div>
      <BottomNav
        active={activeTab === "doctorProfile" ? "doctor" : activeTab}
        onChange={(tab) => {
          setActiveTab(tab);
          if (tab !== "doctorProfile") setDoctorId(null);
        }}
      />
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppShell />
    </AuthProvider>
  );
}
