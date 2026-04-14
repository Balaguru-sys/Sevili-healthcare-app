-- ============================================================
-- MedApp Production Schema — MySQL
-- ============================================================

CREATE DATABASE IF NOT EXISTS medapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE medapp;

-- ── patients ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS patients (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(120) NOT NULL,
    email      VARCHAR(180) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    phone      VARCHAR(30),
    language   VARCHAR(10) DEFAULT 'EN',
    role       VARCHAR(20) NOT NULL DEFAULT 'ROLE_PATIENT',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_patients_email (email)
);

-- ── staff_users ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS staff_users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(120) NOT NULL,
    email      VARCHAR(180) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20) NOT NULL DEFAULT 'ROLE_STAFF',
    department VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_staff_email (email)
);

-- ── doctors ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS doctors (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(120) NOT NULL,
    specialty        VARCHAR(120),
    qualifications   VARCHAR(200),
    bio              TEXT,
    rating           DECIMAL(3,1) DEFAULT 0.0,
    review_count     INT          DEFAULT 0,
    years_experience INT          DEFAULT 0,
    surgeries        INT          DEFAULT 0,
    awards           INT          DEFAULT 0,
    avatar_url       VARCHAR(500)
);

-- ── doctor_tags ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS doctor_tags (
    doctor_id BIGINT      NOT NULL,
    tag       VARCHAR(80) NOT NULL,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
);

-- ── doctor_availability ─────────────────────────────────────
CREATE TABLE IF NOT EXISTS doctor_availability (
    id        BIGINT      AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT      NOT NULL,
    slot_date DATE        NOT NULL,
    time_slot VARCHAR(50) NOT NULL,
    available TINYINT(1)  NOT NULL DEFAULT 1,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    INDEX idx_avail_doctor_date (doctor_id, slot_date)
);

-- ── appointments ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS appointments (
    id               BIGINT      AUTO_INCREMENT PRIMARY KEY,
    patient_id       BIGINT      NOT NULL,
    doctor_id        BIGINT      NOT NULL,
    appointment_date VARCHAR(20) NOT NULL,
    time_slot        VARCHAR(60) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (doctor_id)  REFERENCES doctors(id),
    INDEX idx_appt_patient (patient_id),
    INDEX idx_appt_doctor  (doctor_id)
);

-- ── vitals ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS vitals (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    patient_id        BIGINT       NOT NULL,
    heart_rate        INT,
    blood_pressure    VARCHAR(20),
    oxygen_saturation INT,
    sleep_hours       DOUBLE,
    sleep_minutes     INT,
    move_kcal         INT,
    move_goal         INT,
    exercise_minutes  INT,
    exercise_goal     INT,
    stand_hours       INT,
    stand_goal        INT,
    recorded_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    INDEX idx_vitals_patient (patient_id),
    INDEX idx_vitals_recorded (recorded_at)
);

-- ── medical_records ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS medical_records (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    patient_id  BIGINT       NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    title       VARCHAR(200) NOT NULL,
    file_url    VARCHAR(500),
    uploaded_by BIGINT,
    uploaded_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id)  REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES staff_users(id),
    INDEX idx_records_patient (patient_id)
);

-- ── chat_messages ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS chat_messages (
    id         BIGINT    AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT    NOT NULL,
    role       VARCHAR(20) NOT NULL,
    content    TEXT      NOT NULL,
    language   VARCHAR(10) DEFAULT 'EN',
    timestamp  DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    INDEX idx_chat_patient (patient_id)
);

-- ── emergency_alerts ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS emergency_alerts (
    id                   BIGINT       AUTO_INCREMENT PRIMARY KEY,
    patient_id           BIGINT       NOT NULL,
    latitude             VARCHAR(30),
    longitude            VARCHAR(30),
    location_description VARCHAR(300),
    status               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    triggered_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at          DATETIME,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    INDEX idx_emergency_status (status),
    INDEX idx_emergency_patient (patient_id)
);
