import { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { api } from "../api/apiClient";
import "./Emergency.css";

export default function Emergency({ patientId }) {
  const { t } = useTranslation();

  const [alerting,   setAlerting]   = useState(false);
  const [alertId,    setAlertId]    = useState(null);
  const [coords,     setCoords]     = useState({ lat: null, lng: null });
  const [countdown,  setCountdown]  = useState(null);
  const [sosError,   setSosError]   = useState(null);
  const [resolving,  setResolving]  = useState(false);
  const [resolved,   setResolved]   = useState(false);

  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => setCoords({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
        () => {}
      );
    }
  }, []);

  const triggerSOS = async () => {
    if (alerting) return;
    setSosError(null);
    setAlerting(true);
    setResolved(false);
    setCountdown(5);

    const timer = setInterval(() => {
      setCountdown((c) => {
        if (c <= 1) { clearInterval(timer); return null; }
        return c - 1;
      });
    }, 1000);

    try {
      const res = await api.triggerSos({
        patientId,
        lat:      coords.lat  != null ? String(coords.lat)  : null,
        lng:      coords.lng  != null ? String(coords.lng)  : null,
        location: coords.lat  != null
          ? `${coords.lat.toFixed(4)}°N, ${coords.lng.toFixed(4)}°W`
          : "Location unavailable",
      });
      setAlertId(res.alertId);
    } catch (err) {
      setSosError(err.message);
      setAlerting(false);
      clearInterval(timer);
      setCountdown(null);
    }
  };

  const cancelSOS = async () => {
    if (alertId) {
      setResolving(true);
      try {
        await api.staffResolveAlert(alertId);
      } catch {}
      setResolving(false);
    }
    setAlerting(false);
    setAlertId(null);
    setCountdown(null);
    setResolved(true);
  };

  return (
    <div className={`emergency-page ${alerting ? "emergency-page--active" : ""}`}>

      {/* SOS Button */}
      <div className="sos-center fade-up">
        <div className={`sos-ring-outer ${alerting ? "sos-ring-outer--pulse" : ""}`}>
          <div className="sos-ring-inner">
            <button className="sos-btn" onClick={alerting ? cancelSOS : triggerSOS}>
              <div className="sos-text">SOS</div>

              <div className="sos-sub">
                {alerting
                  ? (countdown ? t("emergency.alertingCountdown", { count: countdown }) : t("emergency.alerting"))
                  : t("emergency.sosBtnLabel")}
              </div>

            </button>
          </div>
        </div>
      </div>

      <div className="sos-headline fade-up-1">
        {resolved
          ? t("emergency.resolvedTitle")
          : alerting
            ? t("emergency.activeTitle")
            : t("emergency.title")}
      </div>

      <div className="sos-subline fade-up-1">
        {resolved
          ? t("emergency.resolvedSubtext")
          : alerting
            ? t("emergency.activeSubtext")
            : t("emergency.subtext")}
      </div>

      {sosError && (
        <div className="sos-error fade-up-1">{sosError}</div>
      )}

      {/* Call Ambulance */}
      <div className="sos-actions fade-up-2">
        <button
          className={`btn-danger ${!alerting ? "btn-danger--dim" : ""}`}
          style={{ width: "100%" }}
          onClick={triggerSOS}
          disabled={alerting}
        >
          <span>📡</span> {t("emergency.callAmbulance")}
        </button>
      </div>

      {/* Secondary Actions */}
      <div className="sos-secondary fade-up-3">

        <a href="tel:911" className="sos-sec-btn glass-card">
          <span className="sos-sec-icon">📞</span>
          <span>{t("emergency.callEmergency")}</span>
        </a>

        <button
          className="sos-sec-btn glass-card"
          onClick={() => {
            if (coords.lat) {
              navigator.clipboard?.writeText(
                `${coords.lat.toFixed(6)}, ${coords.lng.toFixed(6)}`
              );
            }
          }}
        >
          <span className="sos-sec-icon">📍</span>
          <span>{t("emergency.copyLocation")}</span>
        </button>

      </div>

      {/* Location Card */}
      <div className="location-card glass-card fade-up-4">

        <div className="loc-icon">📍</div>

        <div className="loc-info">

          <div className="loc-eyebrow">
            {t("emergency.coordinates")}
          </div>

          {coords.lat != null ? (
            <>
              <div className="loc-place">
                {t("emergency.gpsAcquired")}
              </div>

              <div className="loc-coords">
                {coords.lat.toFixed(5)}°N, {coords.lng.toFixed(5)}°W
              </div>
            </>
          ) : (
            <>
              <div className="loc-place">
                {t("emergency.locationUnavailable")}
              </div>

              <div className="loc-coords">
                {t("emergency.enableLocation")}
              </div>
            </>
          )}

        </div>

        <span className={`chip ${coords.lat != null ? "chip-green" : "chip-red"}`}>
          {coords.lat != null ? "VERIFIED" : "UNKNOWN"}
        </span>

      </div>
    </div>
  );
}