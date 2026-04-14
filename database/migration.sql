-- ============================================================
-- MedApp Feature Migration
-- Run after existing schema.sql
-- ============================================================

USE medapp;

-- ── medicine_orders ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS medicine_orders (
    id                     BIGINT      AUTO_INCREMENT PRIMARY KEY,
    patient_id             BIGINT      NOT NULL,
    prescription_record_id BIGINT      NOT NULL,
    status                 VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes                  TEXT,
    created_at             DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dispatched_at          DATETIME,
    delivered_at           DATETIME,
    FOREIGN KEY (patient_id)             REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (prescription_record_id) REFERENCES medical_records(id),
    INDEX idx_orders_patient (patient_id),
    INDEX idx_orders_status  (status)
);

-- ── Add file_type column to medical_records (for viewer routing) ──
ALTER TABLE medical_records
    ADD COLUMN IF NOT EXISTS file_type VARCHAR(20) DEFAULT 'OTHER';
-- Values: PDF, IMAGE, TEXT, OTHER
