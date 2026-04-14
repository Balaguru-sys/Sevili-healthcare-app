import { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { api } from "../api/apiClient";
import "./DoctorProfile.css";

export default function DoctorProfile({ doctorId, patientId, onBack }) {
  const { t } = useTranslation();

  const [doctor,   setDoctor]   = useState(null);
  const [slots,    setSlots]    = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState(null);

  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [booking,      setBooking]      = useState(false);
  const [booked,       setBooked]       = useState(false);
  const [bookError,    setBookError]    = useState(null);

  useEffect(() => {
    if (!doctorId) return;
    setLoading(true);

    Promise.all([api.doctor(doctorId), api.doctorSlots(doctorId)])
      .then(([doc, availability]) => {
        setDoctor(doc);
        setSlots(availability.filter((s) => s.available));
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [doctorId]);

  const slotsByDate = slots.reduce((acc, s) => {
    if (!acc[s.slotDate]) acc[s.slotDate] = [];
    acc[s.slotDate].push(s);
    return acc;
  }, {});

  const availableDates = Object.keys(slotsByDate).slice(0, 7);
  const currentDateKey = selectedDate || availableDates[0] || null;
  const currentSlots   = currentDateKey ? slotsByDate[currentDateKey] || [] : [];

  const formatDateLabel = (dateStr) => {
    if (!dateStr) return "";
    const d = new Date(dateStr + "T00:00:00");
    return {
      day:  d.toLocaleDateString("en-US", { weekday: "short" }).toUpperCase(),
      date: d.getDate(),
    };
  };

  const handleBook = async () => {
    if (!selectedSlot || !currentDateKey) return;

    setBooking(true);
    setBookError(null);

    try {
      await api.bookAppointment(patientId, {
        doctorId,
        date: currentDateKey,
        slot: selectedSlot.timeSlot,
      });

      setBooked(true);
      setSlots((prev) => prev.filter((s) => s.id !== selectedSlot.id));
      setSelectedSlot(null);

    } catch (err) {
      setBookError(err.message);
    } finally {
      setBooking(false);
    }
  };

  if (loading) return <div className="profile-state">{t("common.loading")}</div>;
  if (error)   return <div className="profile-state profile-state--error">{error}</div>;
  if (!doctor) return null;

  return (
    <div className="doctor-page">

      {/* Back button */}
      <button className="back-btn fade-up" onClick={onBack}>
        ← {t("common.back")}
      </button>

      {/* Hero */}
      <div className="doc-hero fade-up">
        <div className="doc-hero-img">
          <div className="doc-hero-gradient" />

          <div className="doc-hero-info">
            <span className="chip chip-purple" style={{ marginBottom: 8 }}>
              {t("doctors.subtitle")}
            </span>

            <div className="doc-name">{doctor.name}</div>

            <div className="doc-spec">
              {doctor.specialty}
              {doctor.qualifications ? `, ${doctor.qualifications}` : ""}
            </div>

            <div className="doc-rating">
              ★ {doctor.rating}{" "}
              <span className="doc-reviews">
                {doctor.reviewCount}+ {t("doctors.reviews")}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Stats */}
      <div className="doc-stats fade-up-1">
        {[
          { val: `${doctor.yearsExperience}+`, label: t("doctors.exp") },
          { val: doctor.surgeries >= 1000 ? `${(doctor.surgeries/1000).toFixed(1)}k` : String(doctor.surgeries), label: t("doctors.surgeries") },
          { val: String(doctor.awards), label: t("doctors.awards") },
        ].map((s) => (
          <div key={s.label} className="stat-box glass-card">
            <div className="stat-val">{s.val}</div>
            <div className="stat-label">{s.label}</div>
          </div>
        ))}
      </div>

      {/* About */}
      <div className="doc-about glass-card fade-up-2">
        <p className="doc-bio">{doctor.bio}</p>

        {doctor.tags && doctor.tags.length > 0 && (
          <div className="doc-tags">
            {doctor.tags.map((tag) => (
              <span key={tag} className="chip chip-blue">{tag}</span>
            ))}
          </div>
        )}
      </div>

      {/* Availability */}
      <div className="section-row fade-up-3" style={{ padding: "0 16px", marginTop: 20 }}>
        <span className="section-label-text">
          {t("doctors.availability")}
        </span>
      </div>

      {availableDates.length === 0 ? (
        <div className="no-slots glass-card fade-up-3">
          {t("doctors.noSlots")}
        </div>
      ) : (
        <>
          <div className="day-picker fade-up-3">
            {availableDates.map((dateKey) => {
              const { day, date } = formatDateLabel(dateKey);

              return (
                <button
                  key={dateKey}
                  className={`day-btn ${currentDateKey === dateKey ? "day-btn--active" : ""}`}
                  onClick={() => { setSelectedDate(dateKey); setSelectedSlot(null); }}
                >
                  <div className="day-name">{day}</div>
                  <div className="day-num">{date}</div>
                </button>
              );
            })}
          </div>

          <div className="slot-grid fade-up-4">
            {currentSlots.map((slot) => (
              <button
                key={slot.id}
                className={`slot-btn ${selectedSlot?.id === slot.id ? "slot-btn--active" : ""}`}
                onClick={() => setSelectedSlot(slot)}
              >
                {slot.timeSlot}
              </button>
            ))}
          </div>
        </>
      )}

      {/* CTA */}
      <div className="doc-cta fade-up-5">

        {bookError && <div className="book-error">{bookError}</div>}

        {booked && (
          <div className="book-success">
            ✓ {t("doctors.bookSuccess")}
          </div>
        )}

        <button
          className={`btn-primary ${booked ? "btn-booked" : ""}`}
          style={{ width: "100%", fontSize: 16, padding: "16px" }}
          onClick={handleBook}
          disabled={!selectedSlot || booking || booked || availableDates.length === 0}
        >
          {booking
            ? t("doctors.booking")
            : booked
              ? `✓ ${t("doctors.booked")}`
              : `📅 ${t("doctors.bookConsultation")}`}
        </button>

        {!selectedSlot && !booked && availableDates.length > 0 && (
          <div className="cta-hint">
            {t("doctors.selectSlot")}
          </div>
        )}

      </div>
    </div>
  );
}