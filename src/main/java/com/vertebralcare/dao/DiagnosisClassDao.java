package com.vertebralcare.dao;

import com.vertebralcare.model.DiagnosisClass;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 诊断类别数据访问对象
 */
public class DiagnosisClassDao {

    /**
     * 根据ID查询
     */
    public DiagnosisClass findById(int classId) throws SQLException {
        String sql = "SELECT * FROM DiagnosisClass WHERE class_id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, classId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * 根据Code查询
     */
    public DiagnosisClass findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM DiagnosisClass WHERE code=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * 查询所有诊断类别
     */
    public List<DiagnosisClass> findAll() throws SQLException {
        String sql = "SELECT * FROM DiagnosisClass ORDER BY class_id";
        List<DiagnosisClass> list = new ArrayList<>();

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
     * 根据CSV中的类别标签获取class_id
     * "Normal" -> NO的class_id
     * "Abnormal" -> AB的class_id
     */
    public int getClassIdByLabel(String label) throws SQLException {
        String code;
        if ("Normal".equalsIgnoreCase(label)) {
            code = "NO";
        } else if ("Abnormal".equalsIgnoreCase(label)) {
            code = "AB";
        } else {
            code = "AB"; // Default to Abnormal
        }

        DiagnosisClass dc = findByCode(code);
        return dc != null ? dc.getClassId() : -1;
    }

    /**
     * 映射结果集行到DiagnosisClass对象
     */
    private DiagnosisClass mapRow(ResultSet rs) throws SQLException {
        DiagnosisClass dc = new DiagnosisClass();
        dc.setClassId(rs.getInt("class_id"));
        dc.setCode(rs.getString("code"));
        dc.setDescription(rs.getString("description"));
        return dc;
    }
}
