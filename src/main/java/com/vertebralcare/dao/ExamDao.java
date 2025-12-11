package com.vertebralcare.dao;

import com.vertebralcare.model.Exam;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 检查记录数据访问对象
 */
public class ExamDao {

    /**
     * 新增检查记录
     * @return 新增记录的ID
     */
    public int insert(Exam exam) throws SQLException {
        String sql = "INSERT INTO Exam (patient_id, exam_date, pelvic_incidence, pelvic_tilt, " +
                "lumbar_lordosis_angle, sacral_slope, pelvic_radius, degree_spondylolisthesis, " +
                "class_id, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, exam.getPatientId());
            ps.setDate(2, exam.getExamDate());
            ps.setDouble(3, exam.getPelvicIncidence());
            ps.setDouble(4, exam.getPelvicTilt());
            ps.setDouble(5, exam.getLumbarLordosisAngle());
            ps.setDouble(6, exam.getSacralSlope());
            ps.setDouble(7, exam.getPelvicRadius());
            ps.setDouble(8, exam.getDegreeSpondylolisthesis());
            ps.setInt(9, exam.getClassId());
            ps.setString(10, exam.getNotes());

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
     * 批量插入检查记录
     */
    public int batchInsert(List<Exam> exams) throws SQLException {
        String sql = "INSERT INTO Exam (patient_id, exam_date, pelvic_incidence, pelvic_tilt, " +
                "lumbar_lordosis_angle, sacral_slope, pelvic_radius, degree_spondylolisthesis, " +
                "class_id, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int count = 0;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (Exam exam : exams) {
                ps.setInt(1, exam.getPatientId());
                ps.setDate(2, exam.getExamDate());
                ps.setDouble(3, exam.getPelvicIncidence());
                ps.setDouble(4, exam.getPelvicTilt());
                ps.setDouble(5, exam.getLumbarLordosisAngle());
                ps.setDouble(6, exam.getSacralSlope());
                ps.setDouble(7, exam.getPelvicRadius());
                ps.setDouble(8, exam.getDegreeSpondylolisthesis());
                ps.setInt(9, exam.getClassId());
                ps.setString(10, exam.getNotes());
                ps.addBatch();
                count++;

                if (count % 100 == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        }
        return count;
    }

    /**
     * 更新检查记录
     */
    public boolean update(Exam exam) throws SQLException {
        String sql = "UPDATE Exam SET patient_id=?, exam_date=?, pelvic_incidence=?, pelvic_tilt=?, " +
                "lumbar_lordosis_angle=?, sacral_slope=?, pelvic_radius=?, degree_spondylolisthesis=?, " +
                "class_id=?, notes=? WHERE exam_id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, exam.getPatientId());
            ps.setDate(2, exam.getExamDate());
            ps.setDouble(3, exam.getPelvicIncidence());
            ps.setDouble(4, exam.getPelvicTilt());
            ps.setDouble(5, exam.getLumbarLordosisAngle());
            ps.setDouble(6, exam.getSacralSlope());
            ps.setDouble(7, exam.getPelvicRadius());
            ps.setDouble(8, exam.getDegreeSpondylolisthesis());
            ps.setInt(9, exam.getClassId());
            ps.setString(10, exam.getNotes());
            ps.setInt(11, exam.getExamId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * 删除检查记录
     */
    public boolean delete(int examId) throws SQLException {
        String sql = "DELETE FROM Exam WHERE exam_id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, examId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * 根据ID查询
     */
    public Exam findById(int examId) throws SQLException {
        String sql = "SELECT e.*, p.name as patient_name, d.code as diagnosis_code, d.description as diagnosis_desc " +
                "FROM Exam e " +
                "JOIN Patient p ON e.patient_id = p.patient_id " +
                "JOIN DiagnosisClass d ON e.class_id = d.class_id " +
                "WHERE e.exam_id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, examId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowWithJoin(rs);
                }
            }
        }
        return null;
    }

    /**
     * 根据病人ID查询所有检查记录
     */
    public List<Exam> findByPatientId(int patientId) throws SQLException {
        String sql = "SELECT e.*, p.name as patient_name, d.code as diagnosis_code, d.description as diagnosis_desc " +
                "FROM Exam e " +
                "JOIN Patient p ON e.patient_id = p.patient_id " +
                "JOIN DiagnosisClass d ON e.class_id = d.class_id " +
                "WHERE e.patient_id=? " +
                "ORDER BY e.exam_date DESC";

        List<Exam> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithJoin(rs));
                }
            }
        }
        return list;
    }

    /**
     * 查询所有检查记录
     */
    public List<Exam> findAll() throws SQLException {
        String sql = "SELECT e.*, p.name as patient_name, d.code as diagnosis_code, d.description as diagnosis_desc " +
                "FROM Exam e " +
                "JOIN Patient p ON e.patient_id = p.patient_id " +
                "JOIN DiagnosisClass d ON e.class_id = d.class_id " +
                "ORDER BY e.exam_date DESC, e.exam_id DESC";

        List<Exam> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRowWithJoin(rs));
            }
        }
        return list;
    }

    /**
     * 按诊断类别查询
     */
    public List<Exam> findByClassId(int classId) throws SQLException {
        String sql = "SELECT e.*, p.name as patient_name, d.code as diagnosis_code, d.description as diagnosis_desc " +
                "FROM Exam e " +
                "JOIN Patient p ON e.patient_id = p.patient_id " +
                "JOIN DiagnosisClass d ON e.class_id = d.class_id " +
                "WHERE e.class_id=? " +
                "ORDER BY e.exam_date DESC";

        List<Exam> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, classId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithJoin(rs));
                }
            }
        }
        return list;
    }

    /**
     * 获取检查记录总数
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Exam";

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
     * 按类别统计检查数量
     * @return Map<诊断描述, 数量>
     */
    public Map<String, Integer> countByClass() throws SQLException {
        String sql = "SELECT d.description, COUNT(*) as cnt " +
                "FROM Exam e " +
                "JOIN DiagnosisClass d ON e.class_id = d.class_id " +
                "GROUP BY d.class_id, d.description " +
                "ORDER BY cnt DESC";

        Map<String, Integer> result = new HashMap<>();

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.put(rs.getString("description"), rs.getInt("cnt"));
            }
        }
        return result;
    }

    /**
     * 计算各指标的平均值（按类别分组）
     */
    public Map<String, double[]> getAveragesByClass() throws SQLException {
        String sql = "SELECT d.description, " +
                "AVG(e.pelvic_incidence) as avg_pi, " +
                "AVG(e.pelvic_tilt) as avg_pt, " +
                "AVG(e.lumbar_lordosis_angle) as avg_lla, " +
                "AVG(e.sacral_slope) as avg_ss, " +
                "AVG(e.pelvic_radius) as avg_pr, " +
                "AVG(e.degree_spondylolisthesis) as avg_ds " +
                "FROM Exam e " +
                "JOIN DiagnosisClass d ON e.class_id = d.class_id " +
                "GROUP BY d.class_id, d.description";

        Map<String, double[]> result = new HashMap<>();

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                double[] avgs = new double[6];
                avgs[0] = rs.getDouble("avg_pi");
                avgs[1] = rs.getDouble("avg_pt");
                avgs[2] = rs.getDouble("avg_lla");
                avgs[3] = rs.getDouble("avg_ss");
                avgs[4] = rs.getDouble("avg_pr");
                avgs[5] = rs.getDouble("avg_ds");
                result.put(rs.getString("description"), avgs);
            }
        }
        return result;
    }

    /**
     * 映射结果集行到Exam对象（包含关联表字段）
     */
    private Exam mapRowWithJoin(ResultSet rs) throws SQLException {
        Exam exam = new Exam();
        exam.setExamId(rs.getInt("exam_id"));
        exam.setPatientId(rs.getInt("patient_id"));
        exam.setExamDate(rs.getDate("exam_date"));
        exam.setPelvicIncidence(rs.getDouble("pelvic_incidence"));
        exam.setPelvicTilt(rs.getDouble("pelvic_tilt"));
        exam.setLumbarLordosisAngle(rs.getDouble("lumbar_lordosis_angle"));
        exam.setSacralSlope(rs.getDouble("sacral_slope"));
        exam.setPelvicRadius(rs.getDouble("pelvic_radius"));
        exam.setDegreeSpondylolisthesis(rs.getDouble("degree_spondylolisthesis"));
        exam.setClassId(rs.getInt("class_id"));
        exam.setNotes(rs.getString("notes"));
        exam.setCreatedAt(rs.getTimestamp("created_at"));

        // Joined fields
        exam.setPatientName(rs.getString("patient_name"));
        exam.setDiagnosisCode(rs.getString("diagnosis_code"));
        exam.setDiagnosisDescription(rs.getString("diagnosis_desc"));

        return exam;
    }
}
