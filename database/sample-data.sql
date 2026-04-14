USE medapp;

-- ── Staff users (password: Staff@123) ───────────────────────
INSERT INTO staff_users (name, email, password, role, department) VALUES
('Admin User',    'admin@medapp.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lha2', 'ROLE_STAFF', 'Administration'),
('Dr. Coordinator', 'coord@medapp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lha2', 'ROLE_STAFF', 'Coordination');

-- ── Patients (password: Patient@123) ────────────────────────
INSERT INTO patients (name, email, password, phone, language) VALUES
('Sarah Chen',    'sarah.chen@example.com',   '$2a$10$WkMOqjCGAMfMVqAsFvdQx.wvzAshMAtdKZlIMqxZE8E0slE/LFzYW', '+1 (555) 100-2000', 'EN'),
('James Rivera',  'james.rivera@example.com', '$2a$10$WkMOqjCGAMfMVqAsFvdQx.wvzAshMAtdKZlIMqxZE8E0slE/LFzYW', '+1 (555) 200-3000', 'ES'),
('Aiko Tanaka',   'aiko.tanaka@example.com',  '$2a$10$WkMOqjCGAMfMVqAsFvdQx.wvzAshMAtdKZlIMqxZE8E0slE/LFzYW', '+81-90-1234-5678', 'EN');

-- ── Doctors ─────────────────────────────────────────────────
INSERT INTO doctors (name, specialty, qualifications, bio, rating, review_count, years_experience, surgeries, awards) VALUES
('Dr. Julian Vanåe',   'Neurological Surgery', 'MD, PhD',    'World-renowned Neurological Surgeon specialising in minimally invasive spinal procedures and complex brain mapping. Integrates AI-driven diagnostics with clinical excellence at the Neuro-Integrative Unit.', 4.9, 120, 15, 2400, 42),
('Dr. Julian Sterling','Cardiology',           'MD, FACC',   'Senior Cardiologist with expertise in interventional cardiology and preventive heart care.', 4.8, 95, 12, 1800, 28),
('Dr. Elena Rossi',    'Clinical Nutrition',   'MD, RDN',    'Clinical Nutritionist and metabolic health specialist helping patients achieve optimal wellness through personalised dietary interventions.', 4.9, 180, 10, 0, 15),
('Dr. Priya Menon',    'General Medicine',     'MBBS, MD',   'Experienced general physician focused on preventive care and chronic disease management.', 4.7, 210, 8, 0, 10),
('Dr. Aaron Blake',    'Orthopedics',          'MD, MS Orth','Orthopedic surgeon specialising in sports injuries, joint replacements and minimally invasive techniques.', 4.8, 145, 11, 1200, 18);

INSERT INTO doctor_tags (doctor_id, tag) VALUES
(1,'Brain Mapping'),(1,'Spinal Trauma'),(1,'Minimally Invasive'),
(2,'Interventional Cardiology'),(2,'Echocardiography'),(2,'Preventive Care'),
(3,'Metabolic Health'),(3,'Sports Nutrition'),(3,'Weight Management'),
(4,'Preventive Care'),(4,'Chronic Disease'),(4,'Diabetes'),
(5,'Sports Injuries'),(5,'Joint Replacement'),(5,'Physiotherapy');

-- ── Doctor availability (next 5 working days) ───────────────
INSERT INTO doctor_availability (doctor_id, slot_date, time_slot) VALUES
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00 AM - 10:00 AM'),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '02:00 PM - 03:00 PM'),
(1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00 AM - 11:00 AM'),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:30 AM - 11:30 AM'),
(2, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '03:00 PM - 04:00 PM'),
(3, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00 AM - 10:00 AM'),
(3, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '01:00 PM - 02:00 PM'),
(4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00 AM - 09:00 AM'),
(4, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '04:00 PM - 05:00 PM'),
(5, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '11:00 AM - 12:00 PM');

-- ── Vitals for all 3 patients ────────────────────────────────
INSERT INTO vitals (patient_id, heart_rate, blood_pressure, oxygen_saturation, sleep_hours, sleep_minutes, move_kcal, move_goal, exercise_minutes, exercise_goal, stand_hours, stand_goal, recorded_at) VALUES
(1, 72, '118/76', 98, 7.0, 42, 450, 600, 45, 30, 10, 12, NOW()),
(1, 74, '120/78', 97, 6.5, 30, 380, 600, 30, 30, 8,  12, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 70, '116/74', 99, 8.0, 0,  500, 600, 60, 30, 11, 12, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 80, '130/85', 96, 6.0, 15, 320, 500, 20, 30, 7,  12, NOW()),
(2, 82, '132/86', 95, 5.5, 20, 280, 500, 15, 30, 6,  12, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 78, '128/82', 97, 7.0, 0,  400, 500, 35, 30, 9,  12, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 65, '110/70', 99, 8.5, 30, 600, 600, 55, 30, 12, 12, NOW()),
(3, 66, '112/72', 98, 8.0, 15, 550, 600, 50, 30, 11, 12, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 64, '108/68', 99, 9.0, 0,  620, 600, 60, 30, 12, 12, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 71, '117/75', 98, 7.2, 10, 460, 600, 48, 30, 10, 12, DATE_SUB(NOW(), INTERVAL 3 DAY));

-- ── Medical records ──────────────────────────────────────────
INSERT INTO medical_records (patient_id, type, title, file_url, uploaded_by, uploaded_at) VALUES
(1,'LAB_RESULT',  'Metabolic Panel Oct 2024',   '/files/records/sarah-metabolic-oct.pdf',  1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1,'PRESCRIPTION','Lisinopril 10mg Prescription','/files/records/sarah-lisinopril.pdf',     1, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(1,'IMAGING',     'Chest X-Ray Sep 2024',        '/files/records/sarah-chest-xray.pdf',     1, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(1,'OTHER',       'Annual Physical Summary',     '/files/records/sarah-physical-2024.pdf',  1, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(2,'LAB_RESULT',  'Lipid Panel Oct 2024',        '/files/records/james-lipid-oct.pdf',      1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2,'PRESCRIPTION','Metformin 500mg',             '/files/records/james-metformin.pdf',      1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2,'IMAGING',     'Echocardiogram Report',       '/files/records/james-echo.pdf',           1, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(3,'LAB_RESULT',  'CBC Full Blood Count',        '/files/records/aiko-cbc.pdf',             1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3,'PRESCRIPTION','Vitamin D Supplement Plan',   '/files/records/aiko-vitamind.pdf',        1, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(3,'OTHER',       'Nutrition Assessment Report', '/files/records/aiko-nutrition.pdf',       1, DATE_SUB(NOW(), INTERVAL 15 DAY));

-- ── Appointments ─────────────────────────────────────────────
INSERT INTO appointments (patient_id, doctor_id, appointment_date, time_slot, status) VALUES
(1, 2, DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d'), '10:30 AM - 11:30 AM', 'SCHEDULED'),
(1, 3, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '%Y-%m-%d'), '09:00 AM - 10:00 AM', 'COMPLETED'),
(2, 4, DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '%Y-%m-%d'), '08:00 AM - 09:00 AM', 'SCHEDULED'),
(3, 1, DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '%Y-%m-%d'), '10:00 AM - 11:00 AM', 'SCHEDULED');
