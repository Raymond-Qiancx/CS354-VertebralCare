package com.vertebralcare.dao;

import com.vertebralcare.model.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 病人数据访问对象
 */
public class PatientDao {

    /**
     * 新增病人
     * @return 新增病人的ID
     */
    public int insert(Patient patient) throws SQLException {
        String sql = "INSERT INTO Patient (name, gender, birth_date, phone) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, patient.getName());
            ps.setString(2, patient.getGender());
            ps.setDate(3, patient.getBirthDate());
            ps.setString(4, patient.getPhone());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * 更新病人信息
     */
    public boolean update(Patient patient) throws SQLException {
        String sql = "UPDATE Patient SET name=?, gender=?, birth_date=?, phone=? WHERE patient_id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patient.getName());
            ps.setString(2, patient.getGender());
            ps.setDate(3, patient.getBirthDate());
            ps.setString(4, patient.getPhone());
            ps.setInt(5, patient.getPatientId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * 删除病人
     */
    public boolean delete(int patientId) throws SQLException {
        String sql = "DELETE FROM Patient WHERE patient_id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * 根据ID查询病人
     */
    public Patient findById(int patientId) throws SQLException {
        String sql = "SELECT * FROM Patient WHERE patient_id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * 根据姓名模糊查询
     */
    public List<Patient> findByName(String name) throws SQLException {
        String sql = "SELECT * FROM Patient WHERE name LIKE ? ORDER BY patient_id DESC";
        List<Patient> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * 查询所有病人
     */
    public List<Patient> findAll() throws SQLException {
        String sql = "SELECT * FROM Patient ORDER BY patient_id DESC";
        List<Patient> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * 获取病人总数
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Patient";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * 映射结果集行到Patient对象
     */
    private Patient mapRow(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setPatientId(rs.getInt("patient_id"));
        patient.setName(rs.getString("name"));
        patient.setGender(rs.getString("gender"));
        patient.setBirthDate(rs.getDate("birth_date"));
        patient.setPhone(rs.getString("phone"));
        patient.setCreatedAt(rs.getTimestamp("created_at"));
        return patient;
    }
}
