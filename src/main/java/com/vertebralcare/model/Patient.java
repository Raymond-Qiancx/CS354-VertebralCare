package com.vertebralcare.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Patient Entity Class
 * Corresponds to database table Patient
 */
public class Patient {
    private Integer patientId;
    private String name;
    private String gender;  // 'M' or 'F'
    private Date birthDate;
    private String phone;
    private Timestamp createdAt;

    public Patient() {}

    public Patient(String name, String gender, Date birthDate, String phone) {
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.phone = phone;
    }

    // Getters and Setters
    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getGenderDisplay() {
        if ("M".equals(gender)) return "Male";
        if ("F".equals(gender)) return "Female";
        return "-";
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", birthDate=" + birthDate +
                ", phone='" + phone + '\'' +
                '}';
    }
}
