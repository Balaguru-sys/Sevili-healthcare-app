import { useState, useEffect, useCallback, useMemo } from "react";
import { useAuth } from "../../context/AuthContext";
import { api } from "../../api/apiClient";
import "./StaffDashboard.css";

// ── Shared search bar component ───────────────────────────────────────────────
function SearchBar({ value, onChange, placeholder }) {
  return (
    <div className="sd-search-wrap">
      <span className="sd-search-icon">🔍</span>
      <input
        className="sd-search-input"
        type="text"
        placeholder={placeholder || "Search…"}
        value={value}
        onChange={e => onChange(e.target.value)}
      />
      {value && <button className="sd-search-clear" onClick={() => onChange("")}>✕</button>}
    </div>
  );
}

// ── Patient Detail Modal (Update 3) ──────────────────────────────────────────
function PatientDetailModal({ patient, onClose }) {
  const [records,  setRecords]  = useState([]);
  const [vitals,   setVitals]   = useState([]);
  const [appts,    setAppts]    = useState([]);
  const [alerts,   setAlerts]   = useState([]);
  const [orders,   setOrders]   = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [tab,      setTab]      = useState("overview");

  useEffect(() => {
    setLoading(true);
    Promise.allSettled([
      api.staffPatientRecords(patient.id),
      api.staffPatientVitals(patient.id),
      api.staffPatientAppts(patient.id),
      api.staffPatientAlerts(patient.id),
      api.staffPatientOrders(patient.id),
    ]).then(([r, v, a, e, o]) => {
      setRecords(r.status === "fulfilled" ? r.value : []);
      setVitals(v.status === "fulfilled" ? v.value : []);
      setAppts(a.status === "fulfilled" ? a.value : []);
      setAlerts(e.status === "fulfilled" ? e.value : []);
      setOrders(o.status === "fulfilled" ? o.value : []);
    }).finally(() => setLoading(false));
  }, [patient.id]);

  const latestVitals = vitals[0];

  return (
    <div className="sd-modal-overlay" onClick={onClose}>
      <div className="sd-modal-panel" onClick={e => e.stopPropagation()}>
        <div className="sd-modal-header">
          <div className="sd-modal-avatar">{patient.name.split(" ").map(w=>w[0]).slice(0,2).join("")}</div>
          <div>
            <div className="sd-modal-name">{patient.name}</div>
            <div className="sd-modal-email">{patient.email}</div>
            <div className="sd-modal-meta">
              {patient.phone && <span>📞 {patient.phone}</span>}
              {patient.language && <span className="sd-badge" style={{marginLeft:8}}>{patient.language}</span>}
            </div>
          </div>
          <button className="sd-modal-close" onClick={onClose}>✕</button>
        </div>

        <div className="sd-modal-tabs">
          {["overview","records","appointments","alerts","orders"].map(t => (
            <button key={t} className={`sd-modal-tab ${tab===t?"sd-modal-tab--active":""}`} onClick={()=>setTab(t)}>
              {t.charAt(0).toUpperCase()+t.slice(1)}
            </button>
          ))}
        </div>

        <div className="sd-modal-body">
          {loading ? <div className="sd-loading">Loading patient data…</div> : (
            <>
              {tab === "overview" && (
                <div>
                  <h4 className="sd-modal-section-title">Latest Vitals</h4>
                  {latestVitals ? (
                    <div className="sd-vitals-grid">
                      {[
                        { label: "Heart Rate", value: `${latestVitals.heartRate} bpm`, color: "#fb7185" },
                        { label: "Blood Pressure", value: latestVitals.bloodPressure, color: "#60a5fa" },
                        { label: "SpO₂", value: `${latestVitals.oxygenSaturation}%`, color: "#2dd4bf" },
                        { label: "Sleep", value: `${latestVitals.sleepHours}h ${latestVitals.sleepMinutes}m`, color: "#a78bfa" },
                      ].map(v => (
                        <div key={v.label} className="sd-vital-chip" style={{borderColor:`${v.color}30`}}>
                          <div className="sd-vital-chip-val" style={{color:v.color}}>{v.value}</div>
                          <div className="sd-vital-chip-lbl">{v.label}</div>
                        </div>
                      ))}
                    </div>
                  ) : <div className="sd-empty">No vitals recorded.</div>}

                  <h4 className="sd-modal-section-title" style={{marginTop:16}}>Summary</h4>
                  <div className="sd-summary-row">
                    <div className="sd-summary-chip"><div className="sd-summary-val">{records.length}</div><div className="sd-summary-lbl">Records</div></div>
                    <div className="sd-summary-chip"><div className="sd-summary-val">{appts.length}</div><div className="sd-summary-lbl">Appointments</div></div>
                    <div className="sd-summary-chip"><div className="sd-summary-val">{alerts.filter(a=>a.status==="ACTIVE").length}</div><div className="sd-summary-lbl">Active Alerts</div></div>
                    <div className="sd-summary-chip"><div className="sd-summary-val">{orders.filter(o=>o.status==="PENDING").length}</div><div className="sd-summary-lbl">Pending Orders</div></div>
                  </div>
                </div>
              )}

              {tab === "records" && (
                <div>
                  {records.length === 0 ? <div className="sd-empty">No records.</div> : (
                    <table className="sd-table"><thead><tr><th>Type</th><th>Title</th><th>Uploaded</th><th>File</th></tr></thead>
                    <tbody>{records.map(r=>(
                      <tr key={r.id}><td><span className="sd-badge">{r.type}</span></td><td>{r.title}</td><td>{r.uploadedAt}</td><td>{r.fileUrl?<a href={r.fileUrl} target="_blank" rel="noopener noreferrer" className="sd-file-link">View ↗</a>:"—"}</td></tr>
                    ))}</tbody></table>
                  )}
                </div>
              )}

              {tab === "appointments" && (
                <div>
                  {appts.length === 0 ? <div className="sd-empty">No appointments.</div> : (
                    <table className="sd-table"><thead><tr><th>Doctor</th><th>Date</th><th>Time</th><th>Status</th></tr></thead>
                    <tbody>{appts.map(a=>(
                      <tr key={a.id}><td>{a.doctorName}</td><td>{a.appointmentDate}</td><td>{a.timeSlot}</td><td><span className="sd-status" style={{color:a.status==="SCHEDULED"?"#60a5fa":a.status==="COMPLETED"?"#34d399":"#fb7185"}}>{a.status}</span></td></tr>
                    ))}</tbody></table>
                  )}
                </div>
              )}

              {tab === "alerts" && (
                <div>
                  {alerts.length === 0 ? <div className="sd-empty">No emergency alerts.</div> : (
                    <div className="sd-alert-list">
                      {alerts.map(a=>(
                        <div key={a.id} className={`sd-emergency-card ${a.status==="ACTIVE"?"sd-emergency-card--active":""}`}>
                          <div className="sd-emergency-icon">{a.status==="ACTIVE"?"🚨":"✅"}</div>
                          <div className="sd-emergency-info">
                            <div className="sd-emergency-loc">📍 {a.locationDescription||"Location unknown"}</div>
                            <div className="sd-emergency-time">Triggered: {a.triggeredAt}</div>
                          </div>
                          <span className={`sd-status-badge ${a.status==="ACTIVE"?"sd-status-badge--danger":"sd-status-badge--ok"}`}>{a.status}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {tab === "orders" && (
                <div>
                  {orders.length === 0 ? <div className="sd-empty">No medicine orders.</div> : (
                    <table className="sd-table"><thead><tr><th>Prescription</th><th>Status</th><th>Ordered</th></tr></thead>
                    <tbody>{orders.map(o=>(
                      <tr key={o.id}><td>{o.prescriptionTitle}</td><td><span className="sd-status" style={{color:o.status==="PENDING"?"#fb7185":o.status==="DISPATCHED"?"#60a5fa":"#34d399"}}>{o.status}</span></td><td>{o.createdAt}</td></tr>
                    ))}</tbody></table>
                  )}
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}

// ── Overview (Update 5: medicine orders) ─────────────────────────────────────
function Overview({ onNavigate }) {
  const [stats,  setStats]  = useState(null);
  const [alerts, setAlerts] = useState([]);
  const [orders, setOrders] = useState([]);

  useEffect(() => {
    Promise.allSettled([
      api.staffPatients(),
      api.doctors(),
      api.staffAppointments(),
      api.staffActiveAlerts(),
      api.staffOrders(),
    ]).then(([patients, doctors, appts, activeAlerts, ord]) => {
      const p = patients.status==="fulfilled"?patients.value:[];
      const d = doctors.status==="fulfilled"?doctors.value:[];
      const a = appts.status==="fulfilled"?appts.value:[];
      const al = activeAlerts.status==="fulfilled"?activeAlerts.value:[];
      const o = ord.status==="fulfilled"?ord.value:[];
      setStats({ patients:p.length, doctors:d.length, appointments:a.length, pending:a.filter(x=>x.status==="SCHEDULED").length });
      setAlerts(al.slice(0,3));
      setOrders(o);
    });
  }, []);

  const orderCounts = useMemo(() => ({
    pending:    orders.filter(o=>o.status==="PENDING").length,
    dispatched: orders.filter(o=>o.status==="DISPATCHED").length,
    delivered:  orders.filter(o=>o.status==="DELIVERED").length,
  }), [orders]);

  return (
    <div className="sd-section">
      <div className="sd-overview-logo">
        <img src="/sevili-logo1.png" alt="Sevili" className="sd-brand-logo" />
        <span className="sd-brand-name">Sevili Staff</span>
      </div>

      {stats && (
        <div className="sd-stat-grid">
          {[
            { label:"Total Patients", value:stats.patients, icon:"👥", color:"#2dd4bf" },
            { label:"Doctors", value:stats.doctors, icon:"🩺", color:"#a78bfa" },
            { label:"Appointments", value:stats.appointments, icon:"📅", color:"#60a5fa" },
            { label:"Pending Appts", value:stats.pending, icon:"⏳", color:"#fb7185" },
          ].map(s => (
            <div className="sd-stat-card" key={s.label} style={{borderColor:`${s.color}30`}}>
              <div className="sd-stat-icon" style={{color:s.color}}>{s.icon}</div>
              <div className="sd-stat-value" style={{color:s.color}}>{s.value}</div>
              <div className="sd-stat-label">{s.label}</div>
            </div>
          ))}
        </div>
      )}

      {/* Medicine order summary cards */}
      <h3 className="sd-sub-title" style={{marginTop:24,marginBottom:12}}>💊 Medicine Orders</h3>
      <div className="sd-stat-grid">
        {[
          { label:"Pending Orders",    value:orderCounts.pending,    icon:"⏳", color:"#fb7185" },
          { label:"Dispatched Orders", value:orderCounts.dispatched, icon:"🚚", color:"#60a5fa" },
          { label:"Delivered Orders",  value:orderCounts.delivered,  icon:"✅", color:"#34d399" },
        ].map(s => (
          <div className="sd-stat-card" key={s.label} style={{borderColor:`${s.color}30`,cursor:"pointer"}} onClick={()=>onNavigate("orders")}>
            <div className="sd-stat-icon" style={{color:s.color}}>{s.icon}</div>
            <div className="sd-stat-value" style={{color:s.color}}>{s.value}</div>
            <div className="sd-stat-label">{s.label}</div>
          </div>
        ))}
      </div>

      {alerts.length > 0 && (
        <div className="sd-alert-banner" style={{marginTop:20}}>
          <div className="sd-alert-title">🚨 {alerts.length} Active Emergency Alert{alerts.length>1?"s":""}</div>
          {alerts.map(a => (
            <div className="sd-alert-row" key={a.id}>
              <span className="sd-alert-name">{a.patientName}</span>
              <span className="sd-alert-loc">{a.locationDescription||"Location unknown"}</span>
              <span className="sd-alert-time">{a.triggeredAt}</span>
              <button className="sd-btn sd-btn-danger-sm" onClick={()=>onNavigate("emergency")}>Respond</button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

// ── Patients (Updates 2, 3, 6) ────────────────────────────────────────────────
function PatientsView() {
  const [patients,       setPatients]       = useState([]);
  const [loading,        setLoading]        = useState(true);
  const [showForm,       setShowForm]       = useState(false);
  const [editingPatient, setEditingPatient] = useState(null);
  const [detailPatient,  setDetailPatient]  = useState(null);
  const [form,           setForm]           = useState({ name:"",email:"",password:"",phone:"",language:"EN" });
  const [saving,         setSaving]         = useState(false);
  const [msg,            setMsg]            = useState(null);
  const [search,         setSearch]         = useState("");

  const load = useCallback(() => {
    setLoading(true);
    api.staffPatients().then(setPatients).finally(()=>setLoading(false));
  }, []);

  useEffect(()=>{ load(); }, [load]);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return patients;
    return patients.filter(p =>
      p.name.toLowerCase().includes(q) ||
      p.email.toLowerCase().includes(q) ||
      String(p.id).includes(q)
    );
  }, [patients, search]);

  const openCreate = () => {
    setEditingPatient(null);
    setForm({ name:"",email:"",password:"",phone:"",language:"EN" });
    setShowForm(true); setMsg(null);
  };

  const openEdit = (p) => {
    setEditingPatient(p);
    setForm({ name:p.name, email:p.email, password:"", phone:p.phone||"", language:p.language||"EN" });
    setShowForm(true); setMsg(null);
  };

  const submit = async (e) => {
    e.preventDefault(); setSaving(true); setMsg(null);
    try {
      if (editingPatient) {
        await api.staffUpdatePatient(editingPatient.id, form);
        setMsg({type:"ok",text:"Patient updated."});
      } else {
        await api.staffCreatePatient(form);
        setMsg({type:"ok",text:"Patient created."});
      }
      setShowForm(false); setEditingPatient(null); load();
    } catch (err) { setMsg({type:"err",text:err.message}); }
    finally { setSaving(false); }
  };

  const deletePatient = async (p) => {
    if (!window.confirm(`Delete patient "${p.name}"? This cannot be undone.`)) return;
    try { await api.staffDeletePatient(p.id); load(); }
    catch (err) { setMsg({type:"err",text:err.message}); }
  };

  return (
    <div className="sd-section">
      {detailPatient && <PatientDetailModal patient={detailPatient} onClose={()=>setDetailPatient(null)} />}

      <div className="sd-section-header">
        <h2 className="sd-section-title">Patients</h2>
        <button className="sd-btn sd-btn-primary" onClick={openCreate}>+ New Patient</button>
      </div>

      <SearchBar value={search} onChange={setSearch} placeholder="Search by name, email or ID…" />

      {msg && <div className={`sd-msg ${msg.type==="ok"?"sd-msg-ok":"sd-msg-err"}`}>{msg.text}</div>}

      {showForm && (
        <form className="sd-form" onSubmit={submit}>
          <h3 className="sd-form-title">{editingPatient?"Edit Patient":"Create Patient Account"}</h3>
          <div className="sd-form-grid">
            {[{label:"Full Name",key:"name",type:"text",required:!editingPatient},{label:"Email",key:"email",type:"email",required:!editingPatient},{label:editingPatient?"New Password (leave blank to keep)":"Temporary Password",key:"password",type:"password",required:!editingPatient},{label:"Phone",key:"phone",type:"text"}].map(f=>(
              <div className="sd-field" key={f.key}><label className="sd-label">{f.label}</label><input className="sd-input" type={f.type} required={f.required} value={form[f.key]} onChange={e=>setForm(v=>({...v,[f.key]:e.target.value}))} /></div>
            ))}
            <div className="sd-field"><label className="sd-label">Language</label><select className="sd-input" value={form.language} onChange={e=>setForm(v=>({...v,language:e.target.value}))}>{["EN","TA","HI","ML","TE"].map(l=><option key={l}>{l}</option>)}</select></div>
          </div>
          <div className="sd-form-actions">
            <button className="sd-btn sd-btn-primary" type="submit" disabled={saving}>{saving?"Saving…":editingPatient?"Update Patient":"Create Patient"}</button>
            <button className="sd-btn" type="button" onClick={()=>{setShowForm(false);setEditingPatient(null);}}>Cancel</button>
          </div>
        </form>
      )}

      {loading ? <div className="sd-loading">Loading patients…</div> : (
        <div className="sd-table-wrap">
          <table className="sd-table">
            <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Language</th><th>Actions</th></tr></thead>
            <tbody>
              {filtered.map(p=>(
                <tr key={p.id}>
                  <td>
                    <button className="sd-patient-name-btn" onClick={()=>setDetailPatient(p)}>{p.name}</button>
                  </td>
                  <td>{p.email}</td>
                  <td>{p.phone||"—"}</td>
                  <td><span className="sd-badge">{p.language}</span></td>
                  <td>
                    <div className="sd-action-btns">
                      <button className="sd-btn sd-btn-sm" onClick={()=>openEdit(p)}>Edit</button>
                      <button className="sd-btn sd-btn-danger-sm" onClick={()=>deletePatient(p)}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {filtered.length===0 && <div className="sd-empty">{search?"No patients match your search.":"No patients yet."}</div>}
        </div>
      )}
    </div>
  );
}

// ── Doctors (Update 6) ────────────────────────────────────────────────────────
function DoctorsView() {
  const [doctors,  setDoctors]  = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editDoc,  setEditDoc]  = useState(null);
  const [form,     setForm]     = useState({name:"",specialty:"",qualifications:"",bio:"",rating:"",yearsExperience:"",surgeries:"",awards:"",tags:""});
  const [saving,   setSaving]   = useState(false);
  const [msg,      setMsg]      = useState(null);
  const [search,   setSearch]   = useState("");

  const load = useCallback(()=>{setLoading(true);api.doctors().then(setDoctors).finally(()=>setLoading(false));}, []);
  useEffect(()=>{load();}, [load]);

  const filtered = useMemo(()=>{
    const q=search.trim().toLowerCase(); if (!q) return doctors;
    return doctors.filter(d=>d.name.toLowerCase().includes(q)||(d.specialty||"").toLowerCase().includes(q));
  }, [doctors,search]);

  const openCreate = ()=>{setEditDoc(null);setForm({name:"",specialty:"",qualifications:"",bio:"",rating:"",yearsExperience:"",surgeries:"",awards:"",tags:""});setShowForm(true);};
  const openEdit = (doc)=>{setEditDoc(doc);setForm({name:doc.name||"",specialty:doc.specialty||"",qualifications:doc.qualifications||"",bio:doc.bio||"",rating:doc.rating||"",yearsExperience:doc.yearsExperience||"",surgeries:doc.surgeries||"",awards:doc.awards||"",tags:(doc.tags||[]).join(", ")});setShowForm(true);};
  const submit = async (e)=>{
    e.preventDefault();setSaving(true);setMsg(null);
    const payload={...form,rating:form.rating?parseFloat(form.rating):undefined,yearsExperience:form.yearsExperience?parseInt(form.yearsExperience):undefined,surgeries:form.surgeries?parseInt(form.surgeries):undefined,awards:form.awards?parseInt(form.awards):undefined,tags:form.tags?form.tags.split(",").map(t=>t.trim()).filter(Boolean):[]};
    try{if(editDoc){await api.staffUpdateDoctor(editDoc.id,payload);setMsg({type:"ok",text:"Doctor updated."});}else{await api.staffCreateDoctor(payload);setMsg({type:"ok",text:"Doctor created."});}setShowForm(false);setEditDoc(null);load();}
    catch(err){setMsg({type:"err",text:err.message});}finally{setSaving(false);}
  };
  const deleteDoc=async(id)=>{if(!window.confirm("Delete this doctor?"))return;try{await api.staffDeleteDoctor(id);load();}catch(err){setMsg({type:"err",text:err.message});}};

  return (
    <div className="sd-section">
      <div className="sd-section-header"><h2 className="sd-section-title">Doctors</h2><button className="sd-btn sd-btn-primary" onClick={openCreate}>+ Add Doctor</button></div>
      <SearchBar value={search} onChange={setSearch} placeholder="Search by name or specialty…" />
      {msg&&<div className={`sd-msg ${msg.type==="ok"?"sd-msg-ok":"sd-msg-err"}`}>{msg.text}</div>}
      {showForm&&(
        <form className="sd-form" onSubmit={submit}>
          <h3 className="sd-form-title">{editDoc?`Edit ${editDoc.name}`:"Add New Doctor"}</h3>
          <div className="sd-form-grid">
            {[{label:"Full Name",key:"name",required:true},{label:"Specialty",key:"specialty"},{label:"Qualifications",key:"qualifications"},{label:"Rating (0-5)",key:"rating",type:"number"},{label:"Years Experience",key:"yearsExperience",type:"number"},{label:"Surgeries",key:"surgeries",type:"number"},{label:"Awards",key:"awards",type:"number"}].map(f=>(
              <div className="sd-field" key={f.key}><label className="sd-label">{f.label}</label><input className="sd-input" type={f.type||"text"} required={f.required} value={form[f.key]} onChange={e=>setForm(v=>({...v,[f.key]:e.target.value}))}/></div>
            ))}
            <div className="sd-field sd-field-full"><label className="sd-label">Tags (comma-separated)</label><input className="sd-input" value={form.tags} onChange={e=>setForm(v=>({...v,tags:e.target.value}))}/></div>
            <div className="sd-field sd-field-full"><label className="sd-label">Bio</label><textarea className="sd-input sd-textarea" rows={3} value={form.bio} onChange={e=>setForm(v=>({...v,bio:e.target.value}))}/></div>
          </div>
          <div className="sd-form-actions">
            <button className="sd-btn sd-btn-primary" type="submit" disabled={saving}>{saving?"Saving…":editDoc?"Update":"Create"}</button>
            <button className="sd-btn" type="button" onClick={()=>{setShowForm(false);setEditDoc(null);}}>Cancel</button>
          </div>
        </form>
      )}
      {loading?<div className="sd-loading">Loading…</div>:(
        <div className="sd-doctor-grid">
          {filtered.map(doc=>(
            <div className="sd-doctor-card" key={doc.id}>
              <div className="sd-doctor-avatar">{doc.name.split(" ").filter(w=>w!=="Dr.").map(w=>w[0]).slice(0,2).join("")}</div>
              <div className="sd-doctor-info"><div className="sd-doctor-name">{doc.name}</div><div className="sd-doctor-spec">{doc.specialty}</div><div className="sd-doctor-meta"><span>★ {doc.rating}</span><span>{doc.yearsExperience}yr</span>{doc.tags?.slice(0,2).map(t=><span className="sd-badge" key={t}>{t}</span>)}</div></div>
              <div className="sd-doctor-actions"><button className="sd-btn sd-btn-sm" onClick={()=>openEdit(doc)}>Edit</button><button className="sd-btn sd-btn-danger-sm" onClick={()=>deleteDoc(doc.id)}>Delete</button></div>
            </div>
          ))}
          {filtered.length===0&&<div className="sd-empty">{search?"No doctors match.":"No doctors yet."}</div>}
        </div>
      )}
    </div>
  );
}

// ── Records ───────────────────────────────────────────────────────────────────
function RecordsView() {
  const [patients,setPatients]=useState([]);
  const [selectedPatient,setSelectedPatient]=useState(null);
  const [records,setRecords]=useState([]);
  const [loading,setLoading]=useState(false);
  const [form,setForm]=useState({type:"LAB_RESULT",title:""});
  const [file,setFile]=useState(null);
  const [saving,setSaving]=useState(false);
  const [msg,setMsg]=useState(null);
  const [uploadPct,setUploadPct]=useState(null);
  useEffect(()=>{api.staffPatients().then(setPatients);}, []);
  const selectPatient=async(p)=>{setSelectedPatient(p);setLoading(true);try{setRecords(await api.staffGetRecords(p.id));}catch{setRecords([]);}setLoading(false);};
  const submit=async(e)=>{
    e.preventDefault();if(!selectedPatient||!file){setMsg({type:"err",text:"Select a file."});return;}
    setSaving(true);setMsg(null);setUploadPct(20);
    const fd=new FormData();fd.append("type",form.type);fd.append("title",form.title);fd.append("file",file);
    try{setUploadPct(50);await api.staffUploadRecordFile(selectedPatient.id,fd);setUploadPct(100);setMsg({type:"ok",text:"Uploaded to Cloudinary."});setForm({type:"LAB_RESULT",title:""});setFile(null);document.getElementById("rec-file").value="";setRecords(await api.staffGetRecords(selectedPatient.id));}
    catch(err){setMsg({type:"err",text:err.message});}finally{setSaving(false);setTimeout(()=>setUploadPct(null),1500);}
  };
  return (
    <div className="sd-section">
      <h2 className="sd-section-title">Medical Records</h2>
      <div className="sd-records-layout">
        <div className="sd-patient-list"><div className="sd-list-title">Select Patient</div>{patients.map(p=>(<button key={p.id} className={`sd-patient-row ${selectedPatient?.id===p.id?"sd-patient-row--active":""}`} onClick={()=>selectPatient(p)}><div className="sd-patient-name">{p.name}</div><div className="sd-patient-email">{p.email}</div></button>))}</div>
        <div className="sd-records-panel">
          {!selectedPatient?<div className="sd-empty">Select a patient.</div>:(
            <>{msg&&<div className={`sd-msg ${msg.type==="ok"?"sd-msg-ok":"sd-msg-err"}`}>{msg.text}</div>}
            <form className="sd-form sd-form-compact" onSubmit={submit}>
              <h4 className="sd-form-title">📤 Upload Record</h4>
              <div className="sd-form-grid">
                <div className="sd-field"><label className="sd-label">Type</label><select className="sd-input" value={form.type} onChange={e=>setForm(v=>({...v,type:e.target.value}))}><option value="LAB_RESULT">Lab Result</option><option value="PRESCRIPTION">Prescription</option><option value="IMAGING">Imaging</option><option value="OTHER">Other</option></select></div>
                <div className="sd-field"><label className="sd-label">Title</label><input className="sd-input" required value={form.title} onChange={e=>setForm(v=>({...v,title:e.target.value}))}/></div>
                <div className="sd-field sd-field-full"><label className="sd-label">File (PDF/Image/Text)</label><input id="rec-file" type="file" accept=".pdf,.png,.jpg,.jpeg,.txt" className="sd-file-input" onChange={e=>setFile(e.target.files[0]||null)} required/>{file&&<div className="sd-file-preview">📎 {file.name}</div>}{uploadPct!==null&&<div className="sd-upload-bar"><div className="sd-upload-fill" style={{width:`${uploadPct}%`}}/></div>}</div>
              </div>
              <button className="sd-btn sd-btn-primary" type="submit" disabled={saving||!file}>{saving?"Uploading…":"Upload"}</button>
            </form>
            {loading?<div className="sd-loading">Loading…</div>:(
              <div className="sd-table-wrap" style={{marginTop:16}}>
                <table className="sd-table"><thead><tr><th>Type</th><th>Title</th><th>File</th><th>Date</th></tr></thead>
                <tbody>{records.map(r=>(<tr key={r.id}><td><span className="sd-badge">{r.type}</span></td><td>{r.title}</td><td>{r.fileUrl?<a href={r.fileUrl} target="_blank" rel="noopener noreferrer" className="sd-file-link">View ↗</a>:"—"}</td><td>{r.uploadedAt}</td></tr>))}</tbody></table>
                {records.length===0&&<div className="sd-empty">No records.</div>}
              </div>
            )}</>
          )}
        </div>
      </div>
    </div>
  );
}

// ── Vitals (Update 4: prefill existing vitals) ────────────────────────────────
function VitalsView() {
  const [patients,setPatients]=useState([]);
  const [selectedPatient,setSelectedPatient]=useState(null);
  const [vitalsHistory,setVitalsHistory]=useState([]);
  const [loading,setLoading]=useState(false);
  const EMPTY_FORM={heartRate:"",bloodPressure:"",oxygenSaturation:"",sleepHours:"",sleepMinutes:"",moveKcal:"",moveGoal:"",exerciseMinutes:"",exerciseGoal:"",standHours:"",standGoal:""};
  const [form,setForm]=useState(EMPTY_FORM);
  const [saving,setSaving]=useState(false);
  const [msg,setMsg]=useState(null);
  const [prefilled,setPrefilled]=useState(false);
  useEffect(()=>{api.staffPatients().then(setPatients);}, []);

  const selectPatient=async(p)=>{
    setSelectedPatient(p);setLoading(true);setPrefilled(false);
    try{
      const v=await api.staffGetVitals(p.id);
      setVitalsHistory(v);
      // Update 4: Prefill form with latest vitals
      if (v && v.length > 0) {
        const latest = v[0];
        setForm({
          heartRate: latest.heartRate||"",
          bloodPressure: latest.bloodPressure||"",
          oxygenSaturation: latest.oxygenSaturation||"",
          sleepHours: latest.sleepHours||"",
          sleepMinutes: latest.sleepMinutes||"",
          moveKcal: latest.moveKcal||"",
          moveGoal: latest.moveGoal||"",
          exerciseMinutes: latest.exerciseMinutes||"",
          exerciseGoal: latest.exerciseGoal||"",
          standHours: latest.standHours||"",
          standGoal: latest.standGoal||"",
        });
        setPrefilled(true);
      } else {
        setForm(EMPTY_FORM);
      }
    }catch{setVitalsHistory([]);setForm(EMPTY_FORM);}
    setLoading(false);
  };

  const submit=async(e)=>{
    e.preventDefault();if(!selectedPatient)return;setSaving(true);setMsg(null);
    const payload=Object.fromEntries(Object.entries(form).map(([k,v])=>[k,v===""?undefined:(isNaN(v)?v:Number(v))]));
    try{await api.staffUploadVitals(selectedPatient.id,payload);setMsg({type:"ok",text:"Vitals saved."});const v=await api.staffGetVitals(selectedPatient.id);setVitalsHistory(v);setPrefilled(true);}
    catch(err){setMsg({type:"err",text:err.message});}finally{setSaving(false);}
  };

  return (
    <div className="sd-section">
      <h2 className="sd-section-title">Patient Vitals</h2>
      <div className="sd-records-layout">
        <div className="sd-patient-list"><div className="sd-list-title">Select Patient</div>{patients.map(p=>(<button key={p.id} className={`sd-patient-row ${selectedPatient?.id===p.id?"sd-patient-row--active":""}`} onClick={()=>selectPatient(p)}><div className="sd-patient-name">{p.name}</div><div className="sd-patient-email">{p.email}</div></button>))}</div>
        <div className="sd-records-panel">
          {!selectedPatient?<div className="sd-empty">Select a patient.</div>:(
            <>{msg&&<div className={`sd-msg ${msg.type==="ok"?"sd-msg-ok":"sd-msg-err"}`}>{msg.text}</div>}
            {prefilled && <div className="sd-prefill-notice">✏️ Fields prefilled with latest vitals — edit and save to update.</div>}
            <form className="sd-form sd-form-compact" onSubmit={submit}>
              <h4 className="sd-form-title">{prefilled?"Update Vitals":"Upload New Vitals"}</h4>
              <div className="sd-form-grid">
                {[{label:"Heart Rate (bpm)",key:"heartRate"},{label:"Blood Pressure",key:"bloodPressure",type:"text"},{label:"Oxygen Saturation %",key:"oxygenSaturation"},{label:"Sleep Hours",key:"sleepHours"},{label:"Sleep Minutes",key:"sleepMinutes"},{label:"Move (kcal)",key:"moveKcal"},{label:"Move Goal",key:"moveGoal"},{label:"Exercise (min)",key:"exerciseMinutes"},{label:"Exercise Goal",key:"exerciseGoal"},{label:"Stand (hr)",key:"standHours"},{label:"Stand Goal",key:"standGoal"}].map(f=>(
                  <div className="sd-field" key={f.key}><label className="sd-label">{f.label}</label><input className="sd-input" type={f.type||"number"} value={form[f.key]} onChange={e=>setForm(v=>({...v,[f.key]:e.target.value}))}/></div>
                ))}
              </div>
              <button className="sd-btn sd-btn-primary" type="submit" disabled={saving}>{saving?"Saving…":prefilled?"Update Vitals":"Upload Vitals"}</button>
            </form>
            {loading?<div className="sd-loading">Loading…</div>:(
              <div className="sd-table-wrap" style={{marginTop:16}}>
                <table className="sd-table"><thead><tr><th>Recorded</th><th>HR</th><th>BP</th><th>SpO₂</th><th>Sleep</th></tr></thead>
                <tbody>{vitalsHistory.map(v=>(<tr key={v.id}><td>{v.recordedAt}</td><td>{v.heartRate}bpm</td><td>{v.bloodPressure}</td><td>{v.oxygenSaturation}%</td><td>{v.sleepHours}h {v.sleepMinutes}m</td></tr>))}</tbody></table>
                {vitalsHistory.length===0&&<div className="sd-empty">No vitals yet.</div>}
              </div>
            )}</>
          )}
        </div>
      </div>
    </div>
  );
}

// ── Appointments (Update 6) ───────────────────────────────────────────────────
function AppointmentsView() {
  const [appts,setAppts]=useState([]);
  const [loading,setLoading]=useState(true);
  const [filter,setFilter]=useState("ALL");
  const [msg,setMsg]=useState(null);
  const [search,setSearch]=useState("");
  const load=useCallback(()=>{setLoading(true);api.staffAppointments().then(setAppts).finally(()=>setLoading(false));}, []);
  useEffect(()=>{load();}, [load]);
  const action=async(id,type)=>{setMsg(null);try{if(type==="approve")await api.staffApproveAppt(id);else if(type==="reject")await api.staffRejectAppt(id);else await api.staffCompleteAppt(id);setMsg({type:"ok",text:"Updated."});load();}catch(err){setMsg({type:"err",text:err.message});}};
  const filtered=useMemo(()=>{
    let list=filter==="ALL"?appts:appts.filter(a=>a.status===filter);
    const q=search.trim().toLowerCase();
    if(q) list=list.filter(a=>a.patientName.toLowerCase().includes(q)||a.doctorName.toLowerCase().includes(q));
    return list;
  }, [appts,filter,search]);
  const STATUS_COLORS={SCHEDULED:"#60a5fa",COMPLETED:"#34d399",CANCELLED:"#fb7185",NO_SHOW:"#94a3b8"};
  return (
    <div className="sd-section">
      <div className="sd-section-header"><h2 className="sd-section-title">Appointments</h2><div className="sd-filter-row">{["ALL","SCHEDULED","COMPLETED","CANCELLED"].map(s=><button key={s} className={`sd-filter-btn ${filter===s?"sd-filter-btn--active":""}`} onClick={()=>setFilter(s)}>{s}</button>)}</div></div>
      <SearchBar value={search} onChange={setSearch} placeholder="Search by patient or doctor…" />
      {msg&&<div className={`sd-msg ${msg.type==="ok"?"sd-msg-ok":"sd-msg-err"}`}>{msg.text}</div>}
      {loading?<div className="sd-loading">Loading…</div>:(
        <div className="sd-table-wrap"><table className="sd-table"><thead><tr><th>Patient</th><th>Doctor</th><th>Date</th><th>Time</th><th>Status</th><th>Actions</th></tr></thead>
        <tbody>{filtered.map(a=>(<tr key={a.id}><td>{a.patientName}</td><td>{a.doctorName}</td><td>{a.appointmentDate}</td><td>{a.timeSlot}</td><td><span className="sd-status" style={{color:STATUS_COLORS[a.status]||"#fff"}}>{a.status}</span></td><td><div className="sd-action-btns">{a.status==="SCHEDULED"&&(<><button className="sd-btn sd-btn-success-sm" onClick={()=>action(a.id,"complete")}>Complete</button><button className="sd-btn sd-btn-danger-sm" onClick={()=>action(a.id,"reject")}>Cancel</button></>)}</div></td></tr>))}</tbody></table>
        {filtered.length===0&&<div className="sd-empty">No appointments found.</div>}</div>
      )}
    </div>
  );
}

// ── Medicine Orders (Update 6) ────────────────────────────────────────────────
function OrdersView() {
  const [orders,setOrders]=useState([]);
  const [loading,setLoading]=useState(true);
  const [filter,setFilter]=useState("ALL");
  const [msg,setMsg]=useState(null);
  const [search,setSearch]=useState("");
  const load=useCallback(()=>{setLoading(true);api.staffOrders().then(setOrders).finally(()=>setLoading(false));}, []);
  useEffect(()=>{load();}, [load]);
  const action=async(id,type)=>{setMsg(null);try{if(type==="dispatch")await api.staffDispatchOrder(id);else await api.staffDeliverOrder(id);setMsg({type:"ok",text:"Order updated."});load();}catch(err){setMsg({type:"err",text:err.message});}};
  const filtered=useMemo(()=>{
    let list=filter==="ALL"?orders:orders.filter(o=>o.status===filter);
    const q=search.trim().toLowerCase();
    if(q) list=list.filter(o=>o.patientName.toLowerCase().includes(q)||String(o.id).includes(q)||o.prescriptionTitle.toLowerCase().includes(q));
    return list;
  }, [orders,filter,search]);
  const STATUS_COLORS={PENDING:"#fb7185",DISPATCHED:"#60a5fa",DELIVERED:"#34d399",CANCELLED:"#94a3b8"};
  const pendingCount=orders.filter(o=>o.status==="PENDING").length;
  return (
    <div className="sd-section">
      <div className="sd-section-header"><h2 className="sd-section-title">💊 Medicine Orders {pendingCount>0&&<span className="sd-active-badge">{pendingCount} PENDING</span>}</h2><div className="sd-filter-row">{["ALL","PENDING","DISPATCHED","DELIVERED"].map(s=><button key={s} className={`sd-filter-btn ${filter===s?"sd-filter-btn--active":""}`} onClick={()=>setFilter(s)}>{s}</button>)}</div></div>
      <SearchBar value={search} onChange={setSearch} placeholder="Search by patient or order ID…" />
      {msg&&<div className={`sd-msg ${msg.type==="ok"?"sd-msg-ok":"sd-msg-err"}`}>{msg.text}</div>}
      {loading?<div className="sd-loading">Loading orders…</div>:(
        <div className="sd-order-list">{filtered.map(o=>(
          <div key={o.id} className={`sd-order-card ${o.status==="PENDING"?"sd-order-card--pending":""}`}>
            <div className="sd-order-info">
              <div className="sd-order-patient">{o.patientName}</div>
              <div className="sd-order-rx">📋 {o.prescriptionTitle}{o.prescriptionFileUrl&&<a href={o.prescriptionFileUrl} target="_blank" rel="noopener noreferrer" className="sd-file-link" style={{marginLeft:10}}>View Rx ↗</a>}</div>
              <div className="sd-order-meta">Ordered: {o.createdAt}</div>
              {o.dispatchedAt&&<div className="sd-order-meta">Dispatched: {o.dispatchedAt}</div>}
              {o.deliveredAt&&<div className="sd-order-meta">Delivered: {o.deliveredAt}</div>}
            </div>
            <div className="sd-order-right">
              <span className="sd-status" style={{color:STATUS_COLORS[o.status]||"#fff",fontWeight:700}}>{o.status}</span>
              <div className="sd-action-btns" style={{marginTop:8}}>
                {o.status==="PENDING"&&<button className="sd-btn sd-btn-success-sm" onClick={()=>action(o.id,"dispatch")}>🚚 Dispatch</button>}
                {o.status==="DISPATCHED"&&<button className="sd-btn sd-btn-primary sd-btn-sm" onClick={()=>action(o.id,"deliver")}>✅ Delivered</button>}
              </div>
            </div>
          </div>
        ))}{filtered.length===0&&<div className="sd-empty">No orders found.</div>}</div>
      )}
    </div>
  );
}

// ── Emergency (Update 6) ──────────────────────────────────────────────────────
function EmergencyView() {
  const [alerts,setAlerts]=useState([]);
  const [loading,setLoading]=useState(true);
  const [filter,setFilter]=useState("ACTIVE");
  const [search,setSearch]=useState("");
  const load=useCallback(()=>{setLoading(true);api.staffAllAlerts().then(setAlerts).finally(()=>setLoading(false));}, []);
  useEffect(()=>{load();const t=setInterval(load,15000);return()=>clearInterval(t);}, [load]);
  const resolve=async(id)=>{try{await api.staffResolveAlert(id);load();}catch{}};
  const filtered=useMemo(()=>{
    let list=filter==="ALL"?alerts:alerts.filter(a=>a.status===filter);
    const q=search.trim().toLowerCase();
    if(q) list=list.filter(a=>a.patientName.toLowerCase().includes(q)||(a.locationDescription||"").toLowerCase().includes(q));
    return list;
  }, [alerts,filter,search]);
  return (
    <div className="sd-section">
      <div className="sd-section-header"><h2 className="sd-section-title">🚨 Emergency Alerts {alerts.filter(a=>a.status==="ACTIVE").length>0&&<span className="sd-active-badge">{alerts.filter(a=>a.status==="ACTIVE").length} ACTIVE</span>}</h2><div className="sd-filter-row">{["ACTIVE","ALL","RESOLVED"].map(s=><button key={s} className={`sd-filter-btn ${filter===s?"sd-filter-btn--active":""}`} onClick={()=>setFilter(s)}>{s}</button>)}</div></div>
      <SearchBar value={search} onChange={setSearch} placeholder="Search by patient or location…" />
      {loading?<div className="sd-loading">Loading…</div>:(
        <div className="sd-alert-list">{filtered.map(a=>(
          <div key={a.id} className={`sd-emergency-card ${a.status==="ACTIVE"?"sd-emergency-card--active":""}`}>
            <div className="sd-emergency-icon">{a.status==="ACTIVE"?"🚨":"✅"}</div>
            <div className="sd-emergency-info"><div className="sd-emergency-patient">{a.patientName}</div><div className="sd-emergency-loc">📍 {a.locationDescription||(a.latitude?`${a.latitude}, ${a.longitude}`:"Location unknown")}</div><div className="sd-emergency-time">Triggered: {a.triggeredAt}</div>{a.resolvedAt&&<div className="sd-emergency-time">Resolved: {a.resolvedAt}</div>}</div>
            <div className="sd-emergency-right"><span className={`sd-status-badge ${a.status==="ACTIVE"?"sd-status-badge--danger":"sd-status-badge--ok"}`}>{a.status}</span>{a.status==="ACTIVE"&&<button className="sd-btn sd-btn-primary" onClick={()=>resolve(a.id)}>Mark Resolved</button>}</div>
          </div>
        ))}{filtered.length===0&&<div className="sd-empty">No alerts found.</div>}</div>
      )}
    </div>
  );
}

// ── Shell ─────────────────────────────────────────────────────────────────────
const NAV_ITEMS=[
  {key:"overview",label:"Overview",icon:"◈"},
  {key:"patients",label:"Patients",icon:"👥"},
  {key:"doctors",label:"Doctors",icon:"🩺"},
  {key:"records",label:"Records",icon:"📋"},
  {key:"vitals",label:"Vitals",icon:"❤️"},
  {key:"appointments",label:"Appointments",icon:"📅"},
  {key:"orders",label:"Orders",icon:"💊"},
  {key:"emergency",label:"Emergency",icon:"🚨"},
];

export default function StaffDashboard() {
  const {user,logout}=useAuth();
  const [activeView,setActiveView]=useState("overview");
  const [sidebarOpen,setSidebarOpen]=useState(true);

  const renderView=()=>{
    switch(activeView){
      case "overview":     return <Overview onNavigate={setActiveView}/>;
      case "patients":     return <PatientsView/>;
      case "doctors":      return <DoctorsView/>;
      case "records":      return <RecordsView/>;
      case "vitals":       return <VitalsView/>;
      case "appointments": return <AppointmentsView/>;
      case "orders":       return <OrdersView/>;
      case "emergency":    return <EmergencyView/>;
      default:             return <Overview onNavigate={setActiveView}/>;
    }
  };

  return (
    <div className={`staff-shell ${sidebarOpen?"":"staff-shell--collapsed"}`}>
      <aside className="staff-sidebar">
        <div className="staff-logo">
          <img src="/sevili-logo1.png" alt="Sevili" className="staff-logo-img" />
          {sidebarOpen&&<span className="staff-logo-text">Sevili Staff</span>}
        </div>
        <nav className="staff-nav">
          {NAV_ITEMS.map(item=>(
            <button key={item.key} className={`staff-nav-item ${activeView===item.key?"staff-nav-item--active":""}`} onClick={()=>setActiveView(item.key)} title={!sidebarOpen?item.label:""}>
              <span className="staff-nav-icon">{item.icon}</span>
              {sidebarOpen&&<span className="staff-nav-label">{item.label}</span>}
            </button>
          ))}
        </nav>
        <div className="staff-sidebar-footer">
          {sidebarOpen&&<div className="staff-user-info"><div className="staff-user-name">{user?.name}</div><div className="staff-user-role">Hospital Staff</div></div>}
          <button className="staff-logout-btn" onClick={logout} title="Sign out">↩</button>
        </div>
      </aside>
      <button className="staff-toggle" onClick={()=>setSidebarOpen(v=>!v)}>{sidebarOpen?"◀":"▶"}</button>
      <main className="staff-main"><div className="staff-content">{renderView()}</div></main>
    </div>
  );
}
