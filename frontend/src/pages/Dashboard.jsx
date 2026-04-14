import { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { api } from "../api/apiClient";
import { useAuth } from "../context/AuthContext";
import "./Dashboard.css";
import i18n from "../i18n"
import { LANGUAGES } from "../i18n";


function OrderMedicineModal({ prescriptions, onConfirm, onClose }) {
  const { t } = useTranslation();

  const [selected, setSelected] = useState(prescriptions[0]?.id || null);
  const [placing,  setPlacing]  = useState(false);
  const [done,     setDone]     = useState(false);
  const [err,      setErr]      = useState(null);

  const handleOrder = async () => {
    if (!selected) return;
    setPlacing(true); setErr(null);
    try {
      await onConfirm(selected);
      setDone(true);
    } catch (e) {
      setErr(e.message);
    } finally {
      setPlacing(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-box" onClick={e => e.stopPropagation()}>
        <div className="modal-title">💊 {t("dashboard.orderMedicine")}</div>
        <p className="modal-body">{t("dashboard.orderMedicineConfirm")}</p>
        {prescriptions.length > 1 && (
          <div className="modal-select-wrap">
            <label className="modal-label">{t("dashboard.selectPrescription")}</label>
            <select className="modal-select" value={selected} onChange={e => setSelected(Number(e.target.value))}>
              {prescriptions.map(p => <option key={p.id} value={p.id}>{p.title}</option>)}
            </select>
          </div>
        )}
        {err  && <div className="modal-err">{err}</div>}
        {done && <div className="modal-ok">{t("dashboard.orderPlaced")}</div>}
        {!done && (
          <div className="modal-actions">
            <button className="modal-btn modal-btn-primary" onClick={handleOrder} disabled={placing}>{placing ? "…" : t("common.confirm")}</button>
            <button className="modal-btn" onClick={onClose}>{t("common.cancel")}</button>
          </div>
        )}
        {done && <button className="modal-btn modal-btn-primary" onClick={onClose}>{t("common.close")}</button>}
      </div>
    </div>
  );
}

export default function Dashboard({ patientId }) {
  const { t } = useTranslation();

  const { logout } = useAuth();
  const [data,          setData]          = useState(null);
  const [loading,       setLoading]       = useState(true);
  const [error,         setError]         = useState(null);
  const [prescriptions, setPrescriptions] = useState([]);
  const [showOrder,     setShowOrder]     = useState(false);
  const [showLangMenu,  setShowLangMenu]  = useState(false);
  const [currentLang,   setCurrentLang]   = useState(() => localStorage.getItem("medapp_lang") || "en");

  useEffect(() => {
    setLoading(true);
    Promise.all([api.dashboard(patientId), api.records(patientId)]) 
      .then(([dash, recs]) => {
        setData(dash);
        setPrescriptions(recs.filter(r => r.type === "PRESCRIPTION"));
      })
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, [patientId]);

  const handleOrder = async (prescriptionRecordId) => {
    await api.placeOrder(patientId, { prescriptionRecordId });
  };

  const changeLang = (code) => {
    setCurrentLang(code);
    i18n.changeLanguage(code);
    localStorage.setItem("medapp_lang", code);
    setShowLangMenu(false);
  };

  if (loading) return <div className="dashboard-state">{t("common.loading")}</div>;
  if (error)   return <div className="dashboard-state dashboard-state--error">{error}</div>;
  if (!data)   return null;

  const { patient, latestVitals: v, upcomingAppointments: appts } = data;
  const today = new Date().toLocaleDateString(undefined, { weekday: "long", month: "short", day: "numeric" }).toUpperCase();
  const hour = new Date().getHours();
  const greeting = hour < 12 ? t("dashboard.greeting_morning") : hour < 17 ? t("dashboard.greeting_afternoon") : t("dashboard.greeting_evening");

  const ringDash = (r, ratio) => {
    const circ = 2 * Math.PI * r;
    return `${(circ * Math.min(ratio, 1)).toFixed(1)} ${circ.toFixed(1)}`;
  };

  const activeLang = LANGUAGES.find(l => l.code === currentLang);

  return (
    <div className="dashboard">
      {showOrder && prescriptions.length > 0 && (
        <OrderMedicineModal prescriptions={prescriptions} onConfirm={handleOrder} onClose={() => setShowOrder(false)} />
      )}

      <header className="db-header fade-up">
        <div className="db-header-left">
          <div className="header-items">
            <div className="page-date">{today}</div>
            <div className="db-greeting">{greeting} <span className="db-name">{patient.name.split(" ")[0]}</span></div>
          </div>
        </div>
      </header>

      {prescriptions.length > 0 && (
        <button className="order-medicine-btn fade-up" onClick={() => setShowOrder(true)}>
          <span className="material-symbols-outlined medication">local_shipping</span>
          {t("dashboard.orderMedicine")}
        </button>
      )}

      {v ? (
        <div className="vitals-row fade-up-1">
          <div className="vital-card">
            <div className="vital-icon vital-icon--red">♥</div>
            <div className="vital-label">{t("dashboard.heartRate")}</div>
            <div className="vital-value">{v.heartRate} <span className="vital-unit">bpm</span></div>
            <div className="vital-status chip chip-green">{t("dashboard.normalRange")}</div>
          </div>
          <div className="vital-card">
            <div className="vital-icon vital-icon--blue">◎</div>
            <div className="vital-label">{t("dashboard.bloodPressure")}</div>
            <div className="vital-value-lg">{v.bloodPressure}</div>
          </div>
          <div className="vital-card">
            <div className="vital-icon vital-icon--teal">○</div>
            <div className="vital-label">{t("dashboard.oxygen")}</div>
            <div className="vital-value">{v.oxygenSaturation}<span className="vital-unit">%</span></div>
          </div>
          <div className="vital-card">
            <div className="vital-icon vital-icon--purple">☽</div>
            <div className="vital-label">{t("dashboard.sleep")}</div>
            <div className="vital-value-lg">{v.sleepHours}h {v.sleepMinutes}m</div>
            <div className="sleep-bar"><div className="sleep-fill" style={{ width: `${Math.min((v.sleepHours / 9) * 100, 100)}%` }} /></div>
          </div>
        </div>
      ) : (
        <div className="vitals-row fade-up-1"><div className="vital-card vitals-empty">{t("dashboard.noVitals")}</div></div>
      )}

      <div className="section-row fade-up-3">
        <span className="section-label-text">{t("dashboard.upcomingAppointments")}</span>
      </div>

      {appts && appts.length > 0 ? (
        appts.map(appt => (
          <div key={appt.id} className="appt-card glass-card fade-up-3">
            <div className="appt-avatar" style={{ background: "linear-gradient(135deg,#2dd4bf,#4a90d9)" }}>
              <span>{appt.doctorName.split(" ").filter(w => w !== "Dr.").map(w => w[0]).slice(0,2).join("")}</span>
            </div>
            <div className="appt-info">
              <div className="appt-name">{appt.doctorName}</div>
              <div className="appt-spec">{appt.doctorSpecialty}</div>
              <div className="appt-time">📅 {appt.appointmentDate} · {appt.timeSlot}</div>
            </div>
            <span className={`chip chip-${appt.status === "SCHEDULED" ? "green" : "blue"}`}>{appt.status}</span>
          </div>
        ))
      ) : (
        <div className="appt-card glass-card fade-up-3 appt-empty">{t("dashboard.noAppointments")}</div>
      )}
    </div>
  );
}