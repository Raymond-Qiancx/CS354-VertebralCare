# VertebralCare Spine Case Management & Risk Assessment System

A spine case management system based on the Vertebral Column dataset, designed as a project for a database course.

## Tech Stack

* **Database**: MySQL 8.0
* **Backend**: Java 8+ / JDBC
* **Frontend**: Swing GUI
* **Build Tool**: Maven
* **Chart Library**: JFreeChart

## Project Structure

```bash
vertebralcare/
├── pom.xml                          # Maven configuration
├── src/main/
│   ├── java/com/vertebralcare/
│   │   ├── Main.java                # Application entry point
│   │   ├── model/                   # Entity classes (3)
│   │   │   ├── Patient.java
│   │   │   ├── Exam.java
│   │   │   └── DiagnosisClass.java
│   │   ├── dao/                     # Data access layer (4)
│   │   │   ├── DBUtil.java
│   │   │   ├── PatientDao.java
│   │   │   ├── ExamDao.java
│   │   │   └── DiagnosisClassDao.java
│   │   ├── service/                 # Business logic layer (4)
│   │   │   ├── PatientService.java
│   │   │   ├── ExamService.java
│   │   │   ├── StatisticsService.java
│   │   │   └── ImportService.java
│   │   └── ui/                      # Swing UI (5)
│   │       ├── MainFrame.java
│   │       ├── PatientPanel.java
│   │       ├── ExamPanel.java
│   │       ├── StatisticsPanel.java
│   │       └── ImportPanel.java
│   └── resources/
│       ├── db.properties            # Database configuration
│       └── init.sql                 # Initialization SQL script
└── README.md
```

## Quick Start

### 1. Configure the Database

1. Make sure the MySQL service is running
2. Modify the database password in `src/main/resources/db.properties`
3. Execute the initialization SQL:

   ```bash
   mysql -u root -p < src/main/resources/init.sql
   ```

### 2. Build and Run

```bash
# Go to project directory
cd vertebralcare

# Compile
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="com.vertebralcare.Main"

# Or package and then run
mvn clean package
java -jar target/vertebralcare-1.0.0-jar-with-dependencies.jar
```

## Functional Modules

### 1. Patient Management

* Add / edit / delete patients
* Search patients by name
* View patient list

### 2. Exam Records

* Add exam records for patients (6 biomechanical indicators)
* Filter records by patient / diagnosis class
* View exam details

### 3. Statistical Analysis

* Case count statistics
* Normal/Abnormal ratio pie chart
* Average value comparison of indicators by class

### 4. Data Import

* Import dataset in CSV format
* Automatically generate virtual patient information
* Batch import exam records

## Database Design

### E-R Relationships

* Patient (1) → (N) Exam
* DiagnosisClass (1) → (N) Exam

### Tables

| Table Name     | Description          | Primary Key |
| -------------- | -------------------- | ----------- |
| Patient        | Patient information  | patient_id  |
| DiagnosisClass | Diagnosis categories | class_id    |
| Exam           | Exam records         | exam_id     |

### BCNF Analysis

All three tables satisfy BCNF; see the course report for details.

## Dataset

Uses the [Kaggle Vertebral Column dataset](https://www.kaggle.com/datasets/jessanrod3/vertebralcolumndataset/data), containing 310 spinal exam records:

* 6 biomechanical indicators
* Binary label (Normal / Abnormal)

## License

For learning purposes only.
