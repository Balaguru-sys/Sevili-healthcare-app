# Azure Pulse — Medical Concierge App

A multilingual, dark glassmorphic medical healthcare chatbot app built with React + Spring Boot.

---

## Project Structure

```
medapp/
├── preview.html          ← Standalone preview (open in browser, no build needed)
├── frontend/             ← React + Vite frontend
│   ├── src/
│   │   ├── App.jsx / App.css
│   │   ├── main.jsx
│   │   ├── pages/
│   │   │   ├── Dashboard.jsx / .css   (Vitals, Activity, Appointments, AI Analysis)
│   │   │   ├── Chat.jsx / .css        (AI Concierge Chat with Anthropic API)
│   │   │   ├── Records.jsx / .css     (Encrypted Medical Records)
│   │   │   ├── DoctorProfile.jsx/.css (Doctor bio, booking, reviews)
│   │   │   └── Emergency.jsx / .css   (SOS, location, contacts)
│   │   └── components/
│   │       ├── BottomNav.jsx / .css
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
└── backend/              ← Spring Boot backend
    ├── pom.xml
    └── src/main/java/com/medapp/
        ├── MedAppApplication.java
        ├── config/
        │   ├── SecurityConfig.java    (CORS, JWT-ready)
        │   └── DataSeeder.java        (Demo data)
        ├── model/                     (Patient, Doctor, Vitals, Appointment, ChatMessage, EmergencyAlert)
        ├── repository/                (JPA repositories)
        ├── service/
        │   ├── ChatService.java       (Anthropic API integration)
        │   ├── VitalsService.java
        │   ├── AppointmentService.java
        │   └── EmergencyService.java
        ├── controller/
        │   ├── ChatController.java        POST /api/chat/message
        │   ├── VitalsController.java      GET/POST /api/vitals/{patientId}
        │   ├── AppointmentController.java POST /api/appointments
        │   ├── DoctorController.java      GET /api/doctors
        │   └── EmergencyController.java   POST /api/emergency/sos
        └── dto/

```

---

## Quick Start

### 1. Open Preview (No setup needed)
```
Open medapp/preview.html in any browser
```
This is a fully working standalone HTML file — all 5 screens, live vitals, chat (with fallback AI), SOS toggle, doctor booking, records.

---

### 2. Run React Frontend

```bash
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

---

### 3. Run Spring Boot Backend

```bash
cd backend

# Option A — Maven wrapper
./mvnw spring-boot:run

# Option B — IDE
# Import as Maven project, run MedAppApplication.java
```

Backend starts on **http://localhost:8080**

H2 console: http://localhost:8080/h2-console (sa / no password)

---

### 4. Connect AI (Anthropic)

In `backend/src/main/resources/application.properties`:
```properties
anthropic.api.key=your-actual-anthropic-api-key
```

Or set environment variable:
```bash
export ANTHROPIC_API_KEY=sk-ant-...
./mvnw spring-boot:run
```

Without a key, the chat falls back to smart canned responses.

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/chat/message | Send message to AI concierge |
| GET | /api/vitals/{id}/latest | Get latest patient vitals |
| POST | /api/vitals/{id} | Save new vitals reading |
| GET | /api/vitals/{id}/analysis | AI analysis of vitals |
| GET | /api/doctors | List all doctors |
| GET | /api/doctors/{id} | Get doctor by ID |
| POST | /api/appointments | Book appointment |
| GET | /api/appointments/patient/{id} | Get patient appointments |
| PATCH | /api/appointments/{id}/cancel | Cancel appointment |
| POST | /api/emergency/sos | Trigger SOS alert |
| GET | /api/emergency/history/{id} | SOS history |

---

## Production Checklist

- [ ] Swap H2 for MySQL/PostgreSQL in `application.properties`
- [ ] Enable JWT auth in `SecurityConfig.java` (scaffolding already there)
- [ ] Set `ANTHROPIC_API_KEY` environment variable
- [ ] Add Twilio/FCM for real SOS notifications
- [ ] Build React: `npm run build` → serve `dist/` via Spring Boot static resources
- [ ] HTTPS / SSL certificate
- [ ] Set `app.cors.origins` to your production domain

---

## Design

- **Theme**: Deep navy-black (#080c14) glassmorphic dark
- **Fonts**: Syne (headings) + DM Sans (body) — Google Fonts
- **Accent palette**: Electric blue, cyan, teal, purple, rose
- **Screens**: Dashboard · AI Chat · Records · Doctor Profile · Emergency SOS
- **Languages**: EN · ES · FR (with backend support for TA, HI)
