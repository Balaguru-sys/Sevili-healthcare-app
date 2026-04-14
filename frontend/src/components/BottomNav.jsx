  import "./BottomNav.css";

  const TABS = [
    { key: "dashboard", icon: "home", label: "Home"      },
    { key: "records",   icon: "note", label: "Records"   },
    { key: "chat",      icon: "chat",  label: "AI Chat"   },
    { key: "doctor",    icon: "medical_services", label: "Doctors"   },
    { key: "emergency", icon: "siren", label: "SOS"       },
  ];

  export default function BottomNav({ active, onChange }) {
    return (
      <nav className="bottom-nav">
        {TABS.map((t) => (
          <button
            key={t.key}
            className={`nav-btn ${active === t.key ? "nav-btn--active" : ""}`}
            onClick={() => onChange(t.key)}
          >
            <span className="material-symbols-outlined nav-icon">{t.icon}</span>
            <span className="nav-label">{t.label}</span>
          </button>
        ))}
      </nav>
    );
  }
