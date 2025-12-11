package com.vertebralcare.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * 检查记录实体类
 * 对应数据库表 Exam
 */
public class Exam {
    private Integer examId;
    private Integer patientId;
    private Date examDate;

    // 6 biomechanical indicators
    private Double pelvicIncidence;         // 骨盆入射角
    private Double pelvicTilt;              // 骨盆倾斜角
    private Double lumbarLordosisAngle;     // 腰椎前凸角
    private Double sacralSlope;             // 骶骨倾斜角
    private Double pelvicRadius;            // 骨盆半径
    private Double degreeSpondylolisthesis; // 脊椎滑脱程度

    private Integer classId;
    private String notes;
    private Timestamp createdAt;

    // Transient fields for display (joined from other tables)
    private String patientName;
    private String diagnosisCode;
    private String diagnosisDescription;

    public Exam() {}

    public Exam(Integer patientId, Date examDate, Double pelvicIncidence, Double pelvicTilt,
                Double lumbarLordosisAngle, Double sacralSlope, Double pelvicRadius,
                Double degreeSpondylolisthesis, Integer classId) {
        this.patientId = patientId;
        this.examDate = examDate;
        this.pelvicIncidence = pelvicIncidence;
        this.pelvicTilt = pelvicTilt;
        this.lumbarLordosisAngle = lumbarLordosisAngle;
        this.sacralSlope = sacralSlope;
        this.pelvicRadius = pelvicRadius;
        this.degreeSpondylolisthesis = degreeSpondylolisthesis;
        this.classId = classId;
    }

    // Getters and Setters
    public Integer getExamId() {
        return examId;
    }

    public void setExamId(Integer examId) {
        this.examId = examId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public Double getPelvicIncidence() {
        return pelvicIncidence;
    }

    public void setPelvicIncidence(Double pelvicIncidence) {
        this.pelvicIncidence = pelvicIncidence;
    }

    public Double getPelvicTilt() {
        return pelvicTilt;
    }

    public void setPelvicTilt(Double pelvicTilt) {
        this.pelvicTilt = pelvicTilt;
    }

    public Double getLumbarLordosisAngle() {
        return lumbarLordosisAngle;
    }

    public void setLumbarLordosisAngle(Double lumbarLordosisAngle) {
        this.lumbarLordosisAngle = lumbarLordosisAngle;
    }

    public Double getSacralSlope() {
        return sacralSlope;
    }

    public void setSacralSlope(Double sacralSlope) {
        this.sacralSlope = sacralSlope;
    }

    public Double getPelvicRadius() {
        return pelvicRadius;
    }

    public void setPelvicRadius(Double pelvicRadius) {
        this.pelvicRadius = pelvicRadius;
    }

    public Double getDegreeSpondylolisthesis() {
        return degreeSpondylolisthesis;
    }

    public void setDegreeSpondylolisthesis(Double degreeSpondylolisthesis) {
        this.degreeSpondylolisthesis = degreeSpondylolisthesis;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDiagnosisCode() {
        return diagnosisCode;
    }

    public void setDiagnosisCode(String diagnosisCode) {
        this.diagnosisCode = diagnosisCode;
    }

    public String getDiagnosisDescription() {
        return diagnosisDescription;
    }

    public void setDiagnosisDescription(String diagnosisDescription) {
        this.diagnosisDescription = diagnosisDescription;
    }

    @Override
    public String toString() {
        return "Exam{" +
                "examId=" + examId +
                ", patientId=" + patientId +
                ", examDate=" + examDate +
                ", diagnosisCode='" + diagnosisCode + '\'' +
                '}';
    }
}
