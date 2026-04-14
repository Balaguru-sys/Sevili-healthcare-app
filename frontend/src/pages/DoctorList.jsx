import { useState, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { api } from "../api/apiClient";
import "./DoctorList.css";

export default function DoctorList({ onSelectDoctor }) {
  const { t } = useTranslation();

  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);
  const [search,  setSearch]  = useState("");

  useEffect(() => {
    api.doctors()
      .then(setDoctors)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return doctors;
    return doctors.filter(d =>
      d.name.toLowerCase().includes(q) ||
      (d.specialty || "").toLowerCase().includes(q) ||
      (d.qualifications || "").toLowerCase().includes(q)
    );
  }, [doctors, search]);

  if (loading) return <div className="list-state">{t("doctors.loading")}</div>;
  if (error)   return <div className="list-state list-state--error">{error}</div>;

  return (
    <div className="doctor-list-page">
      <header className="page-header fade-up">
        <div className="header-items">
          <div className="page-date">{t("doctors.subtitle")}</div>
          <div className="page-title">{t("doctors.title")}</div>
        </div>
      </header>

      {/* Search bar */}
      <div className="search-bar-wrap fade-up-1">
        <span className="search-icon">🔍</span>
        <input
          className="search-input"
          type="text"
          placeholder={t("doctors.searchPlaceholder")}
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        {search && (
          <button className="search-clear" onClick={() => setSearch("")}>✕</button>
        )}
      </div>

      {filtered.length === 0 && (
        <div className="list-state">
          {t("doctors.noDoctor")}
        </div>
      )}

      <div className="doctor-cards fade-up-1">
        {filtered.map((doc) => (
          <div
            key={doc.id}
            className="doctor-card glass-card"
            onClick={() => onSelectDoctor(doc.id)}
          >
            <div className="dc-avatar">
              {doc.avatarUrl
                ? <img src={doc.avatarUrl} alt={doc.name} />
                : <span>{doc.name.split(" ").filter(w => w !== "Dr.").map(w => w[0]).slice(0,2).join("")}</span>
              }
            </div>

            <div className="dc-info">
              <div className="dc-name">{doc.name}</div>
              <div className="dc-spec">{doc.specialty}</div>
              <div className="dc-qual">{doc.qualifications}</div>

              <div className="dc-meta">
                <span className="chip chip-green">★ {doc.rating}</span>
                <span className="dc-reviews">{doc.reviewCount} {t("doctors.reviews")}</span>
                <span className="dc-exp">{doc.yearsExperience}{t("doctors.exp")}</span>
              </div>

              {doc.tags && doc.tags.length > 0 && (
                <div className="dc-tags">
                  {doc.tags.slice(0, 3).map((t) => (
                    <span key={t} className="chip chip-blue">{t}</span>
                  ))}
                </div>
              )}
            </div>

            <div className="dc-arrow">›</div>
          </div>
        ))}
      </div>
    </div>
  );
}