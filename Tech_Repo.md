# VertebralCare Spine Case Management System - Technical Report

## Technical Report: VertebralCare - Spine Case Management & Risk Assessment System

**Course**: Database and Data Science
**Project Name**: VertebralCare - Spine Case Management & Risk Assessment System
**Tech Stack**: MySQL 8.0 + Java 8 + JDBC + Maven + Swing GUI + JFreeChart

---

## I. Background & Motivation

### 1.1 Research Background

Spinal diseases are common health problems in modern society. In particular, Lower Back Pain affects hundreds of millions of people worldwide. Spinal abnormalities such as Spondylolisthesis and Disc Hernia can be identified and diagnosed early through biomechanical indicators.

This project is based on the **Vertebral Column Data Set** provided by the **UCI Machine Learning Repository**. The dataset contains 310 records of patients’ spinal biomechanical features, with each record containing 6 key indicators and a corresponding diagnostic class.

### 1.2 Project Motivation

1. **Clinical Demand**: Medical institutions need systematic management of spinal examination cases and tracking of patients’ treatment history
2. **Data Analysis Demand**: Through statistical analysis of indicator differences between normal/abnormal cases, assist clinical decision-making
3. **Educational Purpose**: Demonstrate core techniques such as normalized database design (BCNF), three-tier architecture, and GUI development

### 1.3 Dataset Overview

| Feature             | Description                                |
| ------------------- | ------------------------------------------ |
| Data Source         | UCI Machine Learning Repository            |
| Number of Records   | 310                                        |
| Number of Features  | 6 biomechanical indicators + 1 class label |
| Classification Task | Binary classification (Normal / Abnormal)  |

**Six Biomechanical Indicators**:

| Indicator Name              | English Full Name           | Abbreviation | Description                                                         |
| --------------------------- | --------------------------- | ------------ | ------------------------------------------------------------------- |
| Pelvic Incidence            | Pelvic Incidence            | PI           | Angle between pelvis and spine representing their relative position |
| Pelvic Tilt                 | Pelvic Tilt                 | PT           | Degree of pelvic tilt relative to the vertical axis                 |
| Lumbar Lordosis Angle       | Lumbar Lordosis Angle       | LL           | Curvature angle of the lumbar spine                                 |
| Sacral Slope                | Sacral Slope                | SS           | Angle between the sacral plane and the horizontal plane             |
| Pelvic Radius               | Pelvic Radius               | PR           | Geometric measurement parameter of the pelvis                       |
| Degree of Spondylolisthesis | Degree of Spondylolisthesis | GS           | Quantified degree of vertebral slippage                             |

---

## II. Database Design

### 2.1 Design Principles

This project strictly follows **BCNF (Boyce-Codd Normal Form)** in database design to ensure:

* Elimination of data redundancy
* Avoidance of update anomalies
* Guarantee of data integrity

### 2.2 E-R Diagram

```
┌─────────────┐         ┌─────────────┐         ┌─────────────────┐
│   Patient   │ 1     N │    Exam     │ N     1 │ DiagnosisClass  │
│─────────────│─────────│─────────────│─────────│─────────────────│
│ patient_id  │◄────────│ patient_id  │────────►│ class_id        │
│ name        │         │ exam_id     │         │ code            │
│ gender      │         │ exam_date   │         │ description     │
│ birth_date  │         │ 6 indicators│         └─────────────────┘
│ phone       │         │ class_id    │
│ created_at  │         │ notes       │
└─────────────┘         └─────────────┘
```

**Relationship Description**:

* **Patient 1:N Exam**: One patient can have multiple exam records
* **DiagnosisClass 1:N Exam**: One diagnosis class corresponds to multiple exam records

### 2.3 Table Structure Design

#### Table 1: Patient

