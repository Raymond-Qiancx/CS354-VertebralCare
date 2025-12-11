package com.vertebralcare.service;

import com.vertebralcare.dao.DiagnosisClassDao;
import com.vertebralcare.dao.ExamDao;
import com.vertebralcare.dao.PatientDao;
import com.vertebralcare.model.Exam;
import com.vertebralcare.model.Patient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * CSV Data Import Service
 */
public class ImportService {

    private final PatientDao patientDao;
    private final ExamDao examDao;
    private final DiagnosisClassDao diagnosisClassDao;
    private final Random random = new Random();

    // Names for generating virtual patients
    private static final String[] FIRST_NAMES = {
            "James", "John", "Robert", "Michael", "William", "David", "Richard", "Joseph", "Thomas", "Charles",
            "Mary", "Patricia", "Jennifer", "Linda", "Elizabeth", "Barbara", "Susan", "Jessica", "Sarah", "Karen",
            "Daniel", "Matthew", "Anthony", "Mark", "Donald", "Steven", "Paul", "Andrew", "Joshua", "Kenneth",
            "Nancy", "Betty", "Margaret", "Sandra", "Ashley", "Dorothy", "Kimberly", "Emily", "Donna", "Michelle"
    };

    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
            "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
            "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson"
    };

    public ImportService() {
        this.patientDao = new PatientDao();
        this.examDao = new ExamDao();
        this.diagnosisClassDao = new DiagnosisClassDao();
    }

    /**
     * Import CSV file
     * @param csvFile CSV file
     * @param progressCallback Progress callback (current, total)
     * @return Number of imported records
     */
    public int importCSV(File csvFile, ProgressCallback progressCallback) throws IOException, SQLException {
        List<String[]> records = parseCSV(csvFile);

        if (records.isEmpty()) {
            return 0;
        }

        int total = records.size();
        int imported = 0;

        for (int i = 0; i < records.size(); i++) {
            String[] row = records.get(i);

            try {
                // 1. Generate virtual patient
                Patient patient = generateVirtualPatient();
                int patientId = patientDao.insert(patient);

                // 2. Parse exam data
                Exam exam = parseExamFromRow(row, patientId);

                // 3. Insert exam record
                examDao.insert(exam);

                imported++;

                // 4. Progress callback
                if (progressCallback != null) {
                    progressCallback.onProgress(i + 1, total);
                }

            } catch (Exception e) {
                System.err.println("Failed to import row " + (i + 1) + ": " + e.getMessage());
            }
        }

        return imported;
    }

    /**
     * Parse CSV file
     */
    private List<String[]> parseCSV(File csvFile) throws IOException {
        List<String[]> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                // Skip header row
                if (isFirstLine) {
                    isFirstLine = false;
                    // Check if header row
                    if (line.toLowerCase().contains("pelvic") || line.toLowerCase().contains("class")) {
                        continue;
                    }
                }

                // Parse CSV row
                String[] values = line.split(",");
                if (values.length >= 7) {
                    records.add(values);
                }
            }
        }

        return records;
    }

    /**
     * Generate virtual patient
     */
    private Patient generateVirtualPatient() {
        Patient patient = new Patient();

        // Random name
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        patient.setName(firstName + " " + lastName);

        // Random gender
        patient.setGender(random.nextBoolean() ? "M" : "F");

        // Random birth date (20-80 years old)
        long minDay = Date.valueOf("1945-01-01").getTime();
        long maxDay = Date.valueOf("2005-01-01").getTime();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        patient.setBirthDate(new Date(randomDay));

        // Random phone
        patient.setPhone(String.format("(%03d) %03d-%04d",
                random.nextInt(900) + 100,
                random.nextInt(900) + 100,
                random.nextInt(10000)));

        return patient;
    }

    /**
     * Parse exam record from CSV row
     */
    private Exam parseExamFromRow(String[] row, int patientId) throws SQLException {
        Exam exam = new Exam();

        exam.setPatientId(patientId);

        // Random exam date (within last 5 years)
        long minDay = Date.valueOf("2020-01-01").getTime();
        long maxDay = Date.valueOf("2025-12-01").getTime();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        exam.setExamDate(new Date(randomDay));

        // 6 indicators
        exam.setPelvicIncidence(parseDouble(row[0]));
        exam.setPelvicTilt(parseDouble(row[1]));
        exam.setLumbarLordosisAngle(parseDouble(row[2]));
        exam.setSacralSlope(parseDouble(row[3]));
        exam.setPelvicRadius(parseDouble(row[4]));
        exam.setDegreeSpondylolisthesis(parseDouble(row[5]));

        // Diagnosis class
        String classLabel = row[6].trim();
        int classId = diagnosisClassDao.getClassIdByLabel(classLabel);
        exam.setClassId(classId);

        // Notes
        exam.setNotes("Imported from CSV");

        return exam;
    }

    /**
     * Safely parse double
     */
    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Progress callback interface
     */
    public interface ProgressCallback {
        void onProgress(int current, int total);
    }
}
