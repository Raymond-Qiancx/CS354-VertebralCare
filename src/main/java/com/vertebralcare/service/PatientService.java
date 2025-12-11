package com.vertebralcare.service;

import com.vertebralcare.dao.PatientDao;
import com.vertebralcare.model.Patient;

import java.sql.SQLException;
import java.util.List;

/**
 * 病人业务逻辑服务
 */
public class PatientService {

    private final PatientDao patientDao;

    public PatientService() {
        this.patientDao = new PatientDao();
    }

    /**
     * 添加新病人
     */
    public int addPatient(Patient patient) throws SQLException {
        validatePatient(patient);
        return patientDao.insert(patient);
    }

    /**
     * 更新病人信息
     */
    public boolean updatePatient(Patient patient) throws SQLException {
        validatePatient(patient);
        return patientDao.update(patient);
    }

    /**
     * 删除病人
     */
    public boolean deletePatient(int patientId) throws SQLException {
        return patientDao.delete(patientId);
    }

    /**
     * 根据ID获取病人
     */
    public Patient getPatientById(int patientId) throws SQLException {
        return patientDao.findById(patientId);
    }

    /**
     * 根据姓名搜索病人
     */
    public List<Patient> searchByName(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            return getAllPatients();
        }
        return patientDao.findByName(name.trim());
    }

    /**
     * 获取所有病人
     */
    public List<Patient> getAllPatients() throws SQLException {
        return patientDao.findAll();
    }

    /**
     * 获取病人总数
     */
    public int getPatientCount() throws SQLException {
        return patientDao.count();
    }

    /**
     * 验证病人数据
     */
    private void validatePatient(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("病人信息不能为空");
        }
        if (patient.getName() == null || patient.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("病人姓名不能为空");
        }
        if (patient.getGender() != null && !patient.getGender().isEmpty()) {
            if (!"M".equals(patient.getGender()) && !"F".equals(patient.getGender())) {
                throw new IllegalArgumentException("性别必须是 M 或 F");
            }
        }
    }
}
