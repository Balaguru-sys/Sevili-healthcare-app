import { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { api } from "../api/apiClient";
import "./Records.css";

const TYPE_META = {
  LAB_RESULT:   { icon: "🔬", color: "#2dd4bf", badgeClass: "chip-new",    badge: "LAB"  },
  PRESCRIPTION: { icon: "💊", color: "#a78bfa", badgeClass: "chip-purple", badge: "Rx"   },
  IMAGING:      { icon: "🫁", color: "#fb7185", badgeClass: "chip-red",    badge: "SCAN" },
  OTHER:        { icon: "📄", color: "#60a5fa", badgeClass: "chip-blue",   badge: "DOC"  },
};

function FileViewer({ record, onClose }) {
  const { t } = useTranslation();
  const url = record.fileUrl;
  const type = record.fileType || "OTHER";

  return (
    <div className="viewer-overlay" onClick={onClose}>
      <div className="viewer-box" onClick={e => e.stopPropagation()}>
        <div className="viewer-header">
          <span className="viewer-title">{record.title}</span>
          <button className="viewer-close" onClick={onClose}>✕</button>
        </div>
        <div className="viewer-body">
          {type === "PDF" && (
            <iframe
              src={url}
              title={record.title}
              className="viewer-iframe"
              allow="fullscreen"
            />
          )}
          {type === "IMAGE" && (
            <img src={url} alt={record.title} className="viewer-image" />
          )}
          {type === "TEXT" && (
            <TextViewer url={url} />
          )}
          {(type === "OTHER" || !type) && (
            <div className="viewer-fallback">
              <p>Preview not available for this file type.</p>
              <a href={url} target="_blank" rel="noopener noreferrer" className="viewer-link">
                Open in new tab ↗
              </a>
            </div>
          )}
        </div>
        <div className="viewer-footer">
          <a href={url} target="_blank" rel="noopener noreferrer" className="viewer-btn">
            {t("records.viewFile")} ↗
          </a>
        </div>
      </div>
    </div>
  );
}

function TextViewer({ url }) {
  const [text, setText] = useState(null);
  const [err, setErr]   = useState(false);

  useEffect(() => {
    fetch(url)
      .then(r => r.text())
      .then(setText)
      .catch(() => setErr(true));
  }, [url]);

  if (err)  return <div className="viewer-fallback">Could not load text file.</div>;
  if (!text) return <div className="viewer-fallback">Loading…</div>;
  return <pre className="viewer-text">{text}</pre>;
}

export default function Records({ patientId }) {
  const { t } = useTranslation();
  const [records,  setRecords]  = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState(null);
  const [viewing,  setViewing]  = useState(null);

  useEffect(() => {
    api.records(patientId)
      .then(setRecords)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, [patientId]);

  const handleClick = (record) => {
    if (record.fileUrl) setViewing(record);
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return "—";
    try {
      return new Date(dateStr).toLocaleDateString(undefined, {
        month: "short", day: "numeric", year: "numeric",
      });
    } catch { return dateStr; }
  };

  return (
    <div className="records-page">
      {viewing && <FileViewer record={viewing} onClose={() => setViewing(null)} />}

      <header className="page-header fade-up">
        <div>
          <div className="page-date">{t("records.subtitle")}</div>
          <div className="page-title">{t("records.title")}</div>
        </div>
        <div className="shield-badge"><span>🛡</span></div>
      </header>

      <div className="records-hero glass-card fade-up-1">
        <div className="rh-row">
          <div className="rh-item">
            <div className="rh-icon">🔒</div>
            <div className="rh-big">{t("records.encrypted")}</div>
            <div className="rh-sub">{t("records.endToEnd")}</div>
          </div>
          <div className="rh-divider" />
          <div className="rh-item">
            <div className="rh-icon" style={{ color: "#2dd4bf" }}>📋</div>
            <div className="rh-big">{loading ? "—" : records.length}</div>
            <div className="rh-sub">{t("records.totalRecords")}</div>
          </div>
        </div>
        <div className="rh-desc">{t("records.secureHistory")}</div>
      </div>

      {loading && <div className="records-state fade-up-2">{t("common.loading")}</div>}
      {!loading && error && <div className="records-state records-state--error fade-up-2">{error}</div>}
      {!loading && !error && records.length === 0 && (
        <div className="records-state fade-up-2">{t("records.noRecords")}</div>
      )}

      {!loading && !error && records.length > 0 && (
        <div className="records-list fade-up-3">
          {records.map(r => {
            const meta = TYPE_META[r.type] || TYPE_META.OTHER;
            return (
              <div
                key={r.id}
                className={`record-item glass-card ${r.fileUrl ? "record-item--clickable" : ""}`}
                onClick={() => handleClick(r)}
              >
                <div className="ri-icon" style={{
                  background: `${meta.color}18`,
                  border: `1px solid ${meta.color}30`,
                }}>
                  <span>{meta.icon}</span>
                </div>
                <div className="ri-info">
                  <div className="ri-title-row">
                    <span className="ri-title">{r.title}</span>
                    <span className={`chip ${meta.badgeClass}`}>{meta.badge}</span>
                    {r.fileType && (
                      <span className="chip chip-blue" style={{ fontSize: 8 }}>{r.fileType}</span>
                    )}
                  </div>
                  <div className="ri-meta">
                    {formatDate(r.uploadedAt)}
                    {r.uploadedBy ? ` · ${r.uploadedBy}` : ""}
                  </div>
                </div>
                {r.fileUrl && <div className="ri-arrow">›</div>}
              </div>
            );
          })}
        </div>
      )}

      <div className="privacy-card glass-card fade-up-4">
        <div className="priv-title">🛡 {t("records.privacy")}</div>
        <div className="priv-body">{t("records.privacyBody")}</div>
        <div className="priv-bar"><div className="priv-fill" /></div>
      </div>
    </div>
  );
}