```sql
CREATE TABLE Patient (
    patient_id    INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    gender        CHAR(1) CHECK (gender IN ('M', 'F')),
    birth_date    DATE,
    phone         VARCHAR(20),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**BCNF Analysis**: `patient_id → name, gender, birth_date, phone, created_at` (all non-key attributes fully functionally depend on the primary key)

#### Table 2: DiagnosisClass

```sql
CREATE TABLE DiagnosisClass (
    class_id      INT AUTO_INCREMENT PRIMARY KEY,
    code          VARCHAR(10) NOT NULL UNIQUE,
    description   VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Initial data
INSERT INTO DiagnosisClass (code, description) VALUES
('NO', 'Normal'),
('AB', 'Abnormal');
```

**BCNF Analysis**: `class_id → code, description`, and `code` is also a candidate key.

#### Table 3: Exam

```sql
CREATE TABLE Exam (
    exam_id                   INT AUTO_INCREMENT PRIMARY KEY,
    patient_id                INT NOT NULL,
    exam_date                 DATE NOT NULL,
    pelvic_incidence          DOUBLE NOT NULL,
    pelvic_tilt               DOUBLE NOT NULL,
    lumbar_lordosis_angle     DOUBLE NOT NULL,
    sacral_slope              DOUBLE NOT NULL,
    pelvic_radius             DOUBLE NOT NULL,
    degree_spondylolisthesis  DOUBLE NOT NULL,
    class_id                  INT NOT NULL,
    notes                     VARCHAR(500),
    created_at                TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (class_id) REFERENCES DiagnosisClass(class_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    INDEX idx_patient_id (patient_id),
    INDEX idx_exam_date (exam_date),
    INDEX idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**BCNF Analysis**: `exam_id → patient_id, exam_date, 6 indicators, class_id, notes, created_at`

### 2.4 Index Optimization

To improve query performance, three indexes are created on the Exam table:

* `idx_patient_id`: Filter exam records by patient
* `idx_exam_date`: Sort and perform range queries by date
* `idx_class_id`: Perform statistical analysis by diagnosis class

### 2.5 Foreign Key Constraint Strategy

| Foreign Key | ON DELETE | ON UPDATE | Reason                                                      |
| ----------- | --------- | --------- | ----------------------------------------------------------- |
| patient_id  | CASCADE   | CASCADE   | Automatically delete exam records when a patient is deleted |
| class_id    | RESTRICT  | CASCADE   | Prevent deleting diagnosis classes that are still in use    |

---

## III. System Architecture

### 3.1 Three-Tier Architecture Pattern

The system adopts the classic **Three-Tier Architecture**:

```
┌─────────────────────────────────────────────────────────────┐
│                    UI Layer (Swing GUI)                     │
│  MainFrame / PatientPanel / ExamPanel / StatisticsPanel     │
├─────────────────────────────────────────────────────────────┤
│                    Service Layer (Business Logic)           │
│  PatientService / ExamService / StatisticsService           │
├─────────────────────────────────────────────────────────────┤
│                    DAO Layer (Data Access)                  │
│  PatientDao / ExamDao / DiagnosisClassDao / DBUtil          │
├─────────────────────────────────────────────────────────────┤
│                    Database (MySQL)                         │
│  Patient / Exam / DiagnosisClass                            │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 Project File Structure

```
vertebralcare/
├── pom.xml                           # Maven configuration
├── run.sh                            # One-click startup script
├── src/main/
│   ├── java/com/vertebralcare/
│   │   ├── Main.java                 # Application entry point
│   │   ├── model/                    # Entity layer
│   │   │   ├── Patient.java          # Patient entity
│   │   │   ├── Exam.java             # Exam record entity
│   │   │   └── DiagnosisClass.java   # Diagnosis class entity
│   │   ├── dao/                      # Data access layer
│   │   │   ├── DBUtil.java           # Database connection utility
│   │   │   ├── PatientDao.java       # Patient CRUD
│   │   │   ├── ExamDao.java          # Exam CRUD + statistics
│   │   │   └── DiagnosisClassDao.java
│   │   ├── service/                  # Business logic layer
│   │   │   ├── PatientService.java   # Patient business logic
│   │   │   ├── ExamService.java      # Exam business logic
│   │   │   ├── StatisticsService.java# Statistical analysis service
│   │   │   └── ImportService.java    # CSV import service
│   │   └── ui/                       # UI layer
│   │       ├── MainFrame.java        # Main window
│   │       ├── PatientPanel.java     # Patient management panel
│   │       ├── ExamPanel.java        # Exam record panel
│   │       ├── StatisticsPanel.java  # Statistics panel
│   │       └── ImportPanel.java      # Data import panel
│   └── resources/
│       ├── db.properties             # Database configuration
│       └── init.sql                  # Database initialization script
└── Dataset_spine.csv                 # Original dataset
```

### 3.3 Technical Dependencies

```xml
<dependencies>
    <!-- MySQL JDBC Driver 8.0.33 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.0.33</version>
    </dependency>

    <!-- JFreeChart 1.5.3 for statistical charts -->
    <dependency>
        <groupId>org.jfree</groupId>
        <artifactId>jfreechart</artifactId>
        <version>1.5.3</version>
    </dependency>
</dependencies>
```

---

## IV. Feature Implementation

### 4.1 Patient Management Module (Patient Management)

**Description**: Provides complete CRUD operations for patient information.

**UI Layout**:

```
┌────────────────────────────────────────────────────┐
│ Search: [__________] [Search] [Add Patient]        │
├────────────────────────────────────────────────────┤
│ ID │ Name │ Gender │ Birth Date │ Phone │ Actions │
├────────────────────────────────────────────────────┤
│  1 │ John │ Male   │ 1985-03-15 │ (555)..│ [Edit]  │
│  2 │ Mary │ Female │ 1990-07-22 │ (555)..│ [Edit]  │
└────────────────────────────────────────────────────┘
```

**Core Features**:

* **Add Patient**: Input name, gender, birth date, and phone number through a form
* **Search Patient**: Fuzzy search by name (LIKE query)
* **Edit/Delete**: Double-click a row to edit; supports cascaded deletion of related exam records

**Data Validation**:

```java
// PatientService.java
private void validatePatient(Patient patient) {
    if (patient.getName() == null || patient.getName().trim().isEmpty()) {
        throw new IllegalArgumentException("Patient name is required");
    }
    if (patient.getGender() != null &&
        !patient.getGender().equals("M") && !patient.getGender().equals("F")) {
        throw new IllegalArgumentException("Gender must be 'M' or 'F'");
    }
}
```

### 4.2 Exam Records Module (Exam Records)

**Description**: Manages patients’ spinal examination records, recording six biomechanical indicators and the diagnostic result.

**UI Layout**:

```
┌─────────────────────────────────────────────────────────────────────┐
│ Patient: [All ▼]    Diagnosis: [All ▼]    [Add] [View] [Edit] [Del] │
├─────────────────────────────────────────────────────────────────────┤
│ ID │ Patient │ Date │  PI  │  PT  │  LL  │  SS  │  PR  │  GS │Diag │
├─────────────────────────────────────────────────────────────────────┤
│  1 │ John S. │ 2024 │63.03 │22.36 │54.12 │40.67 │98.67 │-0.25│Normal│
└─────────────────────────────────────────────────────────────────────┘
```

**Core Features**:

* **Add Exam**: Select patient → input six indicators → choose diagnosis result
* **Multi-dimensional Filtering**: Filter by patient and diagnosis class
* **Detail View**: Display all indicators and notes in full

**SQL Join Query**:

```sql
SELECT e.*, p.name as patient_name,
       d.code as diagnosis_code, d.description as diagnosis_desc
FROM Exam e
JOIN Patient p ON e.patient_id = p.patient_id
JOIN DiagnosisClass d ON e.class_id = d.class_id
ORDER BY e.exam_date DESC;
```

### 4.3 Statistics & Analytics Module (Statistics & Analytics)

**Description**: Visual display of case statistics and indicator comparison analysis.

**UI Layout**:

```
┌────────────────────────────────────────────────────────────────┐
│ ┌──────────┐ ┌──────────┐ ┌───────────┐ ┌────────────┐        │
│ │Patients: │ │ Exams:   │ │ Normal:   │ │ Abnormal:  │        │
│ │   310    │ │   310    │ │  32.3%    │ │   67.7%    │        │
│ └──────────┘ └──────────┘ └───────────┘ └────────────┘        │
├────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────┐    ┌─────────────────────┐           │
│  │    [Pie Chart]      │    │    [Bar Chart]      │           │
│  │  Diagnosis Class    │    │   Cases by Class    │           │
│  │   Distribution      │    │                     │           │
│  └─────────────────────┘    └─────────────────────┘           │
├────────────────────────────────────────────────────────────────┤
│ Indicator           │ Normal   │ Abnormal                     │
│ Pelvic Incidence    │ 51.5467  │ 68.6828                      │
│ Pelvic Tilt         │ 13.0689  │ 21.4629                      │
│ ...                 │ ...      │ ...                          │
└────────────────────────────────────────────────────────────────┘
```

**Core Features**:

1. **Overview Statistics Cards**: Total number of patients, total number of exams, normal/abnormal proportions
2. **Pie Chart**: Visualization of diagnosis class distribution
3. **Bar Chart**: Comparison of number of cases by diagnosis class
4. **Indicator Average Table**: Comparison of mean values of the six indicators between Normal and Abnormal

**Statistics SQL Implementation**:

```sql
-- Count by diagnosis class
SELECT d.description, COUNT(*) as cnt
FROM Exam e JOIN DiagnosisClass d ON e.class_id = d.class_id
GROUP BY d.class_id, d.description;

-- Compute mean of each indicator by diagnosis class
SELECT d.description,
       AVG(e.pelvic_incidence) as avg_pi,
       AVG(e.pelvic_tilt) as avg_pt,
       AVG(e.lumbar_lordosis_angle) as avg_lla,
       AVG(e.sacral_slope) as avg_ss,
       AVG(e.pelvic_radius) as avg_pr,
       AVG(e.degree_spondylolisthesis) as avg_ds
FROM Exam e JOIN DiagnosisClass d ON e.class_id = d.class_id
GROUP BY d.class_id, d.description;
```

**JFreeChart Chart Generation**:

```java
// Pie chart - diagnosis distribution
DefaultPieDataset pieDataset = new DefaultPieDataset();
for (Map.Entry<String, Integer> entry : countByClass.entrySet()) {
    pieDataset.setValue(entry.getKey(), entry.getValue());
}
JFreeChart pieChart = ChartFactory.createPieChart(
    "Diagnosis Class Distribution", pieDataset, true, true, false);

// Set colors: Normal = green, Abnormal = red
PiePlot piePlot = (PiePlot) pieChart.getPlot();
piePlot.setSectionPaint("Normal", new Color(46, 139, 87));
piePlot.setSectionPaint("Abnormal", new Color(205, 92, 92));
```

### 4.4 Data Import Module (CSV Import)

**Description**: Batch import of the Vertebral Column dataset, automatically generating virtual patient information.

**Import Workflow**:

```
CSV file → parse data rows → generate virtual patient → insert into Patient table →
        → parse 6 indicators → map diagnosis class → insert into Exam table
```

**Virtual Patient Generation Strategy**:

```java
private Patient generateVirtualPatient() {
    Patient patient = new Patient();

    // Random English name (40 first names × 30 last names = 1200 combinations)
    String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
    String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
    patient.setName(firstName + " " + lastName);

    // Random gender
    patient.setGender(random.nextBoolean() ? "M" : "F");

    // Random birth date (20–80 years old)
    long minDay = Date.valueOf("1945-01-01").getTime();
    long maxDay = Date.valueOf("2005-01-01").getTime();
    patient.setBirthDate(new Date(ThreadLocalRandom.current().nextLong(minDay, maxDay)));

    // Random US-style phone number
    patient.setPhone(String.format("(%03d) %03d-%04d",
        random.nextInt(900) + 100,
        random.nextInt(900) + 100,
        random.nextInt(10000)));

    return patient;
}
```

**CSV Parsing Logic**:

```java
// Supported CSV format: PI, PT, LL, SS, PR, GS, Class
// Example: 63.03,22.36,54.12,40.67,98.67,-0.25,Normal

private Exam parseExamFromRow(String[] row, int patientId) {
    Exam exam = new Exam();
    exam.setPatientId(patientId);

    // Parse 6 indicators
    exam.setPelvicIncidence(parseDouble(row[0]));
    exam.setPelvicTilt(parseDouble(row[1]));
    exam.setLumbarLordosisAngle(parseDouble(row[2]));
    exam.setSacralSlope(parseDouble(row[3]));
    exam.setPelvicRadius(parseDouble(row[4]));
    exam.setDegreeSpondylolisthesis(parseDouble(row[5]));

    // Map diagnosis class: "Normal" → class_id=1, "Abnormal" → class_id=2
    String classLabel = row[6].trim();
    int classId = diagnosisClassDao.getClassIdByLabel(classLabel);
    exam.setClassId(classId);

    return exam;
}
```

**Asynchronous Import and Progress Feedback**:

```java
SwingWorker<Integer, Integer> worker = new SwingWorker<>() {
    @Override
    protected Integer doInBackground() {
        return importService.importCSV(file, (current, total) -> {
            int percent = (int) ((current * 100.0) / total);
            publish(percent);  // Update progress bar
        });
    }

    @Override
    protected void done() {
        int count = get();
        showMessage("Import successful! " + count + " records imported");
        refreshCallback.run();  // Refresh other panels
    }
};
```

---

## V. Technical Highlights

### 5.1 SQL Injection Prevention

All database operations use **PreparedStatement** for parameterized queries:

```java
// Secure parameterized query
String sql = "SELECT * FROM Patient WHERE name LIKE ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setString(1, "%" + name + "%");

// Instead of string concatenation (dangerous!)
// String sql = "SELECT * FROM Patient WHERE name LIKE '%" + name + "%'";
```

### 5.2 Resource Management

Use **try-with-resources** to ensure that database connections are automatically closed:

```java
try (Connection conn = DBUtil.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    // Process result set
}  // rs, ps, conn are automatically closed
```

### 5.3 Transaction Processing

Transactions are used for batch import to ensure data consistency:

```java
conn.setAutoCommit(false);
try {
    for (Exam exam : exams) {
        ps.addBatch();
        if (count % 100 == 0) ps.executeBatch();
    }
    ps.executeBatch();
    conn.commit();
} catch (Exception e) {
    conn.rollback();
    throw e;
}
```

### 5.4 GUI Thread Safety

Long-running operations use **SwingWorker** to run in the background, avoiding UI freezing:

```java
// Start GUI on the Event Dispatch Thread
SwingUtilities.invokeLater(() -> {
    MainFrame frame = new MainFrame();
    frame.setVisible(true);
});

// Use SwingWorker for time-consuming operations
SwingWorker<Integer, Integer> worker = new SwingWorker<>() {
    @Override
    protected Integer doInBackground() {
        return importService.importCSV(file, callback);
    }
};
worker.execute();
```

---

## VI. Data Analysis Insights

### 6.1 Dataset Distribution

Based on the 310 records imported from the Vertebral Column dataset:

| Class    | Count | Ratio |
| -------- | ----- | ----- |
| Normal   | 100   | 32.3% |
| Abnormal | 210   | 67.7% |

### 6.2 Indicator Difference Analysis

From the mean value comparison table in the statistics panel, we can observe:

| Indicator         | Normal Mean | Abnormal Mean | Difference Analysis                              |
| ----------------- | ----------- | ------------- | ------------------------------------------------ |
| Pelvic Incidence  | ~51.5       | ~68.7         | Significantly higher in the abnormal group       |
| Pelvic Tilt       | ~13.1       | ~21.5         | Larger tilt in the abnormal group                |
| Lumbar Lordosis   | ~42.0       | ~53.0         | Greater curvature in the abnormal group          |
| Sacral Slope      | ~38.4       | ~47.2         | Higher slope in the abnormal group               |
| Pelvic Radius     | ~118.8      | ~119.2        | Difference not significant                       |
| Spondylolisthesis | ~0.5        | ~38.4         | Most significant difference (degree of slippage) |

**Key Finding**: Degree of Spondylolisthesis is the most significant indicator distinguishing Normal and Abnormal. The mean value in the abnormal group is about 38 times higher than in the normal group.

---

## VII. Deployment

### 7.1 One-Click Start Script

The project provides a `run.sh` script to perform one-click configuration and startup:

```bash
./run.sh
```

**Script Functions**:

1. Check Java, Maven, and MySQL environments
2. Start the MySQL service (if not running)
3. Interactively configure the database password
4. Execute init.sql to initialize the database
5. Compile the project using Maven
6. Run the application

### 7.2 Manual Run

```bash
# 1. Initialize the database
mysql -u root -p < src/main/resources/init.sql

# 2. Configure db.properties
db.url=jdbc:mysql://localhost:3306/vertebral_db
db.user=root
db.password=your_password

# 3. Compile and run
mvn clean compile
mvn exec:java -Dexec.mainClass="com.vertebralcare.Main"
```

---

## VIII. Conclusion

### 8.1 Project Achievements

This project successfully implements a complete spine case management system with:

1. **Normalized Database Design**: 3 tables satisfying BCNF, eliminating data redundancy
2. **Complete CRUD Functionality**: Patient management and exam record management
3. **Data Visualization**: JFreeChart pie charts and bar charts for diagnosis distribution
4. **Statistical Analysis Capability**: Class-based statistics and indicator mean comparison
5. **Batch Data Import**: Support for CSV file import with automatic virtual patient generation
6. **Security Assurance**: PreparedStatement for SQL injection prevention and transactions for data consistency

### 8.2 Technical Value

* Demonstrates practical application of normalized database design
* Illustrates the layered decoupling concept of three-tier architecture
* Implements best practices of GUI multithreaded programming
* Provides a complete workflow for data import and statistical analysis

### 8.3 Future Extensions

* Integrate machine learning models for automatic diagnostic prediction
* Add data export functionality (PDF reports)
* Support multi-user login and access control
* Develop a web version to provide remote access

---

## Appendix: Core File List

| File Path                        | Description                   | Lines |
| -------------------------------- | ----------------------------- | ----- |
| `Main.java`                      | Application entry point       | ~90   |
| `model/Patient.java`             | Patient entity class          | ~92   |
| `model/Exam.java`                | Exam record entity class      | ~200  |
| `model/DiagnosisClass.java`      | Diagnosis class entity class  | ~60   |
| `dao/DBUtil.java`                | Database utility class        | ~100  |
| `dao/PatientDao.java`            | Patient data access           | ~150  |
| `dao/ExamDao.java`               | Exam data access + statistics | ~330  |
| `dao/DiagnosisClassDao.java`     | Diagnosis class data access   | ~100  |
| `service/PatientService.java`    | Patient business logic        | ~80   |
| `service/ExamService.java`       | Exam business logic           | ~90   |
| `service/StatisticsService.java` | Statistical analysis service  | ~110  |
| `service/ImportService.java`     | CSV import service            | ~210  |
| `ui/MainFrame.java`              | Main window                   | ~200  |
| `ui/PatientPanel.java`           | Patient management panel      | ~350  |
| `ui/ExamPanel.java`              | Exam record panel             | ~500  |
| `ui/StatisticsPanel.java`        | Statistics panel              | ~230  |
| `ui/ImportPanel.java`            | Data import panel             | ~220  |

**In total, approximately 17 Java files and ~3100 lines of code**

---

*Report Completion Date: December 2025*
