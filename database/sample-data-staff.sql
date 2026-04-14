-- Additional sample data for staff dashboard testing
-- Run AFTER the main schema.sql and sample-data.sql

USE medapp;

-- Staff user (password: Staff@123)
-- Note: if running fresh, admin@medapp.com is already seeded by DataSeeder in dev mode.
-- This is for MySQL production imports only.

-- Additional patients for richer testing
INSERT IGNORE INTO patients (name, email, password, phone, language, role) VALUES
('Ravi Kumar',    'ravi.kumar@example.com',   '$2a$10$WkMOqjCGAMfMVqAsFvdQx.wvzAshMAtdKZlIMqxZE8E0slE/LFzYW', '+91-99001-23456', 'EN', 'ROLE_PATIENT'),
('Mei Lin',       'mei.lin@example.com',       '$2a$10$WkMOqjCGAMfMVqAsFvdQx.wvzAshMAtdKZlIMqxZE8E0slE/LFzYW', '+86-138-0013-8000', 'EN', 'ROLE_PATIENT');

-- Emergency alert for testing
INSERT IGNORE INTO emergency_alerts (patient_id, latitude, longitude, location_description, status, triggered_at)
SELECT p.id, '13.0827', '80.2707', 'Chennai Medical Zone, Block 3', 'ACTIVE', NOW()
FROM patients p WHERE p.email = 'ravi.kumar@example.com' LIMIT 1;
