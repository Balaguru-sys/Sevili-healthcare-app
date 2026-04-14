const BASE = import.meta.env.VITE_API_BASE_URL || "";

function getToken() {
  return localStorage.getItem("token");
}

async function request(method, path, body) {
  const headers = { "Content-Type": "application/json" };
  const token = getToken();
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const res = await fetch(`${BASE}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    let msg = `Request failed: ${res.status}`;
    try { const err = await res.json(); msg = err.message || msg; } catch {}
    throw new Error(msg);
  }

  if (res.status === 204) return null;
  return res.json();
}

async function uploadFile(path, formData) {
  const token = getToken();
  const headers = {};
  if (token) headers["Authorization"] = `Bearer ${token}`;
  const res = await fetch(`${BASE}${path}`, { method: "POST", headers, body: formData });
  if (!res.ok) {
    let msg = `Upload failed: ${res.status}`;
    try { const err = await res.json(); msg = err.message || msg; } catch {}
    throw new Error(msg);
  }
  return res.json();
}

export const api = {
  // ── Auth ──────────────────────────────────────────────
  login:    (data) => request("POST", "/api/auth/login", data),
  register: (data) => request("POST", "/api/auth/register", data),
  me:       ()     => request("GET",  "/api/auth/me"),

  // ── Patient ───────────────────────────────────────────
  dashboard:         (id)       => request("GET",   `/api/patients/${id}/dashboard`),
  records:           (id)       => request("GET",   `/api/patients/${id}/records`),
  aiContext:         (id)       => request("GET",   `/api/patients/${id}/ai-context`),
  appointments:      (id)       => request("GET",   `/api/patients/${id}/appointments`),
  upcomingAppts:     (id)       => request("GET",   `/api/patients/${id}/appointments/upcoming`),
  bookAppointment:   (id, data) => request("POST",  `/api/patients/${id}/appointments`, data),
  cancelAppointment: (patientId, apptId) =>
      request("PATCH", `/api/patients/${patientId}/appointments/${apptId}/cancel`),

  // ── Medicine orders (patient) ─────────────────────────
  placeOrder:    (patientId, data) => request("POST", `/api/patients/${patientId}/orders`, data),
  patientOrders: (patientId)       => request("GET",  `/api/patients/${patientId}/orders`),

  // ── Doctors ───────────────────────────────────────────
  doctors:     ()   => request("GET", "/api/doctors"),
  doctor:      (id) => request("GET", `/api/doctors/${id}`),
  doctorSlots: (id) => request("GET", `/api/doctors/${id}/availability`),

  // ── Chat ──────────────────────────────────────────────
  sendMessage: (data)      => request("POST", "/api/chat/message", data),
  chatHistory: (patientId) => request("GET",  `/api/chat/history/${patientId}`),

  // ── Emergency ─────────────────────────────────────────
  triggerSos:       (data)      => request("POST", "/api/emergency/sos", data),
  emergencyHistory: (patientId) => request("GET",  `/api/emergency/history/${patientId}`),

  // ── Staff: patients ───────────────────────────────────
  staffPatients:      ()         => request("GET",    "/api/staff/patients"),
  staffGetPatient:    (id)       => request("GET",    `/api/staff/patients/${id}`),
  staffCreatePatient: (data)     => request("POST",   "/api/staff/patients", data),
  staffUpdatePatient: (id, data) => request("PUT",    `/api/staff/patients/${id}`, data),
  staffDeletePatient: (id)       => request("DELETE", `/api/staff/patients/${id}`),

  // ── Staff: patient detail sub-data ───────────────────
  staffPatientRecords: (id) => request("GET", `/api/staff/patients/${id}/records`),
  staffPatientVitals:  (id) => request("GET", `/api/staff/patients/${id}/vitals`),
  staffPatientVitalsLatest: (id) => request("GET", `/api/staff/patients/${id}/vitals/latest`),
  staffPatientAppts:   (id) => request("GET", `/api/patients/${id}/appointments`),
  staffPatientAlerts:  (id) => request("GET", `/api/emergency/history/${id}`),
  staffPatientOrders:  (id) => request("GET", `/api/patients/${id}/orders`),

  // ── Staff: doctors ────────────────────────────────────
  staffCreateDoctor: (data)     => request("POST",   "/api/staff/doctors", data),
  staffUpdateDoctor: (id, data) => request("PUT",    `/api/staff/doctors/${id}`, data),
  staffDeleteDoctor: (id)       => request("DELETE", `/api/staff/doctors/${id}`),

  // ── Staff: records (Cloudinary) ───────────────────────
  staffUploadRecordFile: (patientId, formData) =>
      uploadFile(`/api/staff/patients/${patientId}/records`, formData),
  staffGetRecords: (patientId) =>
      request("GET", `/api/staff/patients/${patientId}/records`),

  // ── Staff: vitals ─────────────────────────────────────
  staffUploadVitals: (patientId, data) =>
      request("POST", `/api/staff/patients/${patientId}/vitals`, data),
  staffGetVitals: (patientId) =>
      request("GET", `/api/staff/patients/${patientId}/vitals`),

  // ── Staff: appointments ───────────────────────────────
  staffAppointments:  ()    => request("GET",   "/api/staff/appointments"),
  staffApproveAppt:   (id)  => request("PATCH", `/api/staff/appointments/${id}/approve`),
  staffRejectAppt:    (id)  => request("PATCH", `/api/staff/appointments/${id}/reject`),
  staffCompleteAppt:  (id)  => request("PATCH", `/api/staff/appointments/${id}/complete`),

  // ── Staff: emergency ──────────────────────────────────
  staffActiveAlerts:  ()    => request("GET",   "/api/staff/emergency"),
  staffAllAlerts:     ()    => request("GET",   "/api/staff/emergency/all"),
  staffResolveAlert:  (id)  => request("PATCH", `/api/staff/emergency/${id}/resolve`),

  // ── Staff: medicine orders ────────────────────────────
  staffOrders:        ()    => request("GET",   "/api/staff/orders"),
  staffDispatchOrder: (id)  => request("PATCH", `/api/staff/orders/${id}/dispatch`),
  staffDeliverOrder:  (id)  => request("PATCH", `/api/staff/orders/${id}/deliver`),
};
