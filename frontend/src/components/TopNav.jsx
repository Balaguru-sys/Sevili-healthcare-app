import "./TopNav.css";
import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import i18n from "../i18n";
import { LANGUAGES } from "../i18n";

export default function TopNav() {
  const { logout } = useAuth();

  const [showLangMenu, setShowLangMenu] = useState(false);
  const [currentLang, setCurrentLang] = useState(
    () => localStorage.getItem("medapp_lang") || "en"
  );

  const changeLang = (code) => {
    setCurrentLang(code);
    i18n.changeLanguage(code);
    localStorage.setItem("medapp_lang", code);
    setShowLangMenu(false);
  };

  const activeLang = LANGUAGES.find(l => l.code === currentLang);

  return (
    <header className="top-nav">

      {/* Left side — logo */}
      <div className="top-nav-left">
        <img src="/sevili-logo1.png" alt="Sevili" className="top-nav-logo" />
        <span className="top-nav-title">Sevili</span>
      </div>

      {/* Right side — language + logout */}
      <div className="top-nav-right">

        <div className="lang-selector-wrap">
          <button
            className="lang-selector-btn"
            onClick={() => setShowLangMenu(v => !v)}
          >
            <span className="material-symbols-outlined lang-icon">language</span>
            <span className="lang-current">{activeLang?.label}</span>
          </button>

          {showLangMenu && (
            <div className="lang-dropdown">
              {LANGUAGES.map(l => (
                <button
                  key={l.code}
                  className={`lang-option ${currentLang === l.code ? "lang-option--active" : ""}`}
                  onClick={() => changeLang(l.code)}
                >
                  <span className="lang-option-label">{l.label}</span>
                  <span className="lang-option-name">{l.name}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        <button
          className="logout-btn"
          onClick={logout}
          title="Sign out"
        >
          <span className="logout-btn">↩</span>
        </button>

      </div>

    </header>
  );
}