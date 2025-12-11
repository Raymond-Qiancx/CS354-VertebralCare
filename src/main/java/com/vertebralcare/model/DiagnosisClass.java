package com.vertebralcare.model;

/**
 * 诊断类别实体类
 * 对应数据库表 DiagnosisClass
 */
public class DiagnosisClass {
    private Integer classId;
    private String code;        // 'NO' for Normal, 'AB' for Abnormal
    private String description;

    public DiagnosisClass() {}

    public DiagnosisClass(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // Getters and Setters
    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isNormal() {
        return "NO".equals(code);
    }

    public boolean isAbnormal() {
        return "AB".equals(code);
    }

    @Override
    public String toString() {
        return description != null ? description : code;
    }
}
