-- VertebralCare Database Initialization Script
-- Spine Case Management System Database Initialization

-- Create database
CREATE DATABASE IF NOT EXISTS vertebral_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE vertebral_db;

-- Drop existing tables (for re-initialization)
DROP TABLE IF EXISTS Exam;
DROP TABLE IF EXISTS DiagnosisClass;
DROP TABLE IF EXISTS Patient;

-- 1. Patient Table
-- BCNF: patient_id -> name, gender, birth_date, phone, created_at
CREATE TABLE Patient (
    patient_id    INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    gender        CHAR(1),
    birth_date    DATE,
    phone         VARCHAR(20),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_gender CHECK (gender IN ('M', 'F'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. DiagnosisClass Table
-- BCNF: class_id -> code, description; code is also a candidate key
CREATE TABLE DiagnosisClass (
    class_id      INT AUTO_INCREMENT PRIMARY KEY,
    code          VARCHAR(10) NOT NULL UNIQUE,
    description   VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert initial diagnosis classes (Binary Classification)
INSERT INTO DiagnosisClass (code, description) VALUES
('NO', 'Normal'),
('AB', 'Abnormal');

-- 3. Exam Table
-- BCNF: exam_id -> patient_id, exam_date, 6 indicators, class_id, notes, created_at
CREATE TABLE Exam (
    exam_id                   INT AUTO_INCREMENT PRIMARY KEY,
    patient_id                INT NOT NULL,
    exam_date                 DATE NOT NULL,
    pelvic_incidence          DOUBLE NOT NULL COMMENT 'Pelvic Incidence',
    pelvic_tilt               DOUBLE NOT NULL COMMENT 'Pelvic Tilt',
    lumbar_lordosis_angle     DOUBLE NOT NULL COMMENT 'Lumbar Lordosis Angle',
    sacral_slope              DOUBLE NOT NULL COMMENT 'Sacral Slope',
    pelvic_radius             DOUBLE NOT NULL COMMENT 'Pelvic Radius',
    degree_spondylolisthesis  DOUBLE NOT NULL COMMENT 'Degree of Spondylolisthesis',
    class_id                  INT NOT NULL,
    notes                     VARCHAR(500) COMMENT 'Notes',
    created_at                TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_exam_patient
        FOREIGN KEY (patient_id) REFERENCES Patient(patient_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_exam_class
        FOREIGN KEY (class_id) REFERENCES DiagnosisClass(class_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    INDEX idx_patient_id (patient_id),
    INDEX idx_exam_date (exam_date),
    INDEX idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Verify tables created
SHOW TABLES;

-- Show table structures
DESCRIBE Patient;
DESCRIBE DiagnosisClass;
DESCRIBE Exam;
